/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.codecrafter.smartAfk.AFKOG;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AfkManager {

	private final Set<UUID> afkPlayers = new HashSet<>();
	private final Map<UUID, Location> afkPositions = new HashMap<>();
	private final Map<UUID, Long> lastActivities = new HashMap<>();
	// main-thread only (Bukkit events + the AFK check task).
	private final Map<UUID, Deque<Long>> interactionTimes = new HashMap<>();
	private final Map<UUID, TargetState> targetStates = new HashMap<>();
	// written from NoCheatPlus' NET check, which may run off the main thread.
	private final Map<UUID, Long> lastAttackFlags = new ConcurrentHashMap<>();
	private static final int MAX_SAMPLES = 64;
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
	private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+");
	private static final TextReplacementConfig URL_REPLACEMENT = TextReplacementConfig.builder()
			.match(URL_PATTERN)
			.replacement((match, builder) -> {
				final String url = match.group();
				return Component.text(url)
						.decorate(TextDecoration.UNDERLINED)
						.clickEvent(ClickEvent.openUrl(url))
						.hoverEvent(HoverEvent.showText(Component.text("Click to open " + url)));
			})
			.build();

	private static Component legacy(String text) {

		return LEGACY.deserialize(text);

	}

	private static Component legacyClickable(String text) {

		return LEGACY.deserialize(text).replaceText(URL_REPLACEMENT);

	}

	public void setAfk(Player player) {

		afkPlayers.add(player.getUniqueId());
		afkPositions.put(player.getUniqueId(), player.getLocation());

		player.sendMessage(legacy(AFKOG.getPrefix() + "&cYou are now AFK."));

		clearMobTargets(player);

	}

	private void clearMobTargets(Player player) {

		player.getWorld().getNearbyEntities(player.getLocation(), 64.0D, 64.0D, 64.0D).forEach(entity -> {

			if (entity instanceof Mob mob && mob.getTarget() != null && mob.getTarget().getUniqueId().equals(player.getUniqueId())) {

				mob.setTarget(null);

			}

		});

	}

	public void unsetAfk(Player player) {

		afkPlayers.remove(player.getUniqueId());
		afkPositions.remove(player.getUniqueId());

		player.sendMessage(legacy(AFKOG.getPrefix() + "&aYou are no longer AFK."));

	}

	public boolean isAfk(Player player) {

		return afkPlayers.contains(player.getUniqueId());

	}

	public Location getAfkPosition(Player player) {

		return afkPositions.get(player.getUniqueId());

	}

	public void updateActivity(Player player) {

		lastActivities.put(player.getUniqueId(), System.currentTimeMillis());

	}

	/**
	 * Records the timestamp of an interaction (click, attack, menu use, block
	 * place/break) for use in cadence analysis. Bounded to {@link #MAX_SAMPLES}
	 * per player.
	 */
	public void recordInteraction(Player player) {

		final Deque<Long> times = interactionTimes.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
		final long now = System.currentTimeMillis();

		// dedupe events that share a millisecond (some actions fire multiple
		// events) so duplicates cannot inflate the sample count or produce
		// zero-interval (degenerate) cadence.
		if (!times.isEmpty() && times.peekLast().longValue() == now) {

			return;

		}

		times.addLast(now);

		while (times.size() > MAX_SAMPLES) {

			times.removeFirst();

		}

	}

	/**
	 * Returns true when the player's recent interaction cadence looks automated:
	 * a sustained burst (at least {@code minSamples} interactions within
	 * {@code recentSpanMs}) whose timing regularity (coefficient of variation) is
	 * below {@code cvThreshold}. Too few or too sparse samples are always treated
	 * as human (returns false).
	 */
	public boolean isRoboticCadence(Player player, int minSamples, long recentSpanMs, double cvThreshold) {

		final int floor = Math.max(minSamples, 2);
		final Deque<Long> times = interactionTimes.get(player.getUniqueId());
		if (times == null || times.size() < floor) {

			return false;

		}

		final long cutoff = System.currentTimeMillis() - recentSpanMs;
		final List<Long> recent = new ArrayList<>();
		for (final long t : times) {

			if (t >= cutoff) {

				recent.add(t);

			}

		}

		if (recent.size() < floor) {

			return false;

		}

		final int n = recent.size() - 1;
		double sum = 0.0D;
		final double[] intervals = new double[n];
		for (int i = 0; i < n; i++) {

			final double interval = recent.get(i + 1) - recent.get(i);
			intervals[i] = interval;
			sum += interval;

		}

		final double mean = sum / n;
		if (mean <= 0.0D) {

			return false;

		}

		double squared = 0.0D;
		for (final double interval : intervals) {

			final double delta = interval - mean;
			squared += delta * delta;

		}

		final double cv = Math.sqrt(squared / n) / mean;
		return cv < cvThreshold;

	}

	private static final class TargetState {

		private String key;
		private int count;
		private long time;

	}

	/**
	 * Records an interaction against a target identity (a block position, an
	 * entity, an inventory slot, or "air") and returns true once the player has
	 * hit the SAME target at least {@code autoclick-max-same-target} times in a
	 * row without interacting with any different target in between, within the
	 * idle window. This catches auto-clicking a single button/lever/sign/slot or
	 * empty air at any rate or jitter. A different target resets the streak.
	 */
	public boolean isRepeatedTarget(Player player, String targetKey) {

		final AfkConfig config = AFKOG.getPlugin().getAfkConfig();
		final int max = config.getAutoclickMaxSameTarget();
		final int timeoutSeconds = config.getAfkTimeoutSeconds();
		// a target hit more than one idle window ago can no longer keep the
		// player non-AFK, so it starts a fresh streak.
		final long windowMs = (timeoutSeconds > 0 ? timeoutSeconds : 300) * 1000L;
		final long now = System.currentTimeMillis();

		final TargetState state = targetStates.computeIfAbsent(player.getUniqueId(), k -> new TargetState());
		if (targetKey.equals(state.key) && now - state.time <= windowMs) {

			state.count++;

		} else {

			state.key = targetKey;
			state.count = 1;

		}
		state.time = now;

		return state.count >= max;

	}

	/**
	 * Convenience wrapper applying the configured cadence thresholds.
	 */
	public boolean isAutomatedCadence(Player player) {

		final AfkConfig config = AFKOG.getPlugin().getAfkConfig();
		return isRoboticCadence(player, config.getAutoclickMinClicks(), config.getAutoclickWindowSeconds() * 1000L, config.getAutoclickCvThreshold());

	}

	/**
	 * True when combat activity should be withheld: either a robotic cadence or a
	 * recent NoCheatPlus attack-frequency flag (when NoCheatPlus is installed).
	 */
	public boolean isCombatAutomated(Player player) {

		final AfkConfig config = AFKOG.getPlugin().getAfkConfig();
		if (isRecentlyAttackFlagged(player, config.getNcpFlagWindowSeconds() * 1000L)) {

			return true;

		}

		return isAutomatedCadence(player);

	}

	/**
	 * Records that NoCheatPlus flagged the player for excessive attack frequency.
	 */
	public void flagAttackFrequency(Player player) {

		lastAttackFlags.put(player.getUniqueId(), System.currentTimeMillis());

	}

	private boolean isRecentlyAttackFlagged(Player player, long windowMs) {

		final Long flagged = lastAttackFlags.get(player.getUniqueId());
		return flagged != null && System.currentTimeMillis() - flagged < windowMs;

	}

	public void initializeSession(Player player) {

		final UUID playerId = player.getUniqueId();
		afkPlayers.remove(playerId);
		afkPositions.remove(playerId);
		interactionTimes.remove(playerId);
		targetStates.remove(playerId);
		lastAttackFlags.remove(playerId);
		lastActivities.put(playerId, System.currentTimeMillis());

	}

	public void clearSession(Player player) {

		final UUID playerId = player.getUniqueId();
		afkPlayers.remove(playerId);
		afkPositions.remove(playerId);
		interactionTimes.remove(playerId);
		targetStates.remove(playerId);
		lastAttackFlags.remove(playerId);
		lastActivities.remove(playerId);

	}

	public void clearLegacyInvulnerability(Player player) {

		if (!isAfk(player) && player.isInvulnerable()) {

			player.setInvulnerable(false);

		}

	}

	public long getLastActivity(Player player) {

		return lastActivities.getOrDefault(player.getUniqueId(), System.currentTimeMillis());

	}

	public void startAfkCheckTask(AFKOG plugin) {

		new BukkitRunnable() {

			final int timeoutSeconds = plugin.getAfkConfig().getAfkTimeoutSeconds();
			final int kickSeconds = plugin.getAfkConfig().getAfkKickSeconds();
			final String kickMessage = plugin.getAfkConfig().getAfkKickMessage();

			@Override
			public void run() {

				plugin.getServer().getOnlinePlayers().forEach(player -> {

					final long idleMillis = System.currentTimeMillis() - getLastActivity(player);

					if (kickSeconds > 0 && idleMillis > kickSeconds * 1000L && !player.hasPermission("afkog.exempt")) {

						player.kick(legacyClickable(kickMessage));

						return;

					}

					if (isAfk(player)) {

						return;

					}

					if (timeoutSeconds > 0 && idleMillis > timeoutSeconds * 1000L) {

						setAfk(player);

					}

				});

			}

			// Every 5 seconds.
		}.runTaskTimer(plugin, 20L, 100L);

	}

}

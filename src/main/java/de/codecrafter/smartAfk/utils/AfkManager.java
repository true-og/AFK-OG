/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.codecrafter.smartAfk.AFKOG;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class AfkManager {

	private final Set<UUID> afkPlayers = new HashSet<>();
	private final Map<UUID, Location> afkPositions = new HashMap<>();
	private final Map<UUID, Long> lastActivities = new HashMap<>();
	private final Map<UUID, Long> lastLookChanges = new HashMap<>();
	private static final long INTERACT_LOOK_WINDOW_MS = 5000L;
	private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

	private static Component legacy(String text) {

		return LEGACY.deserialize(text);

	}

	public void setAfk(Player player) {

		afkPlayers.add(player.getUniqueId());
		afkPositions.put(player.getUniqueId(), player.getLocation());

		final String name = player.getName();

		player.displayName(legacy(AFKOG.getPrefix() + "&r" + name));
		player.playerListName(legacy(AFKOG.getPrefix() + "&r" + name));

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

		final Component name = Component.text(player.getName());

		player.displayName(name);
		player.playerListName(name);

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

	public void updateLook(Player player) {

		lastLookChanges.put(player.getUniqueId(), System.currentTimeMillis());

	}

	public boolean hasRecentLookChange(Player player) {

		final Long last = lastLookChanges.get(player.getUniqueId());
		return last != null && System.currentTimeMillis() - last < INTERACT_LOOK_WINDOW_MS;

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

						player.kick(legacy(kickMessage));

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

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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import de.codecrafter.smartAfk.AFKOG;
import net.kyori.adventure.text.Component;

public class AfkManager {

	private final Set<UUID> afkPlayers = new HashSet<>();
	private final Map<UUID, Location> afkPositions = new HashMap<>();
	private final Map<UUID, Long> lastActivities = new HashMap<>();
	private static final String AFK_PREFIX = "&c[AFK] ";

	public void setAfk(Player player) {

		afkPlayers.add(player.getUniqueId());
		afkPositions.put(player.getUniqueId(), player.getLocation());

		final String name = player.getName();

		player.displayName(Component.text(AFK_PREFIX + "&r" + name));
		player.playerListName(Component.text(AFK_PREFIX + "&r" + name));

		player.sendMessage(AFK_PREFIX + "&c You are now AFK.");

	}

	public void unsetAfk(Player player) {

		afkPlayers.remove(player.getUniqueId());
		afkPositions.remove(player.getUniqueId());

		final Component name = Component.text(player.getName());

		player.displayName(name);
		player.playerListName(name);

		player.sendMessage(AFK_PREFIX + "&a You are no longer AFK.");

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

			@Override
			public void run() {

				plugin.getServer().getOnlinePlayers().forEach(player -> {

					if (isAfk(player)) {

						return;

					}

					if (System.currentTimeMillis() - getLastActivity(player) > timeoutSeconds * 1000L) {

						setAfk(player);

					}

				});

			}

			// Every 5 seconds.   
		}.runTaskTimer(plugin, 20L, 100L);

	}

}
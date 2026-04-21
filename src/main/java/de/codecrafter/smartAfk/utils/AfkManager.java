/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import de.codecrafter.smartAfk.SmartAfk;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AfkManager {
    private final Set<UUID> afkPlayers = new HashSet<>();
    private final Map<UUID, Location> afkPositions = new HashMap<>();
    private final Map<UUID, Long> lastActivities = new HashMap<>();
    private static final String AFK_PREFIX = ChatColor.RED + "[AFK] ";

    public void setAfk(Player player) {
        afkPlayers.add(player.getUniqueId());
        afkPositions.put(player.getUniqueId(), player.getLocation());

        String name = player.getName();
        player.setDisplayName(AFK_PREFIX + ChatColor.RESET + name);
        player.setPlayerListName(AFK_PREFIX + ChatColor.RESET + name);

        player.sendMessage(ChatColor.GREEN + "Du bist jetzt Afk.");
    }

    public void unsetAfk(Player player) {
        afkPlayers.remove(player.getUniqueId());
        afkPositions.remove(player.getUniqueId());

        String name = player.getName();
        player.setDisplayName(name);
        player.setPlayerListName(name);

        player.sendMessage(ChatColor.YELLOW + "Du bist jetzt nicht mehr Afk.");
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

    public long getLastActivity(Player player) {
        return lastActivities.getOrDefault(player.getUniqueId(), System.currentTimeMillis());
    }

    public void startAfkCheckTask(SmartAfk plugin) {
        new BukkitRunnable() {
            final int timeoutSeconds = plugin.getAfkConfig().getAfkTimeoutSeconds();

            @Override
            public void run() {
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (isAfk(player)) return;

                    if (System.currentTimeMillis() - getLastActivity(player) > timeoutSeconds * 1000L) {
                        setAfk(player);
                    }
                });
            }
        }.runTaskTimer(plugin, 20L, 100L); // every 5 seconds
    }
}

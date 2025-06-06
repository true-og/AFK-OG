/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import de.codecrafter.smartAfk.SmartAfk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class AfkManager {
    private final Set<UUID> afkPlayers = new HashSet<>();
    private final Map<UUID, Location> afkPositions = new HashMap<>();
    private final Map<UUID, Long> lastActivities = new HashMap<>();
    private static final Component AFK_PREFIX = Component.text("[AFK] ", NamedTextColor.RED);

    public void setAfk(Player player) {
        afkPlayers.add(player.getUniqueId());
        afkPositions.put(player.getUniqueId(), player.getLocation());
        if (SmartAfk.getPlugin().getAfkConfig().isInvulnerableDuringAfk()) player.setInvulnerable(true);

        Component name = player.name();
        player.displayName(AFK_PREFIX.append(name.color(NamedTextColor.WHITE)));
        player.playerListName(AFK_PREFIX.append(name.color(NamedTextColor.WHITE)));

        player.sendMessage(Component.text("Du bist jetzt Afk.", NamedTextColor.GREEN));
    }

    public void unsetAfk(Player player) {
        afkPlayers.remove(player.getUniqueId());
        afkPositions.remove(player.getUniqueId());
        if (SmartAfk.getPlugin().getAfkConfig().isInvulnerableDuringAfk()) player.setInvulnerable(false);

        Component name = player.name();
        player.displayName(name);
        player.playerListName(name);

        player.sendMessage(Component.text("Du bist jetzt nicht mehr Afk.", NamedTextColor.YELLOW));
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

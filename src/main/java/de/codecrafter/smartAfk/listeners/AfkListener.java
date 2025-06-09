/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.listeners;

import de.codecrafter.smartAfk.SmartAfk;
import de.codecrafter.smartAfk.utils.AfkConfig;
import de.codecrafter.smartAfk.utils.AfkManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        SmartAfk smartAfk = SmartAfk.getPlugin();
        AfkManager afkManager = smartAfk.getAfkManager();
        AfkConfig afkConfig = smartAfk.getAfkConfig();
        afkManager.updateActivity(player);

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {
            return;
        }


        if (afkManager.isAfk(player)) {
            if (afkConfig.isFreezeAfkPlayers()) {
                Location from = event.getFrom();
                Location origTo = event.getTo();
                if (afkConfig.isCancelAfkOnJump() && origTo.getY() >= from.getBlockY() + 1) {
                    afkManager.unsetAfk(player);
                    return;
                }

                event.setTo(from);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        AfkManager afkManager = SmartAfk.getPlugin().getAfkManager();
        afkManager.updateActivity(player);
        if (afkManager.isAfk(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        AfkManager afkManager = SmartAfk.getPlugin().getAfkManager();
        afkManager.updateActivity(player);
        if (afkManager.isAfk(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        AfkManager afkManager = SmartAfk.getPlugin().getAfkManager();
        afkManager.updateActivity(player);

        if (afkManager.isAfk(player)) {
            event.setCancelled(true);
        }
    }
}

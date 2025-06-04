/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.listeners;

import de.codecrafter.smartAfk.utils.UpdateChecker;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private final UpdateChecker updateChecker;

    public PlayerJoinListener(UpdateChecker updateChecker) {
        this.updateChecker = updateChecker;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (updateChecker.isUpdateAvailable()) {
            player.sendMessage(
                    Component.text("A new version of ", NamedTextColor.YELLOW)
                            .append(Component.text("SmartAfk", NamedTextColor.GOLD))
                            .append(Component.text(" is available: ", NamedTextColor.YELLOW))
                            .append(Component.text("v" + updateChecker.getLatestVersion(), NamedTextColor.GREEN))
                            .append(Component.text(" (you have ", NamedTextColor.YELLOW))
                            .append(Component.text("v" + updateChecker.getCurrentVersion(), NamedTextColor.RED))
                            .append(Component.text(")", NamedTextColor.YELLOW)));
        }
    }
}

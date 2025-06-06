/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.listeners;

import de.codecrafter.smartAfk.utils.UpdateChecker;
import org.bukkit.ChatColor;
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
            player.sendMessage(ChatColor.YELLOW + "A new version of " +
                    ChatColor.GOLD + "SmartAfk" +
                    ChatColor.YELLOW + " is available: " +
                    ChatColor.GREEN + "v" + updateChecker.getLatestVersion() +
                    ChatColor.YELLOW + " (you have " +
                    ChatColor.RED + "v" + updateChecker.getCurrentVersion() +
                    ChatColor.YELLOW + ")");
        }
    }
}

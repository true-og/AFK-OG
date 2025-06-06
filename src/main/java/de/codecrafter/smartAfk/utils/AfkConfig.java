/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import de.codecrafter.smartAfk.SmartAfk;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

public class AfkConfig {
    private final SmartAfk plugin;

    // config values
    private boolean freezeAfkPlayers;
    private boolean cancelAfkOnMove;
    private boolean cancelAfkOnJump;
    private boolean invulnerableDuringAfk;
    private int afkTimeoutSeconds;

    /**
     * Creates an instance of {@code TimerConfig} class.
     *
     * @param plugin The plugin.
     */
    public AfkConfig(SmartAfk plugin) {
        this.plugin = plugin;
        this.load(plugin.getConfig());
    }

    /**
     * Reloads the config from the config file on the disk.
     */
    public void reload() {
        plugin.reloadConfig();
        load(plugin.getConfig());
        plugin.getLogger().info(ChatColor.GREEN + "Timer config reloaded.");
    }

    /**
     * Populates the attributes of the class instance with the values from the config file.
     *
     * @param config The config object.
     */
    private void load(FileConfiguration config) {
        this.freezeAfkPlayers = config.getBoolean("freeze-afk-players", true);
        this.cancelAfkOnJump = config.getBoolean("cancel-afk-on-jump", true);
        this.cancelAfkOnMove = config.getBoolean("cancel-afk-on-move", true);
        this.invulnerableDuringAfk = config.getBoolean("invulnerable-during-afk", true);
        this.afkTimeoutSeconds = config.getInt("afk-timeout-seconds", 120);
    }

    public boolean isFreezeAfkPlayers() {
        return freezeAfkPlayers;
    }

    public boolean isCancelAfkOnJump() {
        return cancelAfkOnJump;
    }

    public boolean isCancelAfkOnMove() {
        return cancelAfkOnMove;
    }

    public boolean isInvulnerableDuringAfk() {
        return invulnerableDuringAfk;
    }

    public int getAfkTimeoutSeconds() {
        return afkTimeoutSeconds;
    }
}

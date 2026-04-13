/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk;

import de.codecrafter.smartAfk.commands.AfkCommand;
import de.codecrafter.smartAfk.listeners.AfkListener;
import de.codecrafter.smartAfk.listeners.PlayerJoinListener;
import de.codecrafter.smartAfk.placeholders.AfkPlaceholderExpansion;
import de.codecrafter.smartAfk.utils.AfkConfig;
import de.codecrafter.smartAfk.utils.AfkManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class SmartAfk extends JavaPlugin {
    private static SmartAfk plugin;
    private AfkConfig afkConfig;
    private AfkManager afkManager;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        afkConfig = new AfkConfig(this);
        afkManager = new AfkManager();

        // commands
        getCommand("afk").setExecutor(new AfkCommand());

        // listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new AfkListener(), this);

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new AfkPlaceholderExpansion(this).register();
        }

        // start the afk check task if in config enabled
        if (afkConfig.getAfkTimeoutSeconds() > 0) afkManager.startAfkCheckTask(this);
    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(player -> {
            if (afkManager.isAfk(player)) afkManager.unsetAfk(player);
        });
    }

    public static SmartAfk getPlugin() {
        return plugin;
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    public AfkConfig getAfkConfig() {
        return afkConfig;
    }
}

/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk;

import de.codecrafter.smartAfk.commands.AfkCommand;
import de.codecrafter.smartAfk.listeners.AfkListener;
import de.codecrafter.smartAfk.listeners.PlayerJoinListener;
import de.codecrafter.smartAfk.utils.AfkConfig;
import de.codecrafter.smartAfk.utils.AfkManager;
import de.codecrafter.smartAfk.utils.UpdateChecker;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

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

        // config

        // update checker
        BukkitScheduler scheduler = getServer().getScheduler();
        UpdateChecker updateChecker = new UpdateChecker(this);
        scheduler.runTaskTimerAsynchronously(this, updateChecker::check, 0, 72000);

        // commands
        getCommand("afk").setExecutor(new AfkCommand());

        // listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(updateChecker), this);
        getServer().getPluginManager().registerEvents(new AfkListener(), this);

        // start the afk check task if in config enabled
        if (afkConfig.getAfkTimeoutSeconds() > 0) afkManager.startAfkCheckTask(this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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

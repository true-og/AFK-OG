/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk;

import org.bukkit.plugin.java.JavaPlugin;

import de.codecrafter.smartAfk.commands.AfkCommand;
import de.codecrafter.smartAfk.integrations.NoCheatPlusHook;
import de.codecrafter.smartAfk.listeners.AfkListener;
import de.codecrafter.smartAfk.listeners.PlayerJoinListener;
import de.codecrafter.smartAfk.placeholders.AfkPlaceholderExpansion;
import de.codecrafter.smartAfk.utils.AfkConfig;
import de.codecrafter.smartAfk.utils.AfkManager;

public final class AFKOG extends JavaPlugin {
	private static AFKOG plugin;
	private AfkConfig afkConfig;
	private AfkManager afkManager;
	private static String prefix = "&7[&cAFK&f-&4OG&7] ";

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

		// optional NoCheatPlus backstop for combat auto-click detection.
		NoCheatPlusHook.register(this, afkManager);

		if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {

			new AfkPlaceholderExpansion(this).register();

		}

		getServer().getOnlinePlayers().forEach(afkManager::clearLegacyInvulnerability);

		// start the afk check task if either auto-afk or auto-kick is enabled.
		if (afkConfig.getAfkTimeoutSeconds() > 0 || afkConfig.getAfkKickSeconds() > 0) {

			afkManager.startAfkCheckTask(this);

		}

	}

	@Override
	public void onDisable() {

		NoCheatPlusHook.unregister(this);

		getServer().getOnlinePlayers().stream().filter(afkManager::isAfk).forEach(afkManager::unsetAfk);

	}

	public static AFKOG getPlugin() {

		return plugin;

	}

	public AfkManager getAfkManager() {

		return afkManager;

	}

	public AfkConfig getAfkConfig() {

		return afkConfig;

	}

	public static String getPrefix() {

                return prefix;

        }

}

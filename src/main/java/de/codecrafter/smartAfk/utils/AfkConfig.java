/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import org.bukkit.configuration.file.FileConfiguration;

import de.codecrafter.smartAfk.AFKOG;

public class AfkConfig {

	private final AFKOG plugin;

	// Config values.
	private boolean freezeAfkPlayers;
	private boolean cancelAfkOnJump;
	private boolean invulnerableDuringAfk;
	private int afkTimeoutSeconds;

	/**
	 * Creates an instance of {@code TimerConfig} class.
	 *
	 * @param plugin The plugin.
	 */
	public AfkConfig(AFKOG plugin) {

		this.plugin = plugin;
		this.load(plugin.getConfig());

	}

	/**
	 * Reloads the config from the config file on the disk.
	 */
	public void reload() {

		plugin.reloadConfig();
		load(plugin.getConfig());

		plugin.getLogger().info("AFK-OG config file reloaded.");

	}

	/**
	 * Populates the attributes of the class instance with the values from the config file.
	 *
	 * @param config The config object.
	 */
	private void load(FileConfiguration config) {

		this.afkTimeoutSeconds = config.getInt("afk-timeout-seconds", 120);
		this.freezeAfkPlayers = config.getBoolean("freeze-afk-players", true);
		this.cancelAfkOnJump = config.getBoolean("cancel-afk-on-jump", true);
		this.invulnerableDuringAfk = config.getBoolean("invulnerable-during-afk", true);

	}

	public int getAfkTimeoutSeconds() {

		return afkTimeoutSeconds;

	}

	public boolean isFreezeAfkPlayers() {

		return freezeAfkPlayers;

	}

	public boolean isCancelAfkOnJump() {

		return cancelAfkOnJump;

	}

	public boolean isInvulnerableDuringAfk() {

		return invulnerableDuringAfk;

	}

}
/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;

import de.codecrafter.smartAfk.AFKOG;

public class AfkConfig {

	private final AFKOG plugin;

	// Config values.
	private boolean freezeAfkPlayers;
	private boolean cancelAfkOnJump;
	private boolean invulnerableDuringAfk;
	private int afkTimeoutSeconds;
	private int afkKickSeconds;
	private String afkKickMessage;
	private int autoclickMinClicks;
	private int autoclickWindowSeconds;
	private double autoclickCvThreshold;
	private int autoclickMaxSameTarget;
	private int ncpFlagWindowSeconds;
	// lowercased world names where going AFK is broadcast to everyone.
	private Set<String> afkBroadcastWorlds;
	private String afkBroadcastMessage;
	private String afkBroadcastReturnMessage;

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
		this.afkKickSeconds = config.getInt("afk-kick-seconds", 1200);
		this.afkKickMessage = config.getString("afk-kick-message", "&cYou have been kicked for being AFK too long.\n&7Buy a rank at &bhttps://shop.trueog.net &7to bypass the AFK timer.");
		// clamp to sane ranges so a misconfigured value cannot silently disable
		// detection or produce degenerate math.
		this.autoclickMinClicks = Math.max(2, config.getInt("autoclick-min-clicks", 10));
		this.autoclickWindowSeconds = Math.max(1, config.getInt("autoclick-window-seconds", 4));

		double cvThreshold = config.getDouble("autoclick-cv-threshold", 0.18D);
		if (Double.isNaN(cvThreshold) || cvThreshold <= 0.0D) {

			plugin.getLogger().warning("Invalid autoclick-cv-threshold (" + cvThreshold + "); falling back to 0.18.");
			cvThreshold = 0.18D;

		}
		this.autoclickCvThreshold = cvThreshold;

		this.autoclickMaxSameTarget = Math.max(2, config.getInt("autoclick-max-same-target", 6));
		this.ncpFlagWindowSeconds = Math.max(0, config.getInt("nocheatplus-flag-window-seconds", 2));

		final Set<String> worlds = new HashSet<>();
		final List<String> configured = config.getStringList("afk-broadcast-worlds");
		for (final String world : configured) {

			if (world != null && !world.isBlank()) {

				worlds.add(world.toLowerCase(Locale.ROOT));

			}

		}
		this.afkBroadcastWorlds = worlds;
		this.afkBroadcastMessage = config.getString("afk-broadcast-message", "&e%player% &7is now AFK.");
		this.afkBroadcastReturnMessage = config.getString("afk-broadcast-return-message", "&e%player% &7is no longer AFK.");

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

	public int getAfkKickSeconds() {

		return afkKickSeconds;

	}

	public String getAfkKickMessage() {

		return afkKickMessage;

	}

	public int getAutoclickMinClicks() {

		return autoclickMinClicks;

	}

	public int getAutoclickWindowSeconds() {

		return autoclickWindowSeconds;

	}

	public double getAutoclickCvThreshold() {

		return autoclickCvThreshold;

	}

	public int getAutoclickMaxSameTarget() {

		return autoclickMaxSameTarget;

	}

	public int getNcpFlagWindowSeconds() {

		return ncpFlagWindowSeconds;

	}

	/**
	 * Returns true when going AFK in the given world should be broadcast to
	 * everyone. World names are matched case-insensitively.
	 *
	 * @param worldName The name of the world the player is in.
	 */
	public boolean isBroadcastWorld(String worldName) {

		return worldName != null && afkBroadcastWorlds.contains(worldName.toLowerCase(Locale.ROOT));

	}

	public String getAfkBroadcastMessage() {

		return afkBroadcastMessage;

	}

	public String getAfkBroadcastReturnMessage() {

		return afkBroadcastReturnMessage;

	}

}
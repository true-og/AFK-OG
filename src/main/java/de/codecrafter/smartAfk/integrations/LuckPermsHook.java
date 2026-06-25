/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import de.codecrafter.smartAfk.AFKOG;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;

/**
 * Optional bridge to LuckPerms. When LuckPerms is installed, resolves a player's
 * chat prefix (in their current context) for use in AFK broadcast messages.
 *
 * LuckPerms is a true soft dependency: every method is a no-op returning an empty
 * string when LuckPerms is absent, so the plugin runs identically without it. The
 * LuckPerms API classes are only touched after the presence check, so the JVM
 * never needs them on the classpath when LuckPerms is not installed.
 */
public final class LuckPermsHook {

	private static LuckPerms luckPerms;

	private LuckPermsHook() {

	}

	/**
	 * Caches the LuckPerms API handle if LuckPerms is installed. A no-op otherwise.
	 */
	public static void initialize(AFKOG plugin) {

		if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {

			return;

		}

		try {

			luckPerms = LuckPermsProvider.get();
			plugin.getLogger().info("Hooked into LuckPerms for AFK broadcast prefixes.");

		} catch (final IllegalStateException exception) {

			luckPerms = null;
			plugin.getLogger().warning("LuckPerms is present but its API was unavailable; AFK broadcasts will omit prefixes.");

		}

	}

	/**
	 * Drops the cached API handle so a plugin disable/reload does not retain the
	 * LuckPerms classloader.
	 */
	public static void shutdown() {

		luckPerms = null;

	}

	/**
	 * Returns the player's prefix in their current context, or an empty string when
	 * LuckPerms is absent or the player has no prefix. The returned value may carry
	 * legacy '&'/section color codes set by LuckPerms.
	 *
	 * @param player The player whose prefix to resolve.
	 */
	public static String getPrefix(Player player) {

		if (luckPerms == null) {

			return "";

		}

		final CachedMetaData metaData = luckPerms.getPlayerAdapter(Player.class).getMetaData(player);
		final String prefix = metaData.getPrefix();

		return prefix == null ? "" : prefix;

	}

}

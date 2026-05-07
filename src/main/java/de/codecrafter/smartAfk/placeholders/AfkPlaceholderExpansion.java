/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.placeholders;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.codecrafter.smartAfk.AFKOG;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class AfkPlaceholderExpansion extends PlaceholderExpansion {

	private final AFKOG plugin;

	public AfkPlaceholderExpansion(AFKOG plugin) {

		this.plugin = plugin;

	}

	@Override
	public @NotNull String getIdentifier() {

		return "afkog";

	}

	@Override
	public @NotNull String getAuthor() {

		return String.join(", ", plugin.getPluginMeta().getAuthors());

	}

	@Override
	public @NotNull String getVersion() {

		return plugin.getPluginMeta().getVersion();

	}

	@Override
	public boolean persist() {

		return true;

	}

	@Override
	public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {

		if (!StringUtils.equalsIgnoreCase(params, "status")) {

			return null;

		}

		if (player == null) {

			return "";

		}

		final Player onlinePlayer = player.getPlayer();
		if (onlinePlayer == null) {

			return "";

		}

		return plugin.getAfkManager().isAfk(onlinePlayer) ? "AFK" : "";

	}

}
/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import de.codecrafter.smartAfk.AFKOG;
import de.codecrafter.smartAfk.utils.AfkManager;

public class AfkCommand implements CommandExecutor {

	@Override
	public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

		if (!(commandSender instanceof Player player)) {

			commandSender.sendMessage("This command can only be executed by players.");

			return true;

		}

		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();
		if (afkManager.isAfk(player)) {

			afkManager.updateActivity(player);
			afkManager.unsetAfk(player);

		}
		else {

			afkManager.setAfk(player);

		}

		return true;

	}

}

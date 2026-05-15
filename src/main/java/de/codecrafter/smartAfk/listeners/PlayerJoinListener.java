/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.codecrafter.smartAfk.AFKOG;
import de.codecrafter.smartAfk.utils.AfkManager;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();
		afkManager.initializeSession(event.getPlayer());
		afkManager.clearLegacyInvulnerability(event.getPlayer());

	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {

		AFKOG.getPlugin().getAfkManager().clearSession(event.getPlayer());

	}

}

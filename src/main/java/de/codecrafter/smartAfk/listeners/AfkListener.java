/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.listeners;

import de.codecrafter.smartAfk.AFKOG;
import de.codecrafter.smartAfk.utils.AfkConfig;
import de.codecrafter.smartAfk.utils.AfkManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class AfkListener implements Listener {

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {

		final Player player = event.getPlayer();
		final AFKOG smartAfk = AFKOG.getPlugin();
		final AfkManager afkManager = smartAfk.getAfkManager();
		final AfkConfig afkConfig = smartAfk.getAfkConfig();
		afkManager.updateActivity(player);

		if (event.getFrom().getBlockX() == event.getTo().getBlockX() && event.getFrom().getBlockZ() == event.getTo().getBlockZ() && event.getFrom().getBlockY() == event.getTo().getBlockY()) {

			return;

		}

		if (!afkManager.isAfk(player)) {

			return;

		}

		if (!afkConfig.isFreezeAfkPlayers()) {

			afkManager.unsetAfk(player);

			return;

		}

		final Location from = event.getFrom();
		final Location origTo = event.getTo();
		if (afkConfig.isCancelAfkOnJump() && origTo.getY() >= from.getBlockY() + 1) {

			afkManager.unsetAfk(player);

			return;

		}

		event.setTo(from);

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		final Player player = event.getPlayer();
		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();

		afkManager.updateActivity(player);

		if (afkManager.isAfk(player)) {

			afkManager.unsetAfk(player);

		}

	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

		final Player player = event.getPlayer();
		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();

		afkManager.updateActivity(player);

		if (afkManager.isAfk(player)) {

			afkManager.unsetAfk(player);

		}

	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();
		if (event.getEntity() instanceof Player attackedPlayer) {

			afkManager.updateActivity(attackedPlayer);

			if (afkManager.isAfk(attackedPlayer)) {

				afkManager.unsetAfk(attackedPlayer);

			}

		}

		Player player = getAttacker(event);
		if (player == null) {

			return;

		}

		afkManager.updateActivity(player);

		if (afkManager.isAfk(player)) {

			afkManager.unsetAfk(player);

		}

	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {

		if (!(event.getEntity() instanceof Player player)) {

			return;

		}

		final AFKOG plugin = AFKOG.getPlugin();
		final AfkManager afkManager = plugin.getAfkManager();
		if (!plugin.getAfkConfig().isInvulnerableDuringAfk() || !afkManager.isAfk(player)) {

			return;

		}

		if (event instanceof EntityDamageByEntityEvent damageByEntityEvent && getAttacker(damageByEntityEvent) != null) {

			afkManager.updateActivity(player);
			afkManager.unsetAfk(player);

			return;

		}

		event.setCancelled(true);

	}

	private Player getAttacker(EntityDamageByEntityEvent event) {

		if (event.getDamager() instanceof Player player) {

			return player;

		}

		if (event.getDamager() instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {

			return player;

		}

		return null;

	}

}
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
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
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

		final Location from = event.getFrom();
		final Location to = event.getTo();
		if (from.getYaw() != to.getYaw() || from.getPitch() != to.getPitch()) {

			afkManager.updateLook(player);

		}

		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY()) {

			return;

		}

		if (!afkManager.isAfk(player)) {

			return;

		}

		if (!afkConfig.isFreezeAfkPlayers()) {

			afkManager.unsetAfk(player);

			return;

		}

		if (afkConfig.isCancelAfkOnJump() && to.getY() >= from.getBlockY() + 1) {

			afkManager.unsetAfk(player);

			return;

		}

		event.setTo(from);

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		handleInteractActivity(event.getPlayer());

	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

		handleInteractActivity(event.getPlayer());

	}

	private void handleInteractActivity(Player player) {

		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();

		// only count interactions as activity if the player has moved their look
		// recently. auto-clickers spam clicks without moving the mouse, so we
		// ignore their input and let them go AFK normally.
		if (!afkManager.hasRecentLookChange(player)) {

			return;

		}

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

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityTarget(EntityTargetLivingEntityEvent event) {

		if (!(event.getTarget() instanceof Player player)) {

			return;

		}

		if (!AFKOG.getPlugin().getAfkManager().isAfk(player)) {

			return;

		}

		event.setCancelled(true);
		event.setTarget(null);

		if (event.getEntity() instanceof Mob mob) {

			mob.setTarget(null);

		}

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
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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
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

		final Location from = event.getFrom();
		final Location to = event.getTo();

		// only real positional (block-to-block) movement counts as activity here.
		// look-only changes (mouse jiggle) are ignored, so a mouse-jiggle macro
		// alone cannot defeat the idle timer; walking and water-pushed AFK pools
		// keep players active. interaction-based activity is gated separately by
		// cadence (see creditInteraction).
		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ() && from.getBlockY() == to.getBlockY()) {

			return;

		}

		if (!afkManager.isAfk(player)) {

			afkManager.updateActivity(player);

			return;

		}

		if (!afkConfig.isFreezeAfkPlayers()) {

			afkManager.updateActivity(player);
			afkManager.unsetAfk(player);

			return;

		}

		if (afkConfig.isCancelAfkOnJump() && to.getY() >= from.getBlockY() + 1) {

			afkManager.updateActivity(player);
			afkManager.unsetAfk(player);

			return;

		}

		// frozen AFK player: revert the move and do not credit activity.
		event.setTo(from);

	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {

		// count the main hand only; the off-hand fires a second event per click.
		if (event.getHand() != EquipmentSlot.HAND) {

			return;

		}

		// physical triggers (pressure plates, tripwires) require movement to fire
		// repeatedly, which is already credited as activity; ignore them here.
		// fishing is treated as an ordinary interaction here; auto-fishing is out
		// of scope and handled by a dedicated companion plugin.
		if (event.getAction() == Action.PHYSICAL) {

			return;

		}

		creditInteraction(event.getPlayer(), blockKey(event.getClickedBlock()), true);

	}

	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

		if (event.getHand() != EquipmentSlot.HAND) {

			return;

		}

		creditInteraction(event.getPlayer(), "e:" + event.getRightClicked().getUniqueId(), true);

	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {

		if (event.getWhoClicked() instanceof Player player) {

			// menu use is gated by target (slot) only, not cadence: a real player
			// sorting/trading moves across slots (varied target, always credited),
			// while spamming one slot repeats the target and is withheld.
			creditInteraction(player, "s:" + event.getRawSlot(), false);

		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		creditInteraction(event.getPlayer(), blockKey(event.getBlock()), true);

	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {

		creditInteraction(event.getPlayer(), blockKey(event.getBlock()), true);

	}

	private static String blockKey(org.bukkit.block.Block block) {

		if (block == null) {

			return "air";

		}

		return "b:" + block.getWorld().getName() + ":" + block.getX() + ":" + block.getY() + ":" + block.getZ();

	}

	// credit an interaction as activity unless it looks automated: either the
	// same target is hit repeatedly (auto-click on one button/lever/sign/slot/air)
	// or, when cadence applies, the recent click timing is machine-regular.
	private void creditInteraction(Player player, String targetKey, boolean useCadence) {

		final AfkManager afkManager = AFKOG.getPlugin().getAfkManager();

		boolean automated = afkManager.isRepeatedTarget(player, targetKey);

		if (useCadence) {

			afkManager.recordInteraction(player);
			automated = automated || afkManager.isAutomatedCadence(player);

		}

		if (automated) {

			return;

		}

		afkManager.updateActivity(player);

		if (afkManager.isAfk(player)) {

			afkManager.unsetAfk(player);

		}

	}

	@EventHandler(ignoreCancelled = true)
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

		afkManager.recordInteraction(player);

		// withhold credit when the attack cadence looks automated (grinder
		// auto-clicker) or NoCheatPlus recently flagged the player for attack
		// frequency. being attacked (victim, above) is unaffected.
		if (afkManager.isCombatAutomated(player)) {

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
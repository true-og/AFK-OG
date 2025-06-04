/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import de.codecrafter.smartAfk.SmartAfk;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class AfkManager {
    private final Set<UUID> afkPlayers = new HashSet<>();
    private final Map<UUID, Location> afkPositions = new HashMap<>();
    private static final Component AFK_PREFIX = Component.text("[AFK] ", NamedTextColor.RED);

    public void setAfk(Player player) {
        afkPlayers.add(player.getUniqueId());
        afkPositions.put(player.getUniqueId(), player.getLocation());
        if (SmartAfk.getPlugin().getAfkConfig().isInvulnerableDuringAfk()) player.setInvulnerable(true);

        Component name = player.name();
        player.displayName(AFK_PREFIX.append(name.color(NamedTextColor.WHITE)));
        player.playerListName(AFK_PREFIX.append(name.color(NamedTextColor.WHITE)));

        player.sendMessage(Component.text("Du bist jetzt Afk.", NamedTextColor.GREEN));
    }

    public void unsetAfk(Player player) {
        afkPlayers.remove(player.getUniqueId());
        afkPositions.remove(player.getUniqueId());
        if (SmartAfk.getPlugin().getAfkConfig().isInvulnerableDuringAfk()) player.setInvulnerable(false);

        Component name = player.name();
        player.displayName(name);
        player.playerListName(name);

        player.sendMessage(Component.text("Du bist jetzt nicht mehr Afk.", NamedTextColor.YELLOW));
    }

    public boolean isAfk(Player player) {
        return afkPlayers.contains(player.getUniqueId());
    }

    public Location getAfkPosition(Player player) {
        return afkPositions.get(player.getUniqueId());
    }
}

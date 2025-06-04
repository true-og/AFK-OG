/*
 * This file is part of the Minecraft Smart Afk project.
 * Licensed under the MIT License. See the LICENSE file in the project root for full license information.
 */

package de.codecrafter.smartAfk.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AfkManager {
    private final Set<UUID> afkPlayers = new HashSet<>();
    private static final Component AFK_PREFIX = Component.text("[AFK] ", NamedTextColor.RED);

    public void setAfk(Player player) {
        afkPlayers.add(player.getUniqueId());
        player.setInvulnerable(true);

        Component name = player.name();
        player.displayName(AFK_PREFIX.append(name.color(NamedTextColor.WHITE)));
        player.playerListName(AFK_PREFIX.append(name.color(NamedTextColor.WHITE)));
    }

    public void unsetAfk(Player player) {
        afkPlayers.remove(player.getUniqueId());
        player.setInvulnerable(false);

        Component name = player.name();
        player.displayName(name);
        player.playerListName(name);
    }

    public boolean isAfk(Player player) {
        return afkPlayers.contains(player.getUniqueId());
    }
}

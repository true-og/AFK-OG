# AFK-OG

**AFK-OG** is a lightweight and configurable Minecraft plugin for Purpur-compatible 1.19.4 servers that detects player inactivity.
Players can manually toggle their AFK status, and server admins can configure behaviors like freezing movement, cancelling AFK on jump, or granting invulnerability while a player is AFK.

**Requirements:** Java 17, Purpur/Paper-compatible 1.19.4 server

**Optional dependency:** [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)

**Source:** [https://github.com/IschdeFelin/mc-smart-afk](https://github.com/IschdeFelin/mc-smart-afk)

---

## Features
- Automatic AFK detection based on player inactivity
- Manual AFK toggle using /afk command
- Optionally freeze AFK players to prevent movement
- Optionally make AFK players invulnerable
- Optionally cancel AFK mode when jumping
- Prevents AFK players from interacting with the world
- Adds an `[AFK]` prefix to the player's display name and player list name
- Update checker to inform admins about new releases
- PlaceholderAPI expansion for AFK status output

---

## Commands
### `/afk`
Toggle your AFK status manually.

Useful if you want to go AFK immediately without waiting for the timeout.

This command is player-only.

---

## Installation

1. Build or download the plugin `.jar`.
2. Place it in your server's `/plugins` folder.
3. Install PlaceholderAPI as well if you want placeholder support.
4. Start or restart the server.

---

## Configuration

Edit `config.yml` to adjust AFK behavior to your server’s needs.
Changes require a reload or restart.

Located at: `/plugins/AFK-OG/config.yml`

### Options

| Option                  | Description                                                        | Default |
|-------------------------|--------------------------------------------------------------------|---------|
| afk-timeout-seconds	    | Time of inactivity (in seconds) after which a player is set to AFK | 120     |
| freeze-afk-players      | Prevent AFK players from moving                                    | `true`  |
| cancel-afk-on-jump      | Cancel AFK status if the player jumps                              | `true`  |
| invulnerable-during-afk | Makes players invulnerable while AFK                               | `true`  |

---

## PlaceholderAPI

If PlaceholderAPI is installed, AFK-OG registers the following placeholder:

| Placeholder     | Description                                   |
|----------------|-----------------------------------------------|
| `%afkog_status%` | Returns `AFK` while the player is AFK, otherwise an empty string |

---

## Building

Build the plugin locally with:

```bash
./gradlew build
```

The shaded plugin jar is produced in `build/libs/`.

---

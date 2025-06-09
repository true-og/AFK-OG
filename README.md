# Smart Afk

**SmartAfk** is a lightweight and configurable Minecraft plugin that detects player inactivity.
Players can manually toggle their AFK status, and server admins can configure behaviors like freezing movement or granting invulnerability.

**Download:** [https://modrinth.com/project/smart-afk](https://modrinth.com/project/smart-afk)

---

## Features
- Automatic AFK detection based on player inactivity
- Manual AFK toggle using /afk command
- Optionally freeze AFK players to prevent movement
- Optionally make AFK players invulnerable
- Optionally cancel AFK mode when jumping
- Prevents AFK players from interacting with the world
- Notifies players when they go AFK or return
- Update checker to inform admins about new releases

---

## Commands
### `/afk`
Toggle your AFK status manually.

Useful if you want to go AFK immediately without waiting for the timeout.

---

## Installation

1) Download the `.jar` file from Modrinth.
2) Place it in your server's `/plugins` folder.
3) Restart the server.

---

## Configuration

Edit the config.yml to adjust AFK behavior to your server’s needs.
Changes require a reload or restart.

Located at: `/plugins/SmartAfk/config.yml`

### Options

| Option                  | Description                                                        | Default |
|-------------------------|--------------------------------------------------------------------|---------|
| afk-timeout-seconds	    | Time of inactivity (in seconds) after which a player is set to AFK | 120     |
| freeze-afk-players      | Prevent AFK players from moving                                    | `true`  |
| cancel-afk-on-jump      | Cancel AFK status if the player jumps                              | `true`  |
| invulnerable-during-afk | Makes players invulnerable while AFK                               | `true`  |

---

Have suggestions or found a bug?
Visit my [GitHub repository](https://github.com/IschdeFelin/mc-smart-afk) to open an issue or contribute via pull request!
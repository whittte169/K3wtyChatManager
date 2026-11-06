# K3wtyChatManager

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Platform](https://img.shields.io/badge/platform-Nukkit--MOT-green)
![Version](https://img.shields.io/badge/version-1.0.3-informational)

A chat and client management plugin for **Nukkit-MOT** (Minecraft: Bedrock Edition) servers.

- **Local / global chat** – messages are local by default (radius-based); prefix a message with `!` to send it globally.
- **Fully customizable chat format** with placeholders (rank, title, device, ping, health, position, …).
- **Dynamic name tags** showing device icon, rank, health and ping.
- **Mutes** – temporary, persistent mutes with a reason and the name of the admin who issued them.
- **Anti-caps** – automatically lowercases shouting.
- **Chat cooldowns** – per-rank anti-spam delay between messages.
- **Device & input-mode blocking** – kick mobile / desktop / console players, or players using touch / mouse / gamepad.
- **XUID bans** – timed bans tied to a player's Xbox Live ID, with a reason and the admin's name, so name changes can't bypass the ban. *(timed bans new in 1.0.3)*
- **Device ID bans** – timed bans tied to a player's device ID, with a reason and the admin's name, so name changes can't bypass the ban. *(new in 1.0.3)*
- **Public API** (`ChatManager`, `ClientManager`, `ClientDatabase`) for other plugins.

---

## Table of contents

- [What's new in 1.0.3](#whats-new-in-103)
- [Requirements](#requirements)
- [Installation](#installation)
- [Upgrading from 1.0.2](#upgrading-from-102)
- [For server owners & players](#for-server-owners--players)
  - [Chat system](#chat-system)
  - [Mutes](#mutes)
  - [Anti-caps](#anti-caps)
  - [Chat cooldowns](#chat-cooldowns)
  - [Commands](#commands)
  - [Permissions](#permissions)
  - [Configuration](#configuration)
  - [Placeholders](#placeholders)
  - [Ranks and titles (LuckPerms)](#ranks-and-titles-luckperms)
  - [Bans (XUID & device ID)](#bans-xuid--device-id)
  - [Device & input mode blocking](#device--input-mode-blocking)
  - [Data storage](#data-storage)
- [For developers](#for-developers)
  - [Adding the plugin as a dependency](#adding-the-plugin-as-a-dependency)
  - [API overview](#api-overview)
  - [`ChatManager`](#chatmanager)
  - [`ClientManager` / `ClientDatabase`](#clientmanager--clientdatabase)
  - [Enums](#enums)
  - [Examples](#examples)
  - [Building from source](#building-from-source)
  - [Project structure](#project-structure)
- [Notes](#notes)
- [License](#license)

---

## What's new in 1.0.3

**Features**

- **Device ID bans** – new `/deviceban`, `/devicepardon` (alias `/deviceunban`) and `/getdeviceid` commands. Bans are stored in the database and survive restarts.
- **Timed XUID bans** – `/xuidban` now requires a duration and stores the expiry time, the reason and the admin's name. Expired bans are removed automatically.
- **Ban checks on connect** – the pre-login check now looks at both XUID bans and device ID bans.
- **Live input-mode tracking** – the plugin now reads the current input mode from the player's input packets, so input-mode blocking reacts when a player switches input mode (e.g. touch to gamepad) mid-session.
- New permission: `white.permissions.commands.deviceban`.
- New database table: `banned_players_by_device`.

**Changes**

- `/xuidban` syntax changed from `/xuidban <target> [reason]` to `/xuidban <target> <time> [reason]` (**breaking for scripts and command blocks**).
- The `banned_players_by_xuid` table has new columns: `time`, `reason`, `admin`. **The old database must be deleted** and is recreated automatically, see [Upgrading from 1.0.2](#upgrading-from-102).
- `banPlayerByXUID` has a new signature `(xuid, name, time, reason, admin)` (**API change**, see [ClientDatabase](#clientmanager--clientdatabase)).
- New API methods: `getPlayerXUIDBanInfo`, `banPlayerByDeviceId`, `unBanPlayerByDeviceId`, `isPlayerBannedByDeviceId`, `getPlayerDeviceIdBanInfo` and `ClientManager#getPlayerDeviceId`.
- No new `config.yml` keys.

---

## Requirements

| Requirement | Version | Notes |
|---|---|---|
| Java | 17+ | |
| [Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT) | `1.26.50-R1` (build target) | Plugin API `1.0.0` |
| [LuckPerms](https://luckperms.net/) | 5.5+ (Nukkit build) | **Required** – ranks and titles are read from LuckPerms prefixes/suffixes. Chat formatting calls `LuckPermsProvider.get()`, so LuckPerms must be installed and loaded. |

SQLite JDBC (`3.46.1.0`) is bundled (shaded) into the plugin jar — nothing else to install.

## Installation

1. Install **LuckPerms** on your Nukkit-MOT server.
2. Download `k3wtychatmanager-1.0.3.jar` from the [Releases](https://github.com/whittte169/K3wtyChatManager/releases) page and put it into the server's `plugins/` folder.
3. Start the server. The plugin will generate `plugins/K3wtyChatManager/config.yml`.
4. Edit the config to your liking and restart the server.

On every start the plugin also extracts and registers a bundled **font resource pack** (`font_pack.zip`) that provides the device icons used in name tags / chat. Players will be asked to download the pack when joining.

## Upgrading from 1.0.2

1. Stop the server and replace the old jar with `k3wtychatmanager-1.0.3.jar`.
2. **Delete the old database.** 1.0.3 stores an expiry time, a reason and the admin's name for every ban, so the database format has changed and the old file is not compatible. This step is **mandatory**: stop the server, delete `plugins/K3wtyChatManager/database.db`, then start the server – the plugin automatically creates a new database with the correct tables.

   > **Warning:** Deleting the database also removes all existing **bans and mutes**. If you need them, write them down first (e.g. with `sqlite3 plugins/K3wtyChatManager/database.db "SELECT * FROM banned_players_by_xuid;"`) and re-issue them with `/xuidban`, `/deviceban` and `/mute` after the update.

   If you keep the old database, `/xuidban` and XUID ban checks will fail with a database error.
3. Update any scripts, command blocks or automation that call `/xuidban` – it now needs a duration (e.g. `/xuidban Steve 7d griefing`).
4. Plugins that call `banPlayerByXUID` must be updated to the new signature (see [For developers](#for-developers)).
5. `config.yml` is unchanged – no new keys are required.
6. Grant `white.permissions.commands.deviceban` to staff who should use the device ID commands (it defaults to `op`).

Upgrading from 1.0.1 or older? Follow the 1.0.2 notes in [CHANGELOG.md](CHANGELOG.md) first (new config keys and the `muted_players` table).

---

## For server owners & players

### Chat system

When `enable_chat_dividing` is `true` (default), chat is split into two channels:

| Channel | How to use | Who receives it |
|---|---|---|
| **Local** `[L]` | Just type your message | Players in the **same world** within `local_chat_radius` blocks (default `25`) |
| **Global** `[G]` | Start your message with the global symbol, e.g. `!Hello everyone` | **All** online players |

Notes:

- The global symbol is removed from the message before it is displayed.
- Empty messages (after removing the symbol) are ignored.
- Local messages are also written to the server console log.
- Players **without** the `white.permissions.chat.color` permission have colour/formatting codes (`&a`, `§c`, …) stripped from their messages.
- If `enable_chat_dividing` is `false`, the plugin does not touch chat and the server default is used. In that mode **anti-caps and chat cooldowns are not applied either**, but mutes still are.

Order in which a message is processed: mute check → cooldown → anti-caps → colour handling → channel detection → delivery.

### Mutes

Muted players cannot send chat messages. Mutes:

- last for the duration you specify (`w`eeks, `d`ays, `h`ours, `m`inutes, `s`econds — combine freely, e.g. `1w2d`, `5h30m`, `90s`);
- are saved in the database, so they persist across restarts and relogging;
- expire automatically; an expired mute is removed the next time it is checked;
- are applied by **player name**;
- work regardless of `enable_chat_dividing`.

When a player is muted, they receive a message with the reason, duration and the admin's name, and the mute is broadcast to the whole server. A muted player who tries to chat sees `You're muted!`.

> Both `/mute` and `/unmute` currently only work for **online** players.

### Anti-caps

When `enable_anticaps` is `true` (default), any message containing **three or more consecutive capital letters** (A–Z or А–Я/Ё) is converted to lowercase entirely. For example `HELLO everyone` becomes `hello everyone`, while `Hi, I'm OK` is left untouched.

Players with `white.permissions.chat.caps` are exempt.

### Chat cooldowns

When `enable_chat_cooldowns` is `true` (default), players must wait between messages. A player who writes too early gets `Wait Ns to send message!` and the message is not sent.

- `chat_cooldowns.default` is the delay in seconds for everyone (default `3`).
- Any other key under `chat_cooldowns` is a **rank override**. The key must be exactly equal to the player's LuckPerms **prefix** string (the same value `%rank%` shows, in its raw form, including `&` colour codes). Quote keys that contain special YAML characters:

  ```yaml
  chat_cooldowns:
    default: 3
    "&c[Admin]": 0   # note: 0 is ignored, see below
    "&6[VIP]": 1
  ```

- An override of `0` or less is ignored (the default is used). To remove the cooldown for a group entirely, give it the `white.permissions.chat.spam` permission instead.

Players with `white.permissions.chat.spam` are exempt.

### Commands

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/xuidban <target> <time> [reason]` | – | Bans an **online** player by their XUID for `<time>` and kicks them. The ban is stored in the database, so it persists across restarts. | `white.permissions.commands.xuidban` |
| `/xuidpardon <name>` | `/xuidunban` | Removes a ban created by `/xuidban`. Takes the **player name** that was used when banning (not the XUID). | `white.permissions.commands.xuidban` |
| `/getxuid <target>` | – | Prints the XUID of an **online** player. | `white.permissions.commands.xuidban` |
| `/deviceban <target> <time> [reason]` | – | Bans an **online** player by their device ID for `<time>` and kicks them. *(new in 1.0.3)* | `white.permissions.commands.deviceban` |
| `/devicepardon <name>` | `/deviceunban` | Removes a ban created by `/deviceban`. Takes the **player name** that was used when banning. *(new in 1.0.3)* | `white.permissions.commands.deviceban` |
| `/getdeviceid <target>` | – | Prints the device ID of an **online** player. *(new in 1.0.3)* | `white.permissions.commands.deviceban` |
| `/mute <target> <time> [reason]` | – | Mutes an **online** player for `<time>` (e.g. `30m`, `2d`, `1h30m`). | `white.permissions.commands.mute` |
| `/unmute <target>` | – | Removes a mute from an **online** player. | `white.permissions.commands.mute` |

Time format for `/mute`, `/xuidban` and `/deviceban`: one or more `<number><unit>` groups, no spaces. Units: `w` = week, `d` = day, `h` = hour, `m` = minute, `s` = second. The total must be greater than zero.

Examples:

```text
/getxuid Steve
> Steve's XUID is 2535412345678901

/xuidban Steve 7d griefing
> Steve was banned

/xuidpardon Steve
> Steve was unbanned

/getdeviceid Steve
> Steve's device ID is 5f1c9a3e-0000-0000-0000-000000000000

/deviceban Steve 12h ban evasion
> Steve was banned

/devicepardon Steve
> Steve was unbanned

/mute Steve 2h30m spamming
> Steve was muted

/unmute Steve
> Steve was unmuted
```

> Commands can be run by players and from the console. Commands are also registered with the fallback prefix `k3wtychatmanager` (e.g. `/k3wtychatmanager:mute`).

### Permissions

| Permission | Default | Description |
|---|---|---|
| `white.permissions.commands.xuidban` | `op` | Use `/xuidban`, `/xuidpardon` and `/getxuid`. |
| `white.permissions.commands.deviceban` | `op` | Use `/deviceban`, `/devicepardon` and `/getdeviceid`. *(new in 1.0.3)* |
| `white.permissions.commands.mute` | `op` | Use `/mute` and `/unmute`. |
| `white.permissions.chat.color` | `op` | Use colour codes in chat messages. |
| `white.permissions.chat.caps` | `op` | Bypass anti-caps. |
| `white.permissions.chat.spam` | `op` | Bypass chat cooldowns. |

Grant them with LuckPerms, e.g.:

```text
/lp group moderator permission set white.permissions.commands.xuidban true
/lp group moderator permission set white.permissions.commands.deviceban true
/lp group moderator permission set white.permissions.commands.mute true
/lp group vip permission set white.permissions.chat.color true
/lp group vip permission set white.permissions.chat.spam true
```

### Configuration

`plugins/K3wtyChatManager/config.yml` (defaults shown):

```yaml
# ---- Device blocking ----
enable_device_blocking: false
blocked_devices:
  mobile: false      # Android, iOS, Windows Phone
  desktop: false     # Windows, Windows x86, macOS, Linux, Fire
  console: false     # PlayStation, Xbox, Switch
  other: false       # Dedicated, HoloLens, Gear VR, tvOS
  undefined: false   # Unknown device

# ---- Input mode blocking ----
enable_input_mode_blocking: false
blocked_input_modes:
  mouse: false
  touch: false
  game_pad: false
  motion_controller: false
  count: false

kick_reason:
  device: "You cannot use this device on this server!"
  input_mode: "You cannot use this input mode on this server!"

# ---- Chat ----
enable_chat_dividing: true
local_chat_radius: 25
global_chat_symbol: "!"

chat_display:
  local: "&6[L]"
  global: "&c[G]"
  undefined: "&7[U]"

message_format: "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"

# ---- Name tags ----
enable_name_tag_change: true
name_tag_format: "%device% &7| &r[%rank%&r] %player% %title%\n  &c%health% HP &7| &r%ping% ms"

# ---- Anti-caps ----
enable_anticaps: true

# ---- Chat cooldowns ----
enable_chat_cooldowns: true
chat_cooldowns:
  default: 3         # seconds, applies to everyone
  vip: 1             # rank override: key = LuckPerms prefix of the player
```

| Option | Type | Description |
|---|---|---|
| `enable_device_blocking` | boolean | Master switch for device blocking. |
| `blocked_devices.*` | boolean | Set a group to `true` to kick players on that device group when they connect. |
| `enable_input_mode_blocking` | boolean | Master switch for input-mode blocking. |
| `blocked_input_modes.*` | boolean | Set a mode to `true` to kick players using it. |
| `kick_reason.device` / `kick_reason.input_mode` | string | Message shown to a kicked player. |
| `enable_chat_dividing` | boolean | Enable the local/global chat system and custom format (also required for anti-caps and cooldowns). |
| `local_chat_radius` | number | Radius (in blocks) of local chat. |
| `global_chat_symbol` | string | Prefix that makes a message global. May be more than one character. |
| `chat_display.*` | string | Text for the `%chat_type%` placeholder per channel. Supports `&` colour codes. |
| `message_format` | string | Template for chat messages. See [Placeholders](#placeholders). |
| `enable_name_tag_change` | boolean | Enable custom name tags (refreshed every second). |
| `name_tag_format` | string | Template for name tags (use `\n` for a new line). |
| `enable_anticaps` | boolean | Lowercase messages with 3+ consecutive capital letters. |
| `enable_chat_cooldowns` | boolean | Enable the delay between messages. |
| `chat_cooldowns.default` | number (seconds) | Default cooldown. |
| `chat_cooldowns.<rank>` | number (seconds) | Cooldown for players whose LuckPerms prefix equals `<rank>`. Values ≤ 0 are ignored. |

Colour codes use `&` (e.g. `&6` = gold, `&l` = bold, `&r` = reset).

Config changes are applied after a server restart.

### Placeholders

Placeholders are written between percent signs and can be used in `message_format` and `name_tag_format`. Unknown placeholders are left as-is.

| Placeholder | Available in | Description | Example |
|---|---|---|---|
| `%player%` | chat, name tag | Player name | `Steve` |
| `%message%` | chat only | The message text | `Hello!` |
| `%chat_type%` | chat only | Channel label (`[L]`, `[G]`, `[U]`) | `[L]` |
| `%rank%` | both | LuckPerms **prefix** | `&4Admin` |
| `%title%` | both | LuckPerms **suffix** | `&7the Builder` |
| `%device%` | both | Device icon glyph (from the font pack) for mobile / desktop / console; `Other` / `Undefined` as text | custom glyph in-game |
| `%input_mode%` | both | `Mouse and keyboard` / `Game pad`; `Undefined` for any other mode | `Game pad` |
| `%health%` | both | Current health | `20.0` |
| `%ping%` | both | Ping in ms | `42` |
| `%gamemode%` | both | `Survival`, `Creative`, `Adventure`, `Spectator` | `Survival` |
| `%dimension%` | both | `Overworld`, `Nether`, `The End` | `Overworld` |
| `%x%` `%y%` `%z%` | both | Block coordinates | `120` |
| `%position%` | both | Formatted position | `D: Overworld, X: 120, Y: 64, Z: -45` |

### Ranks and titles (LuckPerms)

- **Rank** = the player's LuckPerms **prefix** (falls back to the prefix of their primary group).
- **Title** = the player's LuckPerms **suffix** (falls back to the suffix of their primary group).

```text
/lp group admin meta setprefix 100 "&c[Admin]"
/lp user Steve meta setsuffix 10 "&7the Builder"
```

The rank is also what the [chat cooldown](#chat-cooldowns) overrides are matched against.

### Bans (XUID & device ID)

Both ban types work the same way:

- A ban lasts for the duration you specify (same time format as `/mute`, e.g. `7d`, `1w2d`, `5h30m`). The duration is required.
- The ban stores the player's name, the expiry time, the reason and the admin's name in the database, so it persists across restarts.
- Expired bans are removed automatically the next time they are checked.
- The target must be **online** when you ban them. The player is kicked and the ban is broadcast to the whole server.
- Banned players are rejected at login with `You're banned!`.
- `/xuidpardon` and `/devicepardon` take the **player name** that was used when banning.

| | XUID ban | Device ID ban |
|---|---|---|
| Identifier | Xbox Live ID (`/getxuid`) | Device ID sent by the client (`/getdeviceid`) |
| Ban / unban | `/xuidban`, `/xuidpardon` | `/deviceban`, `/devicepardon` |
| Table | `banned_players_by_xuid` | `banned_players_by_device` |

> The XUID is empty for players who are not authenticated with Xbox Live (e.g. when `xbox-auth` is disabled), so XUID bans are only meaningful on authenticated servers.

### Device & input mode blocking

- **Device blocking** is checked when a player connects (pre-login), right after the ban checks.
- **Input-mode blocking** is checked when a player connects **and then once per second** while they are online. From 1.0.3 the current mode is taken from the player's input packets, so a player who switches to a blocked input mode mid-session is kicked.

Typical use cases:

- Make a **PC-only** server: set `enable_device_blocking: true` and `mobile: true`, `console: true`.
- Block **touch controls** on a competitive server: set `enable_input_mode_blocking: true` and `touch: true`.

Device groups:

| Group | Operating systems |
|---|---|
| `mobile` | Android, iOS, Windows Phone |
| `desktop` | Windows, Windows x86, macOS, Linux, Fire |
| `console` | PlayStation, Xbox, Nintendo Switch |
| `other` | Dedicated, HoloLens, Gear VR, tvOS |
| `undefined` | Everything else |

### Data storage

Bans and mutes are stored in an SQLite database at:

```text
plugins/K3wtyChatManager/database.db
```

| Table | Columns | Purpose |
|---|---|---|
| `banned_players_by_xuid` | `xuid TEXT PRIMARY KEY`, `name TEXT`, `time INTEGER`, `reason TEXT`, `admin TEXT` | XUID bans. `time` is the **expiry** as a Unix timestamp in milliseconds. |
| `banned_players_by_device` | `deviceId TEXT PRIMARY KEY`, `name TEXT`, `time INTEGER`, `reason TEXT`, `admin TEXT` | Device ID bans. `time` is the **expiry** as a Unix timestamp in milliseconds. *(new in 1.0.3)* |
| `muted_players` | `name TEXT PRIMARY KEY`, `time INTEGER`, `reason TEXT`, `admin TEXT` | Mutes. `time` is the **expiry** as a Unix timestamp in milliseconds. |

Tables are created automatically if they do not exist, but existing tables are **not** altered, so a database from 1.0.2 must be deleted when upgrading – see [Upgrading from 1.0.2](#upgrading-from-102). Back this file up if you want to keep your ban and mute lists.

---

## For developers

### Adding the plugin as a dependency

Add the plugin to your `plugin.yml` so it loads before yours:

```yaml
depend: ["K3wtyChatManager"]
```

Then add the jar to your build. With Maven (local jar, `provided` scope):

```xml
<dependency>
  <groupId>com.white</groupId>
  <artifactId>k3wtychatmanager</artifactId>
  <version>1.0.3</version>
  <scope>provided</scope>
</dependency>
```

Install it into your local repository first:

```bash
mvn install:install-file -Dfile=k3wtychatmanager-1.0.3.jar \
  -DgroupId=com.white -DartifactId=k3wtychatmanager -Dversion=1.0.3 -Dpackaging=jar
```

Or build it yourself (`mvn clean install`) — see [Building from source](#building-from-source).

### API overview

| Class | Package | Purpose |
|---|---|---|
| `ChatManager` | `com.white.k3wtychatmanager.api.chat` | Placeholder formatting, chat-type detection, rank/title lookup, display settings. Singleton. |
| `ChatType` | `com.white.k3wtychatmanager.api.chat` | Enum: `UNDEFINED`, `LOCAL`, `GLOBAL`. |
| `ClientManager` | `com.white.k3wtychatmanager.api.client` | Device OS, input mode, XUID and device ID helpers. Singleton, implements `ClientDatabase`. |
| `ClientDatabase` | `com.white.k3wtychatmanager.api.client` | SQLite-backed storage for XUID bans, device ID bans and mutes (interface with default methods). |
| `DeviceOS` | `com.white.k3wtychatmanager.api.client` | Enum of Bedrock device operating systems. |

Both managers are singletons:

```java
ChatManager chat = ChatManager.getInstance();
ClientManager client = ClientManager.getInstance();
```

### `ChatManager`

| Method | Description |
|---|---|
| `static ChatManager getInstance()` | Get the singleton instance. |
| `String formatMessage(String template, Player player, ChatType type, String message)` | Replaces all placeholders (including `%chat_type%` and `%message%`) and applies `&` colours. |
| `String formatName(String template, Player player)` | Same as above but for name tags (no `%chat_type%` / `%message%`). |
| `String getPlayerRank(UUID uuid)` | LuckPerms prefix (empty string if none). |
| `String getPlayerTitle(UUID uuid)` | LuckPerms suffix (empty string if none). |
| `String getPlayerHealth(Player player)` | Health as string. |
| `String getPlayerGameMode(Player player)` | `Survival` / `Creative` / `Adventure` / `Spectator` / `Undefined`. |
| `String[] getPlayerPosition(Player player, Boolean formatted)` | `formatted = false` → `{dimension, x, y, z}`; `true` → single-element array with `D: …, X: …, Y: …, Z: …`. |
| `ChatType getMessageChatType(String message)` | `GLOBAL` if the message starts with the global symbol, otherwise `LOCAL`. |
| `String removeGlobalChatSymbolFromMessage(String message)` | Strips the global symbol prefix. |
| `String getGlobalChatSymbol()` / `void setGlobalChatSymbol(String symbol)` | Get/set the global chat symbol. |
| `String getChatTypeDisplay(ChatType type)` / `void setChatTypeDisplay(ChatType type, String display)` | Get/set the `%chat_type%` label. |
| `String getDeviceOsDisplay(DeviceOS os)` / `void setDeviceOsDisplay(DeviceOS os, String display)` | Get/set the `%device%` label per OS. |
| `String getInputModeDisplay(InputMode mode)` / `void setInputModeDisplay(InputMode mode, String display)` | Get/set the `%input_mode%` label. |

### `ClientManager` / `ClientDatabase`

`ClientManager` implements `ClientDatabase`, so all database methods are available directly on `ClientManager.getInstance()`.

**Client information**

| Method | Description |
|---|---|
| `static ClientManager getInstance()` | Get the singleton instance. |
| `DeviceOS getPlayerDeviceOs(Player player)` | The player's device OS (`UNDEFINED` for `null`). |
| `InputMode getPlayerInputMode(Player player)` | The player's input mode as reported at login (Nukkit's `InputMode`). The live mode used for input-mode blocking is tracked internally and is not exposed by this method. |
| `String getPlayerXUID(Player player)` | The player's XUID (empty string for `null`; empty for offline-mode/non-authenticated players). |
| `String getPlayerDeviceId(Player player)` | The player's device ID (empty string for `null`). *(new in 1.0.3)* |

**Database**

| Method | Description |
|---|---|
| `void loadDatabaseFolder(String folder)` | Opens `database.db` in the given folder and creates the tables (called by the plugin on load). |
| `Connection getConnection()` | The underlying SQLite connection. |
| `boolean isDatabaseOpened()` | Whether the connection is open. |
| `void closeDatabase(Boolean force)` | Closes the connection when `force == true`. |

**XUID bans** *(`banPlayerByXUID` signature changed in 1.0.3)*

| Method | Description |
|---|---|
| `void banPlayerByXUID(String xuid, String name, int time, String reason, String admin) throws SQLException` | Ban `xuid` for `time` **seconds** and remember `name`. Replaces an existing ban. Does nothing if `name` is `null` or `time < 0`. `null` reason → `"No reason"`, `null` admin → `"Console"`. |
| `void unBanPlayerByXUID(String xuid, String name) throws SQLException` | Remove a ban. If `xuid` is non-null it is used; otherwise the ban is looked up by `name`. Pass `null` for the one you don't have. |
| `boolean isPlayerBannedByXUID(String xuid, String name) throws SQLException` | Check whether an active ban exists. If `xuid` is non-null and non-empty the lookup is done **by XUID**, otherwise by `name`. Expired bans are deleted and return `false`. |
| `String[] getPlayerXUIDBanInfo(String xuid, String name) throws SQLException` | `{xuid, name, remainingSeconds, reason, admin}` for an active ban, or a single-element array (`{name}` / `{xuid}`) if there is none. Lookup by `xuid` if it is non-null and non-empty, otherwise by `name`. *(new in 1.0.3)* |

**Device ID bans** *(new in 1.0.3)*

| Method | Description |
|---|---|
| `void banPlayerByDeviceId(String deviceId, String name, int time, String reason, String admin) throws SQLException` | Ban `deviceId` for `time` **seconds** and remember `name`. Replaces an existing ban. Does nothing if `name` is `null` or `time < 0`. |
| `void unBanPlayerByDeviceId(String deviceId, String name) throws SQLException` | Remove a ban. If `deviceId` is non-null it is used; otherwise the ban is looked up by `name`. |
| `boolean isPlayerBannedByDeviceId(String deviceId, String name) throws SQLException` | Check whether an active ban exists. If `deviceId` is non-null and non-empty the lookup is done by device ID, otherwise by `name`. Expired bans are deleted and return `false`. |
| `String[] getPlayerDeviceIdBanInfo(String deviceId, String name) throws SQLException` | `{deviceId, name, remainingSeconds, reason, admin}` for an active ban, or a single-element array if there is none. Lookup by `deviceId` if it is non-null and non-empty, otherwise by `name`. |

**Mutes**

| Method | Description |
|---|---|
| `void mutePlayer(String name, int time, String reason, String admin) throws SQLException` | Mute `name` for `time` **seconds**. Replaces an existing mute. `null` reason → `"No reason"`, `null` admin → `"Console"`. |
| `void unMutePlayer(String name) throws SQLException` | Remove the mute. |
| `boolean isPlayerMuted(String name) throws SQLException` | `true` if the player has an active mute. Expired mutes are deleted and return `false`. |
| `String[] getPlayerMuteInfo(String name) throws SQLException` | `{name, remainingSeconds, reason, admin}` for an active mute, or `{name}` if the player isn't muted. |

> Player names are compared **case-sensitively** in the mute and name-based ban queries. Use `player.getName()`.

### Enums

```java
public enum ChatType { UNDEFINED, LOCAL, GLOBAL }

public enum DeviceOS {
    UNDEFINED, ANDROID, IOS, MACOS, FIRE, GEAR_VR, HOLOLENS, WINDOWS,
    WINDOWS_X86, DEDICATED, TVOS, PLAYSTATION, SWITCH, XBOX, WINDOWS_PHONE, LINUX
}
```

Both enums expose `getOrdinal()` and `fromOrdinal(int)`.

### Examples

**Format a custom message using the plugin's placeholders**

```java
ChatManager chat = ChatManager.getInstance();

String line = chat.formatMessage(
    "%chat_type% &7| [%rank%&r] %player% &8-> &r%message%",
    player,
    ChatType.GLOBAL,
    "Server restart in 5 minutes!"
);
getServer().broadcastMessage(line);
```

**Custom scoreboard / HUD text**

```java
String hud = ChatManager.getInstance().formatName(
    "%player% &7| &a%health% HP &7| %position%", player);
player.sendTip(hud);
```

**Change labels at runtime**

```java
ChatManager chat = ChatManager.getInstance();
chat.setGlobalChatSymbol("@");
chat.setChatTypeDisplay(ChatType.GLOBAL, "&b[World]");
chat.setDeviceOsDisplay(DeviceOS.ANDROID, "&aAndroid");
```

**Ban / check a player by XUID from your own plugin**

```java
ClientManager client = ClientManager.getInstance();
String xuid = client.getPlayerXUID(player);

try {
    // ban for 7 days (604800 seconds)
    client.banPlayerByXUID(xuid, player.getName(), 604800, "Griefing", "AutoMod");
    if (client.isPlayerBannedByXUID(xuid, player.getName())) {
        player.kick("You have been banned.");
    }
    // later: unban by name only
    client.unBanPlayerByXUID(null, player.getName());
} catch (SQLException e) {
    getLogger().error("Database error: " + e.getMessage());
}
```

**Ban a player's device for 1 hour**

```java
ClientManager client = ClientManager.getInstance();
String deviceId = client.getPlayerDeviceId(player);

try {
    client.banPlayerByDeviceId(deviceId, player.getName(), 3600, "Ban evasion", "AutoMod");
    String[] info = client.getPlayerDeviceIdBanInfo(deviceId, null);
    // info = {deviceId, name, secondsLeft, reason, admin}
} catch (SQLException e) {
    getLogger().error("Database error: " + e.getMessage());
}
```

**Mute a player for 10 minutes and read the mute info**

```java
ClientManager client = ClientManager.getInstance();

try {
    client.mutePlayer(player.getName(), 600, "Advertising", "AutoMod");

    if (client.isPlayerMuted(player.getName())) {
        String[] info = client.getPlayerMuteInfo(player.getName());
        // info[0] = name, info[1] = seconds left, info[2] = reason, info[3] = admin
        player.sendMessage("Muted for " + info[1] + "s: " + info[2]);
    }

    client.unMutePlayer(player.getName());
} catch (SQLException e) {
    getLogger().error("Database error: " + e.getMessage());
}
```

**Restrict a feature to desktop players**

```java
DeviceOS os = ClientManager.getInstance().getPlayerDeviceOs(player);
boolean isMobile = os == DeviceOS.ANDROID || os == DeviceOS.IOS || os == DeviceOS.WINDOWS_PHONE;
if (isMobile) {
    player.sendMessage("This feature is not available on mobile.");
}
```

### Building from source

Requirements: JDK 17+, Maven 3.8+.

```bash
git clone https://github.com/whittte169/K3wtyChatManager.git
cd K3wtyChatManager
mvn clean package
```

The shaded jar is produced at `target/k3wtychatmanager-1.0.3.jar` (use this one, not `original-*.jar`).

Dependencies are resolved from `https://repo.lanink.cn/repository/maven-public/` (Nukkit-MOT) and Maven Central (LuckPerms API, SQLite JDBC). Nukkit-MOT and LuckPerms are `provided`; SQLite JDBC is shaded.

### Project structure

```text
src/main/
├── java/com/white/k3wtychatmanager/
│   ├── Main.java                  # Plugin entry point
│   ├── ChatManagerHandler.java    # Chat event handling: mutes, cooldowns, anti-caps, local/global chat, name tags
│   ├── ClientManagerHandler.java  # Pre-login checks: XUID/device ID bans, device/input blocking; live input-mode tracking
│   ├── CommandManager.java        # /xuidban, /xuidpardon, /getxuid, /deviceban, /devicepardon, /getdeviceid, /mute, /unmute
│   ├── ResourcePackManager.java   # Registers the bundled font pack
│   └── api/
│       ├── chat/   ChatManager, ChatType
│       └── client/ ClientManager, ClientDatabase, DeviceOS
└── resources/
    ├── plugin.yml
    ├── config.yml
    └── font_pack.zip              # Device icon glyphs
```

---

## Notes

- **LuckPerms is required for some features.** Without it, ranks and titles (`%rank%`, `%title%`) are unavailable and chat formatting that relies on them will not work. Rank-based chat cooldowns also depend on it.

## License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for details.

Author: [whittte169](https://github.com/whittte169)

Repository: [github.com/whittte169/K3wtyChatManager](https://github.com/whittte169/K3wtyChatManager)
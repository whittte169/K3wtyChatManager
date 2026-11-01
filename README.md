# K3wtyChatManager

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 17](https://img.shields.io/badge/Java-17-orange)
![Platform](https://img.shields.io/badge/platform-Nukkit--MOT-green)
![Version](https://img.shields.io/badge/version-1.0.1-informational)

A chat and client management plugin for **Nukkit-MOT** (Minecraft: Bedrock Edition) servers.

- **Local / global chat** – messages are local by default (radius-based); prefix a message with `!` to send it globally.
- **Fully customizable chat format** with placeholders (rank, title, device, ping, health, position, …).
- **Dynamic name tags** showing device icon, rank, health and ping.
- **Device & input-mode blocking** – kick mobile / desktop / console players, or players using touch / mouse / gamepad.
- **XUID bans** – ban players by their Xbox Live ID so name changes can't bypass the ban.
- **Public API** (`ChatManager`, `ClientManager`, `ClientDatabase`) for other plugins.

---

## Table of contents

- [Requirements](#requirements)
- [Installation](#installation)
- [For server owners & players](#for-server-owners--players)
  - [Chat system](#chat-system)
  - [Commands](#commands)
  - [Permissions](#permissions)
  - [Configuration](#configuration)
  - [Placeholders](#placeholders)
  - [Ranks and titles (LuckPerms)](#ranks-and-titles-luckperms)
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

## Requirements

| Requirement | Version | Notes |
|---|---|---|
| Java | 17+ | |
| [Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT) | `1.26.50-R1` (build target) | Plugin API `1.0.0` |
| [LuckPerms](https://luckperms.net/) | 5.5+ (Nukkit build) | **Required** – ranks and titles are read from LuckPerms prefixes/suffixes. Chat formatting calls `LuckPermsProvider.get()`, so LuckPerms must be installed and loaded. |

SQLite JDBC is bundled (shaded) into the plugin jar — nothing else to install.

## Installation

1. Install **LuckPerms** on your Nukkit-MOT server.
2. Download `k3wtychatmanager-<version>.jar` from the [Releases](https://github.com/whittte169/K3wtyChatManager/releases) page and put it into the server's `plugins/` folder.
3. Start the server. The plugin will generate `plugins/K3wtyChatManager/config.yml`.
4. Edit the config to your liking and restart the server.

On every start the plugin also extracts and registers a bundled **font resource pack** (`font_pack.zip`) that provides the device icons used in name tags / chat. Players will be asked to download the pack when joining.

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
- If `enable_chat_dividing` is `false`, the plugin does not touch chat and the server default is used.

### Commands

| Command | Aliases | Description | Permission |
|---|---|---|---|
| `/xuidban <target> [reason]` | – | Bans an **online** player by their XUID and kicks them. The ban is stored in the database, so it persists across restarts and name changes. | `white.permissions.commands.xuidban` |
| `/xuidpardon <xuid>` | `/xuidunban` | Removes a XUID from the ban list. Takes the **XUID** (not the player name). | `white.permissions.commands.xuidban` |
| `/getxuid <target>` | – | Prints the XUID of an **online** player. Use it to find the value for `/xuidpardon`. | `white.permissions.commands.xuidban` |

Examples:

```text
/getxuid Steve
> Steve's XUID is 2535412345678901

/xuidban Steve griefing
> Steve was banned by XUID

/xuidpardon 2535412345678901
> 2535412345678901 was unbanned by XUID
```

> Commands can be run by players and from the console. Commands are also registered with the fallback prefix `k3wtychatmanager` (e.g. `/k3wtychatmanager:xuidban`).

### Permissions

| Permission | Default | Description |
|---|---|---|
| `white.permissions.commands.xuidban` | `op` | Use `/xuidban`, `/xuidpardon` and `/getxuid`. |
| `white.permissions.chat.color` | `op` | Use colour codes in chat messages. |

Grant them with LuckPerms, e.g.:

```text
/lp group moderator permission set white.permissions.commands.xuidban true
/lp group vip permission set white.permissions.chat.color true
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
```

| Option | Type | Description |
|---|---|---|
| `enable_device_blocking` | boolean | Master switch for device blocking. |
| `blocked_devices.*` | boolean | Set a group to `true` to kick players on that device group when they connect. |
| `enable_input_mode_blocking` | boolean | Master switch for input-mode blocking. |
| `blocked_input_modes.*` | boolean | Set a mode to `true` to kick players using it. |
| `kick_reason.device` / `kick_reason.input_mode` | string | Message shown to a kicked player. |
| `enable_chat_dividing` | boolean | Enable the local/global chat system and custom format. |
| `local_chat_radius` | number | Radius (in blocks) of local chat. |
| `global_chat_symbol` | string | Prefix that makes a message global. May be more than one character. |
| `chat_display.*` | string | Text for the `%chat_type%` placeholder per channel. Supports `&` colour codes. |
| `message_format` | string | Template for chat messages. See [Placeholders](#placeholders). |
| `enable_name_tag_change` | boolean | Enable custom name tags. |
| `name_tag_format` | string | Template for name tags (use `\n` for a new line). |

Colour codes use `&` (e.g. `&6` = gold, `&l` = bold, `&r` = reset).

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
| `%input_mode%` | both | `Mouse and keyboard` / `Game pad` | `Game pad` |
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

### Device & input mode blocking

Checks are performed when a player connects (pre-login). Typical use cases:

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

XUID bans are stored in an SQLite database at:

```text
plugins/K3wtyChatManager/database.db
```

Table: `banned_players_by_xuid (xuid TEXT PRIMARY KEY)`. Back this file up if you want to keep your ban list.

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
  <version>1.0.1</version>
  <scope>provided</scope>
</dependency>
```

Install it into your local repository first:

```bash
mvn install:install-file -Dfile=k3wtychatmanager-1.0.1.jar \
  -DgroupId=com.white -DartifactId=k3wtychatmanager -Dversion=1.0.1 -Dpackaging=jar
```

Or build it yourself (`mvn clean install`) — see [Building from source](#building-from-source).

### API overview

| Class | Package | Purpose |
|---|---|---|
| `ChatManager` | `com.white.k3wtychatmanager.api.chat` | Placeholder formatting, chat-type detection, rank/title lookup, display settings. Singleton. |
| `ChatType` | `com.white.k3wtychatmanager.api.chat` | Enum: `UNDEFINED`, `LOCAL`, `GLOBAL`. |
| `ClientManager` | `com.white.k3wtychatmanager.api.client` | Device OS, input mode, XUID and XUID-ban helpers. Singleton, implements `ClientDatabase`. |
| `ClientDatabase` | `com.white.k3wtychatmanager.api.client` | SQLite-backed XUID ban storage (interface with default methods). |
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

| Method | Description |
|---|---|
| `static ClientManager getInstance()` | Get the singleton instance. |
| `DeviceOS getPlayerDeviceOs(Player player)` | The player's device OS (`UNDEFINED` for `null`). |
| `InputMode getPlayerInputMode(Player player)` | The player's current input mode (Nukkit's `InputMode`). |
| `String getPlayerXUID(Player player)` | The player's XUID (empty string for `null`; empty for offline-mode/non-authenticated players). |
| `void loadDatabaseFolder(String folder)` | Opens `database.db` in the given folder (called by the plugin on load). |
| `Connection getConnection()` | The underlying SQLite connection. |
| `boolean isDatabaseOpened()` | Whether the connection is open. |
| `void banPlayerByXUID(String xuid) throws SQLException` | Add a XUID to the ban list (idempotent). |
| `void unBanPlayerByXUID(String xuid) throws SQLException` | Remove a XUID from the ban list. |
| `boolean isPlayerBannedByXUID(String xuid) throws SQLException` | Check whether a XUID is banned. |
| `void closeDatabase(Boolean force)` | Closes the connection when `force == true`. |

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
    client.banPlayerByXUID(xuid);
    if (client.isPlayerBannedByXUID(xuid)) {
        player.kick("You have been banned.");
    }
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

The shaded jar is produced at `target/k3wtychatmanager-1.0.1.jar` (use this one, not `original-*.jar`).

Dependencies are resolved from `https://repo.lanink.cn/repository/maven-public/` (Nukkit-MOT) and Maven Central (LuckPerms API, SQLite JDBC). Nukkit-MOT and LuckPerms are `provided`; SQLite JDBC is shaded.

### Project structure

```text
src/main/
├── java/com/white/k3wtychatmanager/
│   ├── Main.java                  # Plugin entry point
│   ├── ChatManagerHandler.java    # Chat event handling, name tags
│   ├── ClientManagerHandler.java  # Join checks: XUID bans, device/input blocking
│   ├── CommandManager.java        # /xuidban, /xuidpardon, /getxuid
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

- **LuckPerms is required for some features.** Without it, ranks and titles (`%rank%`, `%title%`) are unavailable and chat formatting that relies on them will not work.

## License

Distributed under the **MIT License**. See [LICENSE](LICENSE) for details.

Author: [whittte169](https://github.com/whittte169)

Repository: [github.com/whittte169/K3wtyChatManager](https://github.com/whittte169/K3wtyChatManager)

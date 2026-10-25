# ChatManagerSystem

A plugin for **Nukkit-MOT** that adds the following to your Minecraft Bedrock server:

- **device blocking** (mobile, PC, console);
- **input mode blocking** (keyboard and mouse, touch input, gamepad);
- **punishments** for using banned devices (kick, ban, IP ban, XUID ban);
- **customizable NameTag** (the name above a player's head) with privilege, device, ping, and health;
- **customizable chat format** with a LuckPerms prefix;
- **local and global chat**.

This guide covers configuration through `config.yml` only.

---

## Table of contents

1. [Requirements and installation](#requirements-and-installation)
2. [Configuration file](#configuration-file)
3. [Device blocking](#device-blocking)
4. [Input mode blocking](#input-mode-blocking)
5. [Punishments and messages](#punishments-and-messages)
6. [Exception list](#exception-list-allow_players)
7. [NameTag (name above the head)](#nametag-name-above-the-head)
8. [Chat message format](#chat-message-format)
9. [Local and global chat](#local-and-global-chat)
10. [Colors and formatting](#colors-and-formatting)
11. [Important notes](#important-notes)

---

## Requirements and installation

| Requirement | Version |
|---|---|
| Server | Nukkit-MOT |
| Java | 17 or newer |
| [LuckPerms](https://luckperms.net/) | Needed for `%privilege%` (see below) |

1. Put `ChatManagerSystem.jar` into the server's `plugins/` folder.
2. Install LuckPerms (if it is not installed yet) and set up group prefixes.
3. Start the server: the plugin creates `plugins/ChatManagerSystem/config.yml` by itself.
4. Edit the config and **restart the server**.

> ⚠️ **LuckPerms is effectively required.** The plugin does not declare it as an optional dependency and calls it when building the name tag and chat messages. Without LuckPerms, errors will appear in the console while `nametag_enabled` / `displayname_enabled` are on.

> ⚠️ **There is no config reload command.** Restart the server after any change to `config.yml`.

---

## Configuration file

Location: `plugins/ChatManagerSystem/config.yml`

Default values:

```yaml
enable_device_blocking: false
blocked_devices:
  mobile: false
  pc: false
  console: false

enable_inputmode_blocking: false
blocked_inputmodes:
  mouse_and_keyboard: false
  touch_input: false
  gamepad: false

punishment: "kick"
punishment_reason_for_device: "§cYou cannot join this server from this device!"
punishment_reason_for_im: "§cYou cannot use this input device on this server!"
allow_players:
  - "NicknameExample"

nametag_enabled: true
nametag_format: "[%privilege%§r] %player% §7| §r%device%\n  §c%health% HP §7|§r %ping%"

displayname_enabled: true
displayname_format: "%chat% [%privilege%§r] %player% §7> §r%message%"

local_chat_enabled: true
local_chat_radius: 25
global_chat_symbol: "!"
```

---

## Device blocking

Lets you forbid joining from certain types of devices.

### 1. Enable the check

```yaml
enable_device_blocking: false
```

Change `false` to `true`.

### 2. Choose the banned device types

```yaml
blocked_devices:
  mobile: false
  pc: false
  console: false
```

Set a type to `true` to ban it.

| Option | Devices included |
|---|---|
| `mobile` | Android, iOS, Windows Phone |
| `pc` | Windows 10/11, macOS, FireOS |
| `console` | PlayStation, Xbox, Nintendo Switch |

Devices the plugin cannot identify (shown as `Unknown?` in the name tag) are **not blocked by any of these options**.

**Example:** a server for PC and console players only, no mobile players:

```yaml
enable_device_blocking: true
blocked_devices:
  mobile: true
  pc: false
  console: false
```

---

## Input mode blocking

Lets you forbid players from using a certain input method.

### 1. Enable the check

```yaml
enable_inputmode_blocking: false
```

Change `false` to `true`.

### 2. Choose the banned input modes

```yaml
blocked_inputmodes:
  mouse_and_keyboard: false
  touch_input: false
  gamepad: false
```

| Option | Description |
|---|---|
| `mouse_and_keyboard` | Keyboard and mouse |
| `touch_input` | Touch controls (phones, tablets) |
| `gamepad` | Gamepad (DualSense, DualShock, Xbox controller, etc.) |

The plugin recognizes the "VR Controller" mode and shows it through `%inputmode%`, but it cannot be blocked in the config.

**Example:** forbid playing with a gamepad:

```yaml
enable_inputmode_blocking: true
blocked_inputmodes:
  mouse_and_keyboard: false
  touch_input: false
  gamepad: true
```

---

## Punishments and messages

### Punishment type

```yaml
punishment: "kick"
```

One value for **both** checks (device and input mode).

| Value | What happens |
|---|---|
| `"kick"` | The player is disconnected but can rejoin (default). |
| `"ban"` | The player is banned **by nickname**. They can join again only after being pardoned. |
| `"ipban"` | Ban by IP address. The player cannot join with another nickname or account from the same IP. |
| `"xuidban"` | Ban by XUID (the unique ID of an Xbox account). Changing the nickname does not help. |

> ⚠️ The `xuidban` list is kept **in memory only** and is cleared when the server restarts.

> ⚠️ The `punishment` value must be written exactly as in the table (lowercase). With any other value, no punishment is applied.

### Reason messages

```yaml
punishment_reason_for_device: "§cYou cannot join this server from this device!"
punishment_reason_for_im: "§cYou cannot use this input device on this server!"
```

| Option | When it is shown |
|---|---|
| `punishment_reason_for_device` | The player is punished for a banned **device** |
| `punishment_reason_for_im` | The player is punished for a banned **input mode** |

You can use color codes (`§c`, `§l`, etc.). See [Colors and formatting](#colors-and-formatting).

> ⚠️ In version 1.0.0, when a player is punished for input mode, the text from `punishment_reason_for_device` is shown, and `punishment_reason_for_im` is not used. If you enable input mode blocking, set a suitable text in `punishment_reason_for_device` or use a general wording.

---

## Exception list (`allow_players`)

Nicknames of players who are **not** punished for a banned device:

```yaml
allow_players:
  - "NicknameExample"
  - "AnotherPlayer"
```

Each nickname goes on its own line starting with `-`. Case matters: the nickname must match exactly.

> ⚠️ In version 1.0.0 the exception list applies only to **device blocking**. It does not apply to input mode blocking.

---

## NameTag (name above the head)

The NameTag is the text above a player's head. It is updated once per second.

### Enabling and disabling

```yaml
nametag_enabled: true
```

`true` — the plugin replaces the name above the head, `false` — the standard name is kept.

### Format

```yaml
nametag_format: "[%privilege%§r] %player% §7| §r%device%\n  §c%health% HP §7|§r %ping%"
```

`\n` is a line break (works inside double quotes).

| Placeholder | Value |
|---|---|
| `%player%` | Player's nickname |
| `%privilege%` | Group prefix from LuckPerms |
| `%device%` | Player's device (Android, iOS, Windows 10/11…) |
| `%inputmode%` | Input mode (Keyboard and mouse, Touch input, Gamepad…) |
| `%ping%` | Ping, for example `45ms` |
| `%health%` | Health (whole number) |

Result with the default settings:

```
[Admin] Steve | Windows 10/11
  20 HP | 45ms
```

---

## Chat message format

### Enabling and disabling

```yaml
displayname_enabled: true
```

`true` — the plugin formats chat messages itself and enables the local/global chat split. `false` — the plugin does not touch the chat and the server's standard format is used.

### Format

```yaml
displayname_format: "%chat% [%privilege%§r] %player% §7> §r%message%"
```

| Placeholder | Value |
|---|---|
| `%player%` | Player's nickname |
| `%privilege%` | Group prefix from LuckPerms |
| `%device%` | Player's device |
| `%ping%` | Player's ping |
| `%chat%` | Chat type: **L** (local) or **G** (global) |
| `%message%` | Message text |

> `%inputmode%` and `%health%` are **not supported** in the chat format. They will be printed as is, including the percent signs.

Example message with the default settings:

```
L [Admin] Steve > hello everyone
G [Admin] Steve > hello whole server
```

Unknown placeholders are left in the text unchanged.

### Where the prefix comes from

`%privilege%` is the prefix of the player's **primary group** in LuckPerms. You can set it with this command:

```
/lp group <group> meta setprefix 100 "&c[Admin]"
```

If the group has no prefix, `%privilege%` is empty (with the default format you will get empty brackets `[]`).

---

## Local and global chat

Works while `displayname_enabled` is on.

```yaml
local_chat_enabled: true
local_chat_radius: 25
global_chat_symbol: "!"
```

| Option | Description |
|---|---|
| `local_chat_radius` | Local chat radius in blocks (whole number) |
| `global_chat_symbol` | Symbol at the start of a message that makes it global |
| `local_chat_enabled` | Reserved. Has no effect in version 1.0.0 |

**How it works:**

- A message **without** the symbol is local. Only players in the same world within `local_chat_radius` blocks can see it (the area is a cube around the player).
- A message starting with `!` is global. Every player sees it, and the symbol is removed from the text.

Examples (radius 25):

| Player types | Who sees it |
|---|---|
| `hello` | Players within 25 blocks |
| `!hello everyone` | The whole server |

> ⚠️ It is recommended to keep `global_chat_symbol: "!"`. With a different symbol, it will not be removed from the text of a global message.
>
> ⚠️ Do not use the `!` character in `displayname_format` before `%message%`.

---

## Colors and formatting

Texts in the config (NameTag, chat format, punishment reasons) use Minecraft formatting codes with the `§` symbol.

| Code | Color | Code | Color |
|---|---|---|---|
| `§0` | Black | `§8` | Dark gray |
| `§1` | Dark blue | `§9` | Blue |
| `§2` | Dark green | `§a` | Green |
| `§3` | Dark aqua | `§b` | Aqua |
| `§4` | Dark red | `§c` | Red |
| `§5` | Purple | `§d` | Pink |
| `§6` | Gold | `§e` | Yellow |
| `§7` | Gray | `§f` | White |

Also: `§l` — bold, `§o` — italic, `§r` — reset formatting.

Save `config.yml` in **UTF-8** encoding and keep strings containing `§` in quotes.

---

## Important notes

- A **server restart** is required after changing the config.
- The device and input mode checks run **when a player joins** the server.
- If both checks are enabled and the player's device is not banned, the input mode check may not run for that player in version 1.0.0. If you only need input mode blocking, keep `enable_device_blocking: false`.
- Before enabling `ban`, `ipban`, or `xuidban`, test the settings with a separate test account so you do not block the wrong players. `kick` has no lasting consequences: the player can simply join from another device.

---

## Author

whittte169 · License: see the `LICENSE` file

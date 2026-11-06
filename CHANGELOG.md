# Changelog

All notable changes to **K3wtyChatManager** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.3] - 2026-11-06

### Added

- **Device ID bans**
  - `/deviceban <target> <time> [reason]` bans an online player by device ID and kicks them.
  - `/devicepardon <name>` (alias `/deviceunban`) removes a device ID ban by player name.
  - `/getdeviceid <target>` prints the device ID of an online player.
  - New permission `white.permissions.commands.deviceban` (default: `op`) for the three commands above.
  - New database table `banned_players_by_device` (`deviceId`, `name`, `time`, `reason`, `admin`).
  - Device ID bans are checked on connect together with XUID bans; banned players are rejected with `You're banned!`.
- **API** (`ClientManager` / `ClientDatabase`):
  - `ClientManager#getPlayerDeviceId(Player)`
  - `banPlayerByDeviceId(deviceId, name, time, reason, admin)`
  - `unBanPlayerByDeviceId(deviceId, name)`
  - `isPlayerBannedByDeviceId(deviceId, name)`
  - `getPlayerDeviceIdBanInfo(deviceId, name)`
  - `getPlayerXUIDBanInfo(xuid, name)`
- **Live input-mode tracking** – the plugin now listens to `PlayerAuthInputPacket` and keeps the player's current input mode, which is used by the once-per-second input-mode blocking check (falls back to the mode reported at login). Tracked modes are cleaned up when the player quits.

### Changed

- **Timed XUID bans** – `/xuidban` now takes a required duration: `/xuidban <target> <time> [reason]` (units `w`, `d`, `h`, `m`, `s`, e.g. `7d`, `1h30m`). The expiry time, reason and admin name are stored; expired bans are removed automatically when checked. The kick screen and server broadcast now include the reason, duration and admin.
- The `banned_players_by_xuid` table now has the columns `xuid`, `name`, `time`, `reason`, `admin`.
- The `banPlayerByXUID` method signature changed from `(xuid, name)` to `(xuid, name, time, reason, admin)`.
- `isPlayerBannedByXUID` now also handles expiry (expired bans are deleted and reported as not banned). Ban lookups use the XUID / device ID when it is available and fall back to the player name otherwise.
- Version bumped to `1.0.3` in `pom.xml` and `plugin.yml`.

### Breaking changes

- `/xuidban` requires a duration; scripts and command blocks that use the old syntax must be updated.
- `banPlayerByXUID(String, String)` no longer exists; API consumers must switch to the five-argument version and recompile.

### Upgrade notes

- **The old database must be deleted** when upgrading from 1.0.2. The `banned_players_by_xuid` table has new columns and the plugin does not alter existing tables. Stop the server, delete `plugins/K3wtyChatManager/database.db` and start the server again – a new database is created automatically. Note that this also removes existing bans and mutes, so re-issue them if you still need them.
- `config.yml` is unchanged; no new keys are required.
- Grant `white.permissions.commands.deviceban` to staff who need the device ID commands.

## [1.0.2]

### Added

- **Mute system** – `/mute <target> <time> [reason]` and `/unmute <target>`. Mutes are temporary, stored in the database (`muted_players` table) and survive restarts.
- **Anti-caps** – messages with 3 or more consecutive capital letters (Latin and Cyrillic) are lowercased (`enable_anticaps`).
- **Chat cooldowns** – configurable delay between messages with per-rank overrides (`enable_chat_cooldowns`, `chat_cooldowns`).
- **Continuous input-mode blocking** – the input mode is checked on connect and then once per second.
- XUID bans remember the player's **name**.
- New permissions: `white.permissions.commands.mute`, `white.permissions.chat.caps`, `white.permissions.chat.spam`.

### Changed

- `/xuidpardon` takes a **player name** instead of an XUID.
- `banPlayerByXUID`, `unBanPlayerByXUID` and `isPlayerBannedByXUID` take both `xuid` and `name` (API change).
- New config keys: `enable_anticaps`, `enable_chat_cooldowns`, `chat_cooldowns`.

> Entries for versions 1.0.1 and 1.0.0 are not included here.

[1.0.3]: https://github.com/whittte169/K3wtyChatManager/releases/tag/nukkit-1.0.3
[1.0.2]: https://github.com/whittte169/K3wtyChatManager/releases/tag/nukkit-1.0.2
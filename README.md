#### ChatManagerSystem Nukkit-MOT

### Config guide

## How to block device
ChatManagerSystem adding to your Minecraft-Server a player's device check system.
If player using a device, what banned, he will get punishment.
1. You must enable a player's device check system.
Find a section in config:
```yaml
enable_device_blocking: false # will the plugin block access from the specified devices
```
Default setting for this section is false, to enable it, you must replace false to true.
2. You must set settings for device what you want block in list of banned devices
Find a section in config:
```yaml
blocked_devices:
  mobile: false
  pc: false
  console: false
```
mobile - it is a mobile OS types, iOS, Android (if you using a Linux OS, plugin will show what you playing from Andrioid) and else
pc - it is a personal computer OS types, Windows, FireOS, macOS and else
console - it is a game consoles OS types, PlayStation, Xbox, Nintendo Switch
To block the device what you want block, you must replace false to true after : after device type.

## How to block input mode device
Also ChatManagerSystem adding to your Minecraft-Server a player's input mode device check system
1. How in device block system, you must enable a inputmode check system.
Find a section in config
```yaml
enable_inputmode_blocking: false # will the plugin block input devices: keyboard and mouse, touch input, gamepad
```
Default setting for this section is also false, to enable it, you must replace false to true
2. You must set settings for input mode what you want block in list of banned input modes
Find a section in config
```yaml
blocked_inputmodes:
  mouse_and_keyboard: false
  touch_input: false
  gamepad: false
```
mouse_and_keyboard - plugin will show this group also if player using just one of elements(mouse or keyboard)
touch_input - using in phones and pads
gamepad - gamepad from game console, DualSense, DualShock and else
To block the input mode what you want block, you also must replace false to true after : after input mode.

## How to appoint punishment
You can appoint a punishment for using a banned device or input mode.
Also you can set reason message to punishment.
Find a config section:
```yaml
punishment: "kick" # punishment for using the device or input device ("ban", "kick", "ipban", "xuidban")
```
Default setting for this section is kick, this punishment just a kicking a player from server.
Punishment types:
"kick" - kicking player from server, after this player is still can try log on server.
"ban" - banning player, after this player won't have a permission to log on server untill he will get a pardon.
"ipban" - banning player by IP-Address, if player will try to log on server using a another device or another nickname, won't log on server and will be kicked.
"xuidban" - banning player by XUID, XUID is a unique id of player xbox account, if player changes nickname, he will kicked.
Just replace "kick" to punishment what you want from list
## Changing a punishment reason message
You also can change a message with what player will get punishment.
Find a section from config:
```yaml
punishment_reason_for_device: "§cYou cannot join this server from this device!" # reason for ban/kick for device
punishment_reason_for_im: "§cYou cannot use this input device on this server!" # reason for ban/kick for input device
```
punishment_reason_for_device is a message with what player will get punishment for using banned device
punishment_reason_for_im is a message with what player will get punishment for using banned input mode
Change a message after : after one of reasons.

## Allow players list
Also you can add a nicknames of players what won't get a punishment for using a banned device or input mode.
Find a section in config:
```yaml
allow_players: # Nicknames of players permitted to log in from banned devices and use banned input modes
  - "NicknameExample"
```
Just add a nickname for an example, set - and "Nickname" inside section.

## How to change a NameTag and DisplayName in chat format
ChatManagerSystem adding to your server a real chat manager.
But you must add a plugin LuckPerms to your server first if you want to show players privilege.

# NameTag:
NameTag is a floating text above player's head with his name.
In config you can change a floating text for player.
1. Enable a NameTag changing(not necessary, default setting for this section is true)
Find a section in config:
```yaml
nametag_enabled: true # will the player name above the head be replaced
```
Default setting for this section is true, and this value is a enabling a NameTag changing, if you want disable this system, replace true to false.
2. Edit a NameTag format how you want
You also can change NameTag format.
Find a config section:
```yaml
nametag_format: "[%privilege%§r] %player% §7| §r%device%\n  §c%health% HP §7|§r %ping%"
```
Player data what you can get from list:
%player% - players name
%privilege% - players privilege
%ping% - players ping
%device% - players device OS
%inputmode% - players input mode
%health% - players health
Just change a string after nametag_format: how you want.

# DisplayName
DisplayName is a how players name shows in chat, but in config you can how will shows all message.
1. Enable a DisplayName changing(not necessary, default setting for this section is true)
Find a section in config:
```yaml
displayname_enabled: true # whether the player's name will be replaced in the chat
```
Default setting for this section is true, and this value is a enabling a DisplayName changing, if you want disable this system, replace true to false.
2. Edit a DisplayName format how you want
Find a config section:
```yaml
displayname_format: "%chat% [%privilege%§r] %player% §7> §r%message%"
```
Player data what you can get from list:
%player% - players name
%privilege% - players privilege
%ping% - players ping
%device% - players device OS
%chat% - chat type, global or local
%message% - message, what was sended in chat
Just change a string after displayname_format: how you want.

## Local and global chats
Chat manager also adding to your server a local and global chat system.
1. Enable a local chat system(not necessary, default setting for this section is true)
Find a section in config:
```yaml
local_chat_enabled: true
```
Default setting for this section is true, and this value is a enabling a local chat system, if you want disable this system, replace true to false.
You also can set a value for local chat radius, find a section in config:
```yaml
local_chat_radius: 25
```
Remove a 25 and set a value in blocks.
You can set a symbol with message must starts to message will show in global chat.
Find a section in config:
```yaml
global_chat_symbol: "!" # symbol before a player's message to send it to global chat
```
Replace "!" to symbol what you want to message starts with.

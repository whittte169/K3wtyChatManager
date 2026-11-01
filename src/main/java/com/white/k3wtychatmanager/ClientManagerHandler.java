package com.white.k3wtychatmanager;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.plugin.Plugin;

import java.util.Arrays;

import com.white.k3wtychatmanager.api.client.ClientManager;
import com.white.k3wtychatmanager.api.client.DeviceOS;

import cn.nukkit.Player;
import cn.nukkit.utils.Config;
import cn.nukkit.network.protocol.types.InputMode;

/**
 * 
 * ClientManagerHandler interface
 * used to implement the core mechanics of the plugin
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * @version 1.0.0
 * 
 */
public interface ClientManagerHandler {
    Plugin getPlugin();

    @EventHandler
    default void onJoin(PlayerPreLoginEvent event) {
        ClientManager clientManager = ClientManager.getInstance();
        Player player = event.getPlayer();
        String XUID = clientManager.getPlayerXUID(player);
        try {
            if (clientManager.isPlayerBannedByXUID(XUID)) {
                player.kick("You're banned!");
                return;
            }
        } catch (Exception e) {
            getPlugin().getLogger().error(e.getMessage());
        }
        DeviceOS[] mobileDeviceOS = new DeviceOS[]{DeviceOS.ANDROID, DeviceOS.IOS, DeviceOS.WINDOWS_PHONE};
        DeviceOS[] desktopDeviceOS = new DeviceOS[]{DeviceOS.WINDOWS, DeviceOS.WINDOWS_X86, DeviceOS.MACOS, DeviceOS.LINUX, DeviceOS.FIRE};
        DeviceOS[] consoleDeviceOS = new DeviceOS[]{DeviceOS.PLAYSTATION, DeviceOS.XBOX, DeviceOS.SWITCH};
        DeviceOS[] otherDeviceOS = new DeviceOS[]{DeviceOS.DEDICATED, DeviceOS.HOLOLENS, DeviceOS.GEAR_VR, DeviceOS.TVOS};
        DeviceOS playerDeviceOS = clientManager.getPlayerDeviceOs(player);
        String playerDeviceGroup;
        if (Arrays.asList(mobileDeviceOS).contains(playerDeviceOS)) {
            playerDeviceGroup = "Mobile";
        }
        else if (Arrays.asList(desktopDeviceOS).contains(playerDeviceOS)) {
            playerDeviceGroup = "Desktop";
        }
        else if (Arrays.asList(consoleDeviceOS).contains(playerDeviceOS)) {
            playerDeviceGroup = "Console";
        }
        else if (Arrays.asList(otherDeviceOS).contains(playerDeviceOS)) {
            playerDeviceGroup = "Other";
        }
        else {
            playerDeviceGroup = "Undefined";
        }
        Config config = getPlugin().getConfig();
        if (config.getBoolean("enable_device_blocking", false)) {
            String kickReason = config.getString("kick_reason.device", "You cannot use this device on this server!");
            if (config.getBoolean("blocked_devices.mobile", false) && playerDeviceGroup.equals("Mobile")) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_devices.desktop", false) && playerDeviceGroup.equals("Desktop")) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_devices.console", false) && playerDeviceGroup.equals("Console")) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_devices.other", false) && playerDeviceGroup.equals("Other")) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_devices.undefined", false) && playerDeviceGroup.equals("Undefined")) {
                player.kick(kickReason);
                return;
            }
        }
        InputMode playerInputMode = clientManager.getPlayerInputMode(player);
        if (config.getBoolean("enable_input_mode_blocking", false)) {
            String kickReason = config.getString("kick_reason.input_mode", "You cannot use this input mode on this server!");
            if (config.getBoolean("blocked_input_modes.mouse", false) && playerInputMode.equals(InputMode.MOUSE)) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_input_modes.touch", false) && playerInputMode.equals(InputMode.TOUCH)) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_input_modes.game_pad", false) && playerInputMode.equals(InputMode.GAME_PAD)) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_input_modes.motion_controller", false) && playerInputMode.equals(InputMode.MOTION_CONTROLLER)) {
                player.kick(kickReason);
                return;
            }
            if (config.getBoolean("blocked_input_modes.count", false) && playerInputMode.equals(InputMode.COUNT)) {
                player.kick(kickReason);
                return;
            }
        }
    }
    
    default void checkInputMode() {
        Config config = getPlugin().getConfig();
        ClientManager clientManager = ClientManager.getInstance();
        getPlugin().getServer().getScheduler().scheduleRepeatingTask(getPlugin(), () -> {
            for (Player player : getPlugin().getServer().getOnlinePlayers().values()) {
                InputMode playerInputMode = clientManager.getPlayerInputMode(player);
                if (config.getBoolean("enable_input_mode_blocking", false)) {
                    String kickReason = config.getString("kick_reason.input_mode", "You cannot use this input mode on this server!");
                    if (config.getBoolean("blocked_input_modes.mouse", false) && playerInputMode.equals(InputMode.MOUSE)) {
                        player.kick(kickReason);
                        return;
                    }
                    if (config.getBoolean("blocked_input_modes.touch", false) && playerInputMode.equals(InputMode.TOUCH)) {
                        player.kick(kickReason);
                        return;
                    }
                    if (config.getBoolean("blocked_input_modes.game_pad", false) && playerInputMode.equals(InputMode.GAME_PAD)) {
                        player.kick(kickReason);
                        return;
                    }
                    if (config.getBoolean("blocked_input_modes.motion_controller", false) && playerInputMode.equals(InputMode.MOTION_CONTROLLER)) {
                        player.kick(kickReason);
                        return;
                    }
                    if (config.getBoolean("blocked_input_modes.count", false) && playerInputMode.equals(InputMode.COUNT)) {
                        player.kick(kickReason);
                        return;
                    }
                }
            }
        }, 20);
    }
}

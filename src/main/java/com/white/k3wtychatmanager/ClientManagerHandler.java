package com.white.k3wtychatmanager;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerPreLoginEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.event.server.DataPacketReceiveEvent;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlayerAuthInputPacket;
import cn.nukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
 * @version 1.0.2
 * 
 */
public interface ClientManagerHandler {
    Map<UUID, InputMode> currentInputModes = new ConcurrentHashMap<>();
    Plugin getPlugin();

    @EventHandler
    default void onJoin(PlayerPreLoginEvent event) {
        ClientManager clientManager = ClientManager.getInstance();
        Player player = event.getPlayer();
        String XUID = clientManager.getPlayerXUID(player);
        String deviceId = clientManager.getPlayerDeviceId(player);
        try {
            if (clientManager.isPlayerBannedByXUID(XUID, player.getName()) || clientManager.isPlayerBannedByDeviceId(deviceId, player.getName())) {
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
            if (isInputModeBlocked(config, playerInputMode)) {
                player.kick(kickReason);
            }
        }
    }
    
    @EventHandler
    default void onInputPacket(DataPacketReceiveEvent event) {
        DataPacket packet = event.getPacket();
        if (!(packet instanceof PlayerAuthInputPacket)) return;
        InputMode mode = ((PlayerAuthInputPacket) packet).getInputMode();
        if (mode == null) return;
        currentInputModes.put(event.getPlayer().getUniqueId(), mode);
    }

    @EventHandler
    default void onInputQuit(PlayerQuitEvent event) {
        currentInputModes.remove(event.getPlayer().getUniqueId());
    }

    default boolean isInputModeBlocked(Config config, InputMode mode) {
        if (mode == null) return false;
        switch (mode) {
            case MOUSE: return config.getBoolean("blocked_input_modes.mouse", false);
            case TOUCH: return config.getBoolean("blocked_input_modes.touch", false);
            case GAME_PAD: return config.getBoolean("blocked_input_modes.game_pad", false);
            case MOTION_CONTROLLER: return config.getBoolean("blocked_input_modes.motion_controller", false);
            case COUNT: return config.getBoolean("blocked_input_modes.count", false);
            default: return false;
        }
    }

    default void checkInputMode() {
        ClientManager clientManager = ClientManager.getInstance();
        getPlugin().getServer().getScheduler().scheduleRepeatingTask(getPlugin(), () -> {
            Config config = getPlugin().getConfig();
            if (!config.getBoolean("enable_input_mode_blocking", false)) return;
            String kickReason = config.getString("kick_reason.input_mode", "You cannot use this input mode on this server!");
            for (Player player : getPlugin().getServer().getOnlinePlayers().values()) {
                InputMode mode = currentInputModes.get(player.getUniqueId());
                if (mode == null) mode = clientManager.getPlayerInputMode(player);
                if (isInputModeBlocked(config, mode)) {
                    player.kick(kickReason);
                }
            }
        }, 20);
    }
}

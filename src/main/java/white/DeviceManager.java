package white;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerLoginEvent;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * The DeviceManager class
 * Library for method with Player
 * also a task handler involving the Player's device
 * @author whittte169
 * @version 1.0.0
 * 
 */
public class DeviceManager implements Listener {
    private Config config;
    private List<String> XUIDBans = new ArrayList<>();

    public DeviceManager(Config config) {
        this.config = config;
    }

    public String[] getPlayerDeviceOSAndIM(Player player) {
        int deviceId = player.getLoginChainData().getDeviceOS();
        int imId = player.getLoginChainData().getCurrentInputMode();
        String device = switch (deviceId) {
            case 1 -> "Android";
            case 2 -> "iOS";
            case 3 -> "macOS";
            case 4 -> "FireOS";
            case 7 -> "Windows 10/11";
            case 10 -> "PlayStation";
            case 11 -> "Nintendo Switch";
            case 12 -> "Xbox";
            case 13 -> "Windows Phone";
            default -> "Unknown?";
        };
        String inputMode = switch (imId) {
            case 1 -> "Keyboard and mouse";
            case 2 -> "Touch input";
            case 3 -> "Gamepad";
            case 4 -> "VR Controller";
            default -> "Unknown?";
        };
        String[] result = new String[]{device, inputMode};
        return result;
    }

    public String getPlayerPing(Player player) {
        return player.getPing() + "ms";
    }

    public String getPlayerXUID(Player player) {
        return player.getLoginChainData().getXUID();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        String[] playerData = getPlayerDeviceOSAndIM(player);
        String playerDevice = playerData[0];
        String playerInputMode = playerData[1];
        if (checkPlayerXUID(getPlayerXUID(player))) {
            player.kick("You are banned!");
            event.setCancelled(true);
            return;
        }
        if (config.getBoolean("enable_device_blocking", false)) {
            if (!checkPlayerDevice(playerDevice)) {
                return;
            }
            List<String> whiteList = config.getStringList("allow_players");
            if (whiteList.contains(player.getName())) {
                return;
            }
            if (config.getString("punishment", "kick").equals("kick")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("kick")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot join this server from this device!"));
                event.setCancelled(true);
            }
            if (config.getString("punishment", "kick").equals("ban")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("ban")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot join this server from this device!"));
                if (player.isBanned()) {
                    return;
                }
                player.setBanned(true);
                event.setCancelled(true);
            }
            if (config.getString("punishment", "kick").equals("ipban")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("ipban")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot join this server from this device!"));
                Server.getInstance().getIPBans().addBan(player.getAddress(), config.getString("punishment_reason_for_device", "§cYou cannot join this server from this device!"), null, "Server");
                event.setCancelled(true);
            }
            if (config.getString("punishment", "kick").equals("xuidban")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to log in while using banned device was %s!", player.getName(), returnLogString("xuidban")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot join this server from this device!"));
                XUIDBans.add(getPlayerXUID(player));
                event.setCancelled(true);
            }
        }
        if (config.getBoolean("enable_inputmode_blocking", false)) {
            if (!checkPlayerInputMode(playerInputMode)) {
                return;
            }
            if (config.getString("punishment", "kick").equals("kick")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("kick")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot use this input device on this server!"));
                event.setCancelled(true);
            }
            if (config.getString("punishment", "kick").equals("ban")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("ban")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot use this input device on this server!"));
                if (player.isBanned()) {
                    return;
                }
                player.setBanned(true);
                event.setCancelled(true);
            }
            if (config.getString("punishment", "kick").equals("ipban")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("ipban")));
                player.kick(config. getString("punishment_reason_for_device", "§cYou cannot use this input device on this server!"));
                Server.getInstance().getIPBans().addBan(player.getAddress(), config.getString("punishment_reason_for_device", "§cYou cannot use this input device on this server!"), null, "Server");
                event.setCancelled(true);
            }
            if (config.getString("punishment", "kick").equals("xuidban")) {
                Main.getInstance().getLogger().info(String.format("§c%s tried to use banned input device and was %s!", player.getName(), returnLogString("xuidban")));
                player.kick(config.getString("punishment_reason_for_device", "§cYou cannot use this input device on this server!"));
                XUIDBans.add(getPlayerXUID(player));
                event.setCancelled(true);
            }
        }
    }

    private String returnLogString(String punishment) {
        if (punishment.equals("kick")) {
            return "was kicked";
        }
        if (punishment.equals("ban")) {
            return "was banned";
        }
        if (punishment.equals("ipban")) {
            return "was IP-banned";
        }
        if (punishment.equals("xuidban")) {
            return "was XUID-banned";
        }
        return "";
    }

    public boolean checkPlayerXUID(String xuid) {
        if (XUIDBans.contains(xuid)) {
            return true;
        }
        return false;
    }

    public boolean isPlayerBannedByXUID(Player player) {
        if (XUIDBans.contains(getPlayerXUID(player))) {
            return true;
        }
        return false;
    }

    public boolean checkPlayerDevice(String device) {
        ConfigSection blockedDevices = config.getSection("blocked_devices");
        String group = getDeviceGroup(device);
        if (blockedDevices.getBoolean("mobile", false) && group.equals("Phones")) return true;
        if (blockedDevices.getBoolean("pc", false) && group.equals("PCs")) return true;
        if (blockedDevices.getBoolean("console", false) && group.equals("Consoles")) return true;
        return false;
    }


    public boolean checkPlayerInputMode(String inputMode) {
        ConfigSection blockedIMs = config.getSection("blocked_inputmodes");
    
        if (blockedIMs.getBoolean("mouse_and_keyboard", false) && inputMode.equals("Keyboard and mouse")) return true;
        if (blockedIMs.getBoolean("gamepad", false) && inputMode.equals("Gamepad")) return true;
        if (blockedIMs.getBoolean("touch_input", false) && inputMode.equals("Touch input")) return true;
    
        return false;
    }


    public String getDeviceGroup(String device) {
        String[] phoneGroup = new String[]{"Android", "iOS", "Windows Phone"};
        String[] pcGroup = new String[]{"Windows 10/11", "macOS", "FireOS"};
        String[] consoleGroup = new String[]{"Xbox", "PlayStation", "Nintendo Switch"};
        if (Arrays.asList(phoneGroup).contains(device)) {
            return "Phones";
        }
        else if (Arrays.asList(pcGroup).contains(device)) {
            return "PCs";
        }
        else if (Arrays.asList(consoleGroup).contains(device)) {
            return "Consoles";
        }
        return "Unknown";
    }
}
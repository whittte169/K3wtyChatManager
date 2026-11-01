package com.white.k3wtychatmanager.api.chat;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.types.InputMode;
import cn.nukkit.utils.TextFormat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Supplier;
import java.util.regex.Matcher;

import com.white.k3wtychatmanager.api.client.ClientManager;
import com.white.k3wtychatmanager.api.client.DeviceOS;

/**
 * 
 * ChatManager
 * library for method with Player chat pos
 * needs LuckPerms plugin for correct work
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * @version 1.0.1
 * 
 */
public class ChatManager {
    private volatile String globalChatSymbol = "!";
    private final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
    private final Map<ChatType, String> chatTypes = new ConcurrentHashMap<>();
    private final Map<DeviceOS, String> deviceOsTypes = new ConcurrentHashMap<>();
    private final Map<InputMode, String> inputModeTypes = new ConcurrentHashMap<>();
    private ClientManager clientManager = ClientManager.getInstance();
    private static ChatManager INSTANCE = new ChatManager();

    private ChatManager() {
        this.globalChatSymbol = "!";
        chatTypes.put(ChatType.UNDEFINED, "&7[U]");
        chatTypes.put(ChatType.LOCAL, "&6[L]");
        chatTypes.put(ChatType.GLOBAL, "&c[G]");
        DeviceOS[] mobileDeviceOS = new DeviceOS[]{DeviceOS.ANDROID, DeviceOS.IOS, DeviceOS.WINDOWS_PHONE};
        DeviceOS[] desktopDeviceOS = new DeviceOS[]{DeviceOS.WINDOWS, DeviceOS.WINDOWS_X86, DeviceOS.MACOS, DeviceOS.LINUX, DeviceOS.FIRE};
        DeviceOS[] consoleDeviceOS = new DeviceOS[]{DeviceOS.PLAYSTATION, DeviceOS.XBOX, DeviceOS.SWITCH};
        DeviceOS[] otherDeviceOS = new DeviceOS[]{DeviceOS.DEDICATED, DeviceOS.HOLOLENS, DeviceOS.GEAR_VR, DeviceOS.TVOS};
        for (DeviceOS deviceOS : DeviceOS.values()) {
            if (Arrays.asList(mobileDeviceOS).contains(deviceOS)) {
                deviceOsTypes.put(deviceOS, "");
            }
            else if (Arrays.asList(desktopDeviceOS).contains(deviceOS)) {
                deviceOsTypes.put(deviceOS, "");
            }
            else if (Arrays.asList(consoleDeviceOS).contains(deviceOS)) {
                deviceOsTypes.put(deviceOS, "");
            }
            else if (Arrays.asList(otherDeviceOS).contains(deviceOS)) {
                deviceOsTypes.put(deviceOS, "Other");
            } else {
                deviceOsTypes.put(DeviceOS.UNDEFINED, "Undefined");
            }
        }
        inputModeTypes.put(InputMode.MOUSE, "Mouse and keyboard");
        inputModeTypes.put(InputMode.GAME_PAD, "Game pad");
    }

    /**
     * Get an instance of ChatManager
     * <h4>This method cannot return null</h4>
     * @return INSTANCE {@link ChatManager}
     * @throws IllegalStateException
     */
    public static @NonNull ChatManager getInstance() throws IllegalStateException {
        if (INSTANCE == null) throw new IllegalStateException("ChatManager is not initialized.");
        return INSTANCE;
    }

    /**
     * Getting player's rank
     * <h4>This method cannot return null</h4>
     * @param playerUUID {@link UUID}
     * @return rank {@link String}
     */
    public @NonNull String getPlayerRank(UUID playerUUID) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(playerUUID);
        if (user == null) {
            return "";
        }
        CachedMetaData metaData;
        metaData = user.getCachedData().getMetaData(QueryOptions.contextual(luckPerms.getContextManager().getStaticContext()));
        String rank = metaData.getPrefix();
        if (rank == null) {
            String primaryGroup = user.getPrimaryGroup();
            Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
            if (group == null) return "";
            metaData = group.getCachedData().getMetaData(QueryOptions.contextual(luckPerms.getContextManager().getStaticContext()));
            rank = metaData.getPrefix();
        }
        return rank != null ? rank : "";
    }

    /**
     * Getting player's title
     * <h4>This method cannot return null</h4>
     * @param playerUUID {@link UUID}
     * @return title {@link String}
     */
    public @NonNull String getPlayerTitle(UUID playerUUID) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(playerUUID);
        if (user == null) {
            return "";
        }
        CachedMetaData metaData;
        metaData = user.getCachedData().getMetaData(QueryOptions.contextual(luckPerms.getContextManager().getStaticContext()));
        String title = metaData.getSuffix();
        if (title == null) {
            String primaryGroup = user.getPrimaryGroup();
            Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
            if (group == null) return "";
            metaData = group.getCachedData().getMetaData(QueryOptions.contextual(luckPerms.getContextManager().getStaticContext()));
            title = metaData.getSuffix();
        }
        return title != null ? title : "";
    }

    /**
     * Format message
     * <h4>This method cannot return null</h
     * @param string {@link String}
     * @param player {@link Player}
     * @param chatType {@link ChatType}
     * @return result {@link String}
     */
    public @NonNull String formatMessage(String string, Player player, ChatType chatType, String message) {
        if (string == null) return "";
        if (player == null) return string;
        String[] pos = getPlayerPosition(player, false);
        UUID uuid = player.getUniqueId();
        DeviceOS playerDeviceOS = clientManager.getPlayerDeviceOs(player);
        InputMode playerInputMode = clientManager.getPlayerInputMode(player);
        Map<String, Supplier<String>> placeholders = new HashMap<>();
        placeholders.put("device", () -> getDeviceOsDisplay(playerDeviceOS));
        placeholders.put("input_mode", () -> getInputModeDisplay(playerInputMode));
        placeholders.put("health", () -> getPlayerHealth(player));
        placeholders.put("player", player::getName);
        placeholders.put("rank", () -> TextFormat.colorize('&', getPlayerRank(uuid)));
        placeholders.put("title", () -> TextFormat.colorize('&', getPlayerTitle(uuid)));
        placeholders.put("gamemode", () -> getPlayerGameMode(player));
        placeholders.put("dimension", () -> pos[0]);
        placeholders.put("x", () -> pos[1]);
        placeholders.put("y", () -> pos[2]);
        placeholders.put("z", () -> pos[3]);
        placeholders.put("position", () -> getPlayerPosition(player, true)[0]);
        placeholders.put("chat_type", () -> TextFormat.colorize('&', getChatTypeDisplay(chatType)));
        placeholders.put("message", () -> message != null ? message : "");
        placeholders.put("ping", () -> String.valueOf(player.getPing()));
        String template = TextFormat.colorize('&', string);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            Supplier<String> supplier = placeholders.get(matcher.group(1));
            String value = supplier != null ? supplier.get() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString() != null ? result.toString() : string;
    }

    /**
     * Format name tag
     * <h4>This method cannot return null</h
     * @param string {@link String}
     * @param player {@link Player}
     * @return result {@link String}
     */
    public @NonNull String formatName(String string, Player player) {
        if (string == null) return "";
        if (player == null) return string;
        String[] pos = getPlayerPosition(player, false);
        UUID uuid = player.getUniqueId();
        DeviceOS playerDeviceOS = clientManager.getPlayerDeviceOs(player);
        InputMode playerInputMode = clientManager.getPlayerInputMode(player);
        Map<String, Supplier<String>> placeholders = new HashMap<>();
        placeholders.put("device", () -> getDeviceOsDisplay(playerDeviceOS));
        placeholders.put("input_mode", () -> getInputModeDisplay(playerInputMode));
        placeholders.put("health", () -> getPlayerHealth(player));
        placeholders.put("player", player::getName);
        placeholders.put("rank", () -> TextFormat.colorize('&', getPlayerRank(uuid)));
        placeholders.put("title", () -> TextFormat.colorize('&', getPlayerTitle(uuid)));
        placeholders.put("gamemode", () -> getPlayerGameMode(player));
        placeholders.put("dimension", () -> pos[0]);
        placeholders.put("x", () -> pos[1]);
        placeholders.put("y", () -> pos[2]);
        placeholders.put("z", () -> pos[3]);
        placeholders.put("position", () -> getPlayerPosition(player, true)[0]);
        placeholders.put("ping", () -> String.valueOf(player.getPing()));
        String template = TextFormat.colorize('&', string);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            Supplier<String> supplier = placeholders.get(matcher.group(1));
            String value = supplier != null ? supplier.get() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(result);
        return result.toString() != null ? result.toString() : string;
    }

    /**
     * Get player health
     * <h4>This method cannot return null</h4>
     * @param player {@link Player}
     * @return player's health {@link String}
     */
    public @NonNull String getPlayerHealth(Player player) {
        return String.valueOf(player.getHealth()) != null ? String.valueOf(player.getHealth()) : "0.0";
    }

    /**
     * Get player's gamemode
     * <h4>This method cannot return null</h4>
     * @param player {@link Player}
     * @return player's gamemode {@link String}
     */
    public @NonNull String getPlayerGameMode(Player player) {
        int gamemodeIndex = player.getGamemode();
        String gamemode;
        switch (gamemodeIndex) {
            case 0 -> gamemode = "Survival";
            case 1 -> gamemode = "Creative";
            case 2 -> gamemode = "Adventure";
            case 3 -> gamemode = "Spectator";
            default -> gamemode = "Undefined";
        }
        return gamemode != null ? gamemode : "Undefined";
    }

    /**
     * Get player's position
     * <h4>This method cannot return null</h4>
     * @param player {@link Player}
     * @param formated {@link Boolean}
     * @return position {@link Arrays}
     */
    public @NonNull String[] getPlayerPosition(Player player, Boolean formated) {
        if (player == null) return new String[]{};
        formated = formated != null ? formated : false;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        int level = player.getLevel().getDimension();
        String dimension;
        switch (level) {
            case 0 -> dimension = "Overworld";
            case 1 -> dimension = "Nether";
            case 2 -> dimension = "The End";
            default -> dimension = "Undefined";
        }
        if (formated) {
            String formatedPosition = String.format("D: %s, X: %d, Y: %d, Z: %d", 
                dimension, 
                (int) Math.floor(x), 
                (int) Math.floor(y), 
                (int) Math.floor(z)
            );
            return new String[]{formatedPosition};
        } else {
            return new String[]{
                dimension, 
                String.valueOf((int) Math.floor(x)), 
                String.valueOf((int) Math.floor(y)), 
                String.valueOf((int) Math.floor(z))
            };
        }
    }

    /**
     * Get chat type of message
     * <h4>This method cannot return null</h4>
     * @param message {@link String}
     * @return chat type {@link ChatType}
     */
    public @NonNull ChatType getMessageChatType(String message) {
        return message.startsWith(this.globalChatSymbol) ? ChatType.GLOBAL : ChatType.LOCAL;
    }

    /**
     * Remove global chat symbol from message
     * <h4>This method cannot return null</h4>
     * @param message {@link String}
     * @return formated message {@link String}
     */
    public @NonNull String removeGlobalChatSymbolFromMessage(String message) {
        return message.startsWith(this.globalChatSymbol) ? message.substring(this.globalChatSymbol.length()) : message;
    }

    /**
     * Get chat type display
     * <h4>This method cannot return null</h4>
     * @param chatType {@link ChatType}
     * @return chat type display {@link String}
     */
    public @NonNull String getChatTypeDisplay(ChatType chatType) {
        String display = chatTypes.getOrDefault(chatType, chatTypes.get(ChatType.UNDEFINED));
        return display != null ? display : "Undefined";
    }

    /**
     * Set chat type display
     * @param chatType {@link ChatType}
     * @param display {@link String}
     */
    public void setChatTypeDisplay(ChatType chatType, String display) {
        chatTypes.put(chatType, display != null ? display : "");
    }

    /**
     * Get global chat symbol
     * <h4>This method cannot return null</h4>
     * @return global chat symbol {@link String}
     */
    public @NonNull String getGlobalChatSymbol() {
        return this.globalChatSymbol != null ? this.globalChatSymbol : "!";
    }

    /**
     * Set global chat symbol
     * @param symbol {@link String}
     */
    public void setGlobalChatSymbol(String symbol) {
        this.globalChatSymbol = symbol != null ? symbol : this.globalChatSymbol;
    }

    /**
     * Get device OS display
     * <h4>This method cannot return null</h4>
     * @param deviceOs {@link DeviceOS}
     * @return device OS display {@link String}
     */
    public @NonNull String getDeviceOsDisplay(DeviceOS deviceOs) {
        String display = deviceOsTypes.getOrDefault(deviceOs, deviceOsTypes.get(DeviceOS.UNDEFINED));
        return display != null ? display : "Undefined";
    }
    
    /**
     * Set device OS display
     * @param deviceOs {@link DeviceOS}
     * @param display {@link String}
     */
    public void setDeviceOsDisplay(DeviceOS deviceOs, String display) {
        deviceOsTypes.put(deviceOs, display != null ? display : "");
    }

    /**
     * Get input mode display
     * <h4>This method cannot return null</h4>
     * @param inputMode {@link InputMode}
     * @return input mode display {@link String}
     */
    public @NonNull String getInputModeDisplay(InputMode inputMode) {
        String display = inputModeTypes.getOrDefault(inputMode, inputModeTypes.get(InputMode.UNDEFINED));
        return display != null ? display : "Undefined";
    }

    /**
     * Set input mode display
     * @param inputMode {@link InputMode}
     * @param display {@link String}
     */
    public void setInputModeDisplay(InputMode inputMode, String display) {
        inputModeTypes.put(inputMode, display != null ? display : "");
    }
}
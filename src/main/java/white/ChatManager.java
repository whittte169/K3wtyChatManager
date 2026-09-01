package white;

import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.Server;
import cn.nukkit.Player;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.query.QueryOptions;

import java.util.UUID;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * ChatManager class
 * Library for method with Player
 * also uses to listen Player's chat event
 * Needs LuckPerms plugin for correct work
 * @author whittte169
 * @version 1.0.0
 * 
 */
public class ChatManager implements Listener {
    private Config config;
    private final DeviceManager manager;

    public ChatManager(Config config, DeviceManager manager) {
        this.config = config;
        this.manager = manager;
    }

    private String getPlayerPrivilege(UUID playerUUID) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(playerUUID);
        if (user == null) {
            return "";
        }
        String primaryGroup = user.getPrimaryGroup();
        Group group = luckPerms.getGroupManager().getGroup(primaryGroup);
        if (group == null) return "";
        CachedMetaData metaData = group.getCachedData().getMetaData(QueryOptions.contextual(luckPerms.getContextManager().getStaticContext()));
        String privilege = metaData.getPrefix();
        return privilege != null ? privilege : "";
    }

    public void playerUpdateNameTag(Player player) {
        player.setNameTag(formatNameTag(player));
    }

    private String formatNameTag(Player player) {
        final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
        String[] data = manager.getPlayerDeviceOSAndIM(player);
        String privilege = getPlayerPrivilege(player.getUniqueId());
        int health = (int) player.getHealth();
        String ping = manager.getPlayerPing(player);
        String nametagFormat = config.getString("nametag_format", "[%privilege%§r] %player% §7| §r%device%\n  §c%health% HP §7|§r %ping%");
        Map<String, String> variables = new HashMap<>();
        variables.put("privilege", privilege);
        variables.put("player", player.getName());
        variables.put("device", data[0]);
        variables.put("health", String.valueOf(health));
        variables.put("ping", ping);
        variables.put("inputmode", data[1]);
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(nametagFormat);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = variables.getOrDefault(variable, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String formatDisplayName(Player player, String message) {
        final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
        String[] data = manager.getPlayerDeviceOSAndIM(player);
        String privilege = getPlayerPrivilege(player.getUniqueId());
        String ping = manager.getPlayerPing(player);
        String displaynameFormat = config.getString("displayname_format", "%chat% [%privilege%§r] %player% §7> §r%message%");
        Map<String, String> variables = new HashMap<>();
        variables.put("privilege", privilege);
        variables.put("player", player.getName());
        variables.put("device", data[0]);
        variables.put("ping", ping);
        variables.put("message", message);
        variables.put("chat", getChatType(message));
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(displaynameFormat);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String variable = matcher.group(1);
            String replacement = variables.getOrDefault(variable, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String getChatType(String message) {
        if (message == null) {
            return "§7U§r";
        }
        if (!message.startsWith(config.getString("global_chat_symbol", "!"))) {
            return "§6L§r";
        }
        if (message.startsWith(config.getString("global_chat_symbol", "!"))) {
            return "§cG§r";
        }
        return "§7U§r";
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerChat(PlayerChatEvent event) {
        if (event.getMessage() == null) {
            return;
        }
        if (config.getBoolean("displayname_enabled", true)) {
            event.setCancelled(true);
            if (event.getMessage().startsWith(config.getString("global_chat_symbol", "!"))) {
                Server.getInstance().broadcastMessage(formatDisplayName(event.getPlayer(), event.getMessage()).replaceFirst("!", ""));
            } else {
                Player player = event.getPlayer();
                float radius = (float) config.getInt("local_chat_radius", 25);
                Entity[] nearbyEntities = player.getLevel().getNearbyEntities(
                    player.getBoundingBox().grow(radius, radius, radius), 
                    player
                );
                for (Entity entity : nearbyEntities) {
                    if (!(entity instanceof Player)) {
                        continue;
                    }
                    Player target = (Player) entity;
                    target.sendMessage(formatDisplayName(player, event.getMessage()));
                }
                Main.getInstance().getLogger().info(formatDisplayName(player, event.getMessage()));
                player.sendMessage(formatDisplayName(player, event.getMessage()));
            }
        }
    }
}
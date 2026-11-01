package com.white.k3wtychatmanager;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.white.k3wtychatmanager.api.chat.ChatManager;
import com.white.k3wtychatmanager.api.chat.ChatType;
import com.white.k3wtychatmanager.api.client.ClientManager;

public interface ChatManagerHandler {
    public final Map<String, Long> playersCooldowns = new HashMap<>();
    Plugin getPlugin();

    default void reloadConfigLabels() {
        getPlugin().saveDefaultConfig();
        getPlugin().reloadConfig();
        Config config = getPlugin().getConfig();
        ChatManager chatManager = ChatManager.getInstance();
        chatManager.setChatTypeDisplay(ChatType.LOCAL, "&6[L]");
        chatManager.setChatTypeDisplay(ChatType.GLOBAL, "&c[G]");
        chatManager.setChatTypeDisplay(ChatType.UNDEFINED, "&7[U]");
        chatManager.setGlobalChatSymbol(config.getString("global_chat_symbol", "!"));
    }

    default void updateNameTag() {
        Config config = getPlugin().getConfig();
        if (!config.getBoolean("enable_name_tag_change", true)) return;
        ChatManager chatManager = ChatManager.getInstance();
        getPlugin().getServer().getScheduler().scheduleRepeatingTask(getPlugin(), () -> {
            for (Player player : getPlugin().getServer().getOnlinePlayers().values()) {
                player.setNameTag(chatManager.formatName(config.getString("name_tag_format", "%device% &7| &r[%rank%&r] %player% %title%\n  &c%health% HP &7| &r%ping% ms"), player));
            }
        }, 20);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    default void onChat(PlayerChatEvent event) {
        Config config = getPlugin().getConfig();
        ChatManager chatManager = ChatManager.getInstance();
        Player player = event.getPlayer();
        boolean isMuted = false;
        try {
            isMuted = ClientManager.getInstance().isPlayerMuted(player.getName());
        } catch (SQLException e) {
            getPlugin().getLogger().error(e.getMessage());
        }
        if (isMuted) {
            player.sendMessage(TextFormat.colorize('&', "&cYou're muted!"));
            event.setCancelled(true);
            return;
        }
        if (!config.getBoolean("enable_chat_dividing", true)) return;
        if (config.getBoolean("enable_chat_cooldowns", true)) {
            if (!player.hasPermission("white.permissions.chat.spam")) {
                if (playersCooldowns.containsKey(player.getName())) {
                    if ((playersCooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000 > 0) {
                        player.sendMessage(
                            TextFormat.colorize('&',
                            "&cWait " + (playersCooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000 + "s to send message!"
                            )
                        );
                        event.setCancelled(true);
                        return;
                    } else {
                        playersCooldowns.remove(player.getName());
                    }
                }
                String playerRank = ChatManager.getInstance().getPlayerRank(player.getUniqueId());
                int playerCooldown = config.getInt("chat_cooldowns.default", 3);;
                ConfigSection chatCooldowns = (ConfigSection) config.getSection("chat_cooldowns");
                for (Map.Entry<String, Object> entry : chatCooldowns.entrySet()) {
                    String rank = entry.getKey();
                    int cooldown = ((Number) entry.getValue()).intValue();
                    if (rank.equals(playerRank) && cooldown > 0) {
                        playerCooldown = cooldown;
                        break;
                    }
                }
                playersCooldowns.put(player.getName(), System.currentTimeMillis() + playerCooldown * 1000);
            }
        }
        String message = event.getMessage();
        if (!player.hasPermission("white.permissions.chat.caps") && config.getBoolean("enable_anticaps", true)) {
             if (message != null && message.matches(".*[А-ЯЁA-Z]{3,}.*")) {
                message = message.toLowerCase();
            }
        }
        if (!player.hasPermission("white.permissions.chat.color")) {
            message = TextFormat.clean(message).replaceAll("(?i)&[0-9a-fk-or]", "");;
        } else {
            message = TextFormat.colorize('&', message);
        }
        ChatType chatType = chatManager.getMessageChatType(message);
        if (chatType == ChatType.GLOBAL) {
            message = chatManager.removeGlobalChatSymbolFromMessage(message).trim();
            event.setCancelled(true);
            if (message.isBlank()) return;
            getPlugin().getServer().broadcastMessage(chatManager.formatMessage(config.getString("message_format", "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"), player, chatType, message));
        } else {
            event.setCancelled(true);
            if (message == null) return;
            double radius = config.getDouble("local_chat_radius", 25);
            for (Player target : getPlugin().getServer().getOnlinePlayers().values()) {
                if (target.getLevel() == player.getLevel() && target.distance(player) <= radius) target.sendMessage(chatManager.formatMessage(config.getString("message_format", "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"), player, chatType, message));
            }
            getPlugin().getLogger().info(chatManager.formatMessage(config.getString("message_format", "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"), player, chatType, message));
        }
    }
}

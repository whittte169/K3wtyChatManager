package com.white.k3wtychatmanager;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.player.PlayerChatEvent;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;

import com.white.k3wtychatmanager.api.chat.ChatManager;
import com.white.k3wtychatmanager.api.chat.ChatType;

public interface ChatManagerHandler {
    Plugin getPlugin();

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
        if (!config.getBoolean("enable_chat_dividing", true)) return;
        chatManager.setChatTypeDisplay(ChatType.LOCAL, "&6[L]");
        chatManager.setChatTypeDisplay(ChatType.GLOBAL, "&c[G]");
        chatManager.setChatTypeDisplay(ChatType.UNDEFINED, "&7[U]");
        chatManager.setGlobalChatSymbol(config.getString("global_chat_symbol", "!"));
        Player player = event.getPlayer();
        String message = event.getMessage();
        if (!player.hasPermission("white.permissions.chat.color")) {
            message = TextFormat.clean(message).replaceAll("(?i)&[0-9a-fk-or]", "");;
        }
        ChatType chatType = chatManager.getMessageChatType(message);
        if (chatType == ChatType.GLOBAL) {
            message = chatManager.removeGlobalChatSymbolFromMessage(message).trim();
            event.setCancelled(true);
            if (message.isBlank()) return;
            getPlugin().getServer().broadcastMessage(chatManager.formatMessage(config.getString("message_format", "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"), player, chatType, message));
        } else {
            event.setCancelled(true);
            if (message.isBlank()) return;
            double radius = config.getDouble("local_chat_radius", 25);
            for (Player target : getPlugin().getServer().getOnlinePlayers().values()) {
                if (target.getLevel() == player.getLevel() && target.distance(player) <= radius) target.sendMessage(chatManager.formatMessage(config.getString("message_format", "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"), player, chatType, message));
                getPlugin().getLogger().info(chatManager.formatMessage(config.getString("message_format", "%chat_type% &7| &r[%rank%&r] %player% %title% &8&l-> &r%message%"), player, chatType, message));
            }
        }
    }
}

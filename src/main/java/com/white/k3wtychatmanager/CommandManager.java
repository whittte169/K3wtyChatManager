package com.white.k3wtychatmanager;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;

import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;

import com.white.k3wtychatmanager.api.client.ClientManager;

/**
 *
 * CommandManager
 * used for register commands
 * @author <a href="https://github.com/whittte169">whittte169</a>
 *
 */
public final class CommandManager {
    private static CommandManager INSTANCE = new CommandManager();
    
    private CommandManager() {}

    public static @NonNull CommandManager getInstance() throws IllegalStateException {
        if (INSTANCE == null) throw new IllegalStateException("Main is not initialized.");
        return INSTANCE;
    }

    public void registerCommands(Plugin plugin) {
        String prefix = plugin.getName().toLowerCase();
        plugin.getServer().getCommandMap().register(prefix, new XUIDBanCommand());
        plugin.getServer().getCommandMap().register(prefix, new XUIDPardonCommand());
        plugin.getServer().getCommandMap().register(prefix, new getXUIDCommand());
    }

    private final class XUIDBanCommand extends Command {
        public XUIDBanCommand() {
            super("xuidban", "Ban player by XUID", "/xuidban <target> [reason]");
            this.setPermission("white.permissions.commands.xuidban");
            this.commandParameters.clear();
            this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("target", CommandParamType.TARGET),
                CommandParameter.newType("reason", CommandParamType.TEXT)
            });
        }

        @Override 
        public boolean execute(CommandSender sender, String lable, String[] args) {
            if (!this.testPermission(sender)) return false;
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) return false;
            if (sender instanceof Player) sender = (Player) sender;
            if (sender instanceof ConsoleCommandSender) sender = (ConsoleCommandSender) sender;
            if (args.length < 1) {
                sender.sendMessage(TextFormat.colorize('&', "Usage: " + this.getUsage()));
                return false;
            }
            String targetName = args[0];
            StringBuilder reasonBuilder = new StringBuilder();
            if (args.length > 1) {
                for (int i = 1; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
            }
            String reason = reasonBuilder.toString().trim();
            if (reason.isEmpty()) {
                reason = "No reason";
            }
            Player target = sender.getServer().getPlayer(targetName);
            String xuid;
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            ClientManager clientManager = ClientManager.getInstance();
            xuid = clientManager.getPlayerXUID(target);
            try {
                clientManager.banPlayerByXUID(xuid);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(targetName + " was banned by XUID");
            target.kick("You was banned!\nReason: " + reason);
            return true;
        }
    }

    private final class XUIDPardonCommand extends Command {
        public XUIDPardonCommand() {
            super("xuidpardon", "Ban player by XUID", "/xuidpardon <target>", new String[]{"xuidunban"});
            this.setPermission("white.permissions.commands.xuidban");
            this.commandParameters.clear();
            this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("target", CommandParamType.TARGET),
            });
        }

        @Override 
        public boolean execute(CommandSender sender, String lable, String[] args) {
            if (!this.testPermission(sender)) return false;
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) return false;
            if (sender instanceof Player) sender = (Player) sender;
            if (sender instanceof ConsoleCommandSender) sender = (ConsoleCommandSender) sender;
            if (args.length < 1) {
                sender.sendMessage(TextFormat.colorize('&', "Usage: " + this.getUsage()));
                return false;
            }
            String xuid = args[0];
            ClientManager clientManager = ClientManager.getInstance();
            Boolean isPlayerBanned = false;
            try {
                isPlayerBanned = clientManager.isPlayerBannedByXUID(xuid);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            if (!isPlayerBanned) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not banned!"));
                return false;
            }
            try {
                clientManager.unBanPlayerByXUID(xuid);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(xuid + " was unbanned by XUID");
            return true;
        }
    }

    private final class getXUIDCommand extends Command {
        public getXUIDCommand() {
            super("getxuid", "Get player's XUID", "/getxuid [target: string]");
            this.setPermission("white.permissions.commands.xuidban");
            this.commandParameters.clear();
            this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("target", CommandParamType.TARGET),
            });
        }

        @Override 
        public boolean execute(CommandSender sender, String lable, String[] args) {
            if (!this.testPermission(sender)) return false;
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) return false;
            if (sender instanceof Player) sender = (Player) sender;
            if (sender instanceof ConsoleCommandSender) sender = (ConsoleCommandSender) sender;
            if (args.length < 1) {
                sender.sendMessage(TextFormat.colorize('&', "Usage: " + this.getUsage()));
                return false;
            }
            String targetName = args[0];
            Player target = sender.getServer().getPlayer(targetName);
            String xuid;
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            ClientManager clientManager = ClientManager.getInstance();
            xuid = clientManager.getPlayerXUID(target);
            sender.sendMessage(targetName + "'s XUID is " + xuid);
            return true;
        }
    }
}

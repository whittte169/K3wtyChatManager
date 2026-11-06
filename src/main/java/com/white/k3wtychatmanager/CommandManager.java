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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        plugin.getServer().getCommandMap().register(prefix, new muteCommand());
        plugin.getServer().getCommandMap().register(prefix, new unMuteCommand());
        plugin.getServer().getCommandMap().register(prefix, new DeviceIdBanCommand());
        plugin.getServer().getCommandMap().register(prefix, new DeviceIdPardonCommand());
        plugin.getServer().getCommandMap().register(prefix, new getDeviceIdCommand());
    }

    private final class XUIDBanCommand extends Command {
        private final Pattern FULL_PATTERN = Pattern.compile("(\\d+[wdhms])+");
        private final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");
        public XUIDBanCommand() {
            super("xuidban", "Ban player by XUID", "/xuidban <target> <time> [reason]");
            this.setPermission("white.permissions.commands.xuidban");
            this.commandParameters.clear();
            this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("target", CommandParamType.TARGET),
                CommandParameter.newType("time", CommandParamType.STRING),
                CommandParameter.newType("reason", CommandParamType.TEXT)
            });
        }

        @Override 
        public boolean execute(CommandSender sender, String lable, String[] args) {
            if (!this.testPermission(sender)) return false;
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) return false;
            if (sender instanceof Player) sender = (Player) sender;
            if (sender instanceof ConsoleCommandSender) sender = (ConsoleCommandSender) sender;
            if (args.length < 2) {
                sender.sendMessage(TextFormat.colorize('&', "Usage: " + this.getUsage()));
                return false;
            }
            String targetName = args[0];
            String time = args[1].trim().toLowerCase();
            if (!FULL_PATTERN.matcher(time).matches()) {
                sender.sendMessage(TextFormat.colorize('&', "&cTime format: 2d, 3h, 5h1m (w, d, h, m, s)"));
                return false;
            }
            long totalSeconds = 0;
            Matcher matcher = TIME_PATTERN.matcher(time.toLowerCase());
            while (matcher.find()) {
                long duration = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2);
                switch (unit) {
                    case "w": totalSeconds += duration * 7 * 24 * 60 * 60; break;
                    case "d": totalSeconds += duration * 24 * 60 * 60; break;
                    case "h": totalSeconds += duration * 60 * 60; break;
                    case "m": totalSeconds += duration * 60; break;
                    case "s": totalSeconds += duration * 1; break;
                }
            }
            if (totalSeconds <= 0) {
                sender.sendMessage(TextFormat.colorize('&', "&cTime must be greater than 0!"));
                return false;
            }
            int seconds = (int) Math.min(totalSeconds, Integer.MAX_VALUE);
            StringBuilder reasonBuilder = new StringBuilder();
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
            }
            String reason = reasonBuilder.toString().trim();
            if (reason.isEmpty()) {
                reason = "No reason";
            }
            Player target = sender.getServer().getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            targetName = target.getName();
            ClientManager clientManager = ClientManager.getInstance();
            String targetXUID = clientManager.getPlayerXUID(target);
            try {
                clientManager.banPlayerByXUID(targetXUID ,targetName, seconds, reason, sender.getName());
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(targetName + " was banned");
            target.kick("You're banned!\nReason: " + reason + "\nTime: " + time + "\nBy: " + sender.getName());
            sender.getServer().broadcastMessage(TextFormat.colorize(
                '&', "&c" + targetName + " was banned!\n&cReason: " + reason + "\n&cTime: " + time + "\n&cBy: " + sender.getName()
            ));
            return true;
        }
    }

    private final class XUIDPardonCommand extends Command {
        public XUIDPardonCommand() {
            super("xuidpardon", "Unban player banned by XUID", "/xuidpardon <target>", new String[]{"xuidunban"});
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
            String name = args[0];
            ClientManager clientManager = ClientManager.getInstance();
            Boolean isPlayerBanned = false;
            try {
                isPlayerBanned = clientManager.isPlayerBannedByXUID(null, name);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            if (!isPlayerBanned) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not banned!"));
                return false;
            }
            try {
                clientManager.unBanPlayerByXUID(null, name);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(name + " was unbanned");
            return true;
        }
    }

    private final class getXUIDCommand extends Command {
        public getXUIDCommand() {
            super("getxuid", "Get player's XUID", "/getxuid <target>");
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

    private final class muteCommand extends Command {
        private final Pattern FULL_PATTERN = Pattern.compile("(\\d+[wdhms])+");
        private final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");
        public muteCommand() {
            super("mute", "Mute player", "/mute <target> <time> [reason]");
            this.setPermission("white.permissions.commands.mute");
            this.commandParameters.clear();
            this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("target", CommandParamType.TARGET),
                CommandParameter.newType("time", CommandParamType.STRING),
                CommandParameter.newType("reason", CommandParamType.TEXT)
            });
        }

        @Override 
        public boolean execute(CommandSender sender, String lable, String[] args) {
            if (!this.testPermission(sender)) return false;
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) return false;
            if (sender instanceof Player) sender = (Player) sender;
            if (sender instanceof ConsoleCommandSender) sender = (ConsoleCommandSender) sender;
            if (args.length < 2) {
                sender.sendMessage(TextFormat.colorize('&', "Usage: " + this.getUsage()));
                return false;
            }
            String targetName = args[0];
            String time = args[1].trim().toLowerCase();
            if (!FULL_PATTERN.matcher(time).matches()) {
                sender.sendMessage(TextFormat.colorize('&', "&cTime format: 2d, 3h, 5h1m (w, d, h, m, s)"));
                return false;
            }
            long totalSeconds = 0;
            Matcher matcher = TIME_PATTERN.matcher(time.toLowerCase());
            while (matcher.find()) {
                long duration = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2);
                switch (unit) {
                    case "w": totalSeconds += duration * 7 * 24 * 60 * 60; break;
                    case "d": totalSeconds += duration * 24 * 60 * 60; break;
                    case "h": totalSeconds += duration * 60 * 60; break;
                    case "m": totalSeconds += duration * 60; break;
                    case "s": totalSeconds += duration * 1; break;
                }
            }
            if (totalSeconds <= 0) {
                sender.sendMessage(TextFormat.colorize('&', "&cTime must be greater than 0!"));
                return false;
            }
            int seconds = (int) Math.min(totalSeconds, Integer.MAX_VALUE);
            StringBuilder reasonBuilder = new StringBuilder();
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
            }
            String reason = reasonBuilder.toString().trim();
            if (reason.isEmpty()) {
                reason = "No reason";
            }
            Player target = sender.getServer().getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            targetName = target.getName();
            ClientManager clientManager = ClientManager.getInstance();
            try {
                clientManager.mutePlayer(targetName, seconds, reason, sender.getName());
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(targetName + " was muted");
            target.sendMessage(
                TextFormat.colorize(
                    '&', "&cYou're muted!\n&cReason: " + reason + "\n&cTime: " + time + "\n&cBy: " + sender.getName()
                )
            );
            sender.getServer().broadcastMessage(TextFormat.colorize(
                '&', "&c" + targetName + " was muted!\n&cReason: " + reason + "\n&cTime: " + time + "\n&cBy: " + sender.getName()
            ));
            return true;
        }
    }

    private final class unMuteCommand extends Command {
        public unMuteCommand() {
            super("unmute", "Unmute player", "/unmute <target>");
            this.setPermission("white.permissions.commands.mute");
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
            String name = args[0];
            Player target = sender.getServer().getPlayer(name);
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            ClientManager clientManager = ClientManager.getInstance();
            Boolean isPlayerMuted = false;
            try {
                isPlayerMuted = clientManager.isPlayerMuted(target.getName());
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            if (!isPlayerMuted) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not muted!"));
                return false;
            }
            try {
                clientManager.unMutePlayer(target.getName());
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(target.getName() + " was unmuted");
            target.sendMessage("You were unmuted!");
            return true;
        }
    }

    private final class DeviceIdBanCommand extends Command {
        private final Pattern FULL_PATTERN = Pattern.compile("(\\d+[wdhms])+");
        private final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([wdhms])");
        public DeviceIdBanCommand() {
            super("deviceban", "Ban player by device ID", "/deviceban <target> <time> [reason]");
            this.setPermission("white.permissions.commands.deviceban");
            this.commandParameters.clear();
            this.commandParameters.put("default", new CommandParameter[]{
                CommandParameter.newType("target", CommandParamType.TARGET),
                CommandParameter.newType("time", CommandParamType.STRING),
                CommandParameter.newType("reason", CommandParamType.TEXT)
            });
        }

        @Override 
        public boolean execute(CommandSender sender, String lable, String[] args) {
            if (!this.testPermission(sender)) return false;
            if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) return false;
            if (sender instanceof Player) sender = (Player) sender;
            if (sender instanceof ConsoleCommandSender) sender = (ConsoleCommandSender) sender;
            if (args.length < 2) {
                sender.sendMessage(TextFormat.colorize('&', "Usage: " + this.getUsage()));
                return false;
            }
            String targetName = args[0];
            String time = args[1].trim().toLowerCase();
            if (!FULL_PATTERN.matcher(time).matches()) {
                sender.sendMessage(TextFormat.colorize('&', "&cTime format: 2d, 3h, 5h1m (w, d, h, m, s)"));
                return false;
            }
            long totalSeconds = 0;
            Matcher matcher = TIME_PATTERN.matcher(time.toLowerCase());
            while (matcher.find()) {
                long duration = Long.parseLong(matcher.group(1));
                String unit = matcher.group(2);
                switch (unit) {
                    case "w": totalSeconds += duration * 7 * 24 * 60 * 60; break;
                    case "d": totalSeconds += duration * 24 * 60 * 60; break;
                    case "h": totalSeconds += duration * 60 * 60; break;
                    case "m": totalSeconds += duration * 60; break;
                    case "s": totalSeconds += duration * 1; break;
                }
            }
            if (totalSeconds <= 0) {
                sender.sendMessage(TextFormat.colorize('&', "&cTime must be greater than 0!"));
                return false;
            }
            int seconds = (int) Math.min(totalSeconds, Integer.MAX_VALUE);
            StringBuilder reasonBuilder = new StringBuilder();
            if (args.length > 2) {
                for (int i = 2; i < args.length; i++) {
                    reasonBuilder.append(args[i]).append(" ");
                }
            }
            String reason = reasonBuilder.toString().trim();
            if (reason.isEmpty()) {
                reason = "No reason";
            }
            Player target = sender.getServer().getPlayer(targetName);
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            targetName = target.getName();
            ClientManager clientManager = ClientManager.getInstance();
            String targetDeviceIdString = clientManager.getPlayerDeviceId(target);
            try {
                clientManager.banPlayerByDeviceId(targetDeviceIdString ,targetName, seconds, reason, sender.getName());
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(targetName + " was banned");
            target.kick("You're banned!\nReason: " + reason + "\nTime: " + time + "\nBy: " + sender.getName());
            sender.getServer().broadcastMessage(TextFormat.colorize(
                '&', "&c" + targetName + " was banned!\n&cReason: " + reason + "\n&cTime: " + time + "\n&cBy: " + sender.getName()
            ));
            return true;
        }
    }

    private final class DeviceIdPardonCommand extends Command {
        public DeviceIdPardonCommand() {
            super("devicepardon", "Unban player banned by device ID", "/devicepardon <target>", new String[]{"deviceunban"});
            this.setPermission("white.permissions.commands.deviceban");
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
            String name = args[0];
            ClientManager clientManager = ClientManager.getInstance();
            Boolean isPlayerBanned = false;
            try {
                isPlayerBanned = clientManager.isPlayerBannedByDeviceId(null, name);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            if (!isPlayerBanned) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not banned!"));
                return false;
            }
            try {
                clientManager.unBanPlayerByDeviceId(null, name);
            } catch (SQLException e) {
                sender.sendMessage(TextFormat.colorize('&', "&c" + e.getMessage()));
                return false;
            }
            sender.sendMessage(name + " was unbanned");
            return true;
        }
    }

    private final class getDeviceIdCommand extends Command {
        public getDeviceIdCommand() {
            super("getdeviceid", "Get player's device ID", "/getdeviceid <target>");
            this.setPermission("white.permissions.commands.deviceban");
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
            String deviceId;
            if (target == null) {
                sender.sendMessage(TextFormat.colorize('&', "&cPlayer is not found!"));
                return false;
            }
            ClientManager clientManager = ClientManager.getInstance();
            deviceId = clientManager.getPlayerDeviceId(target);
            sender.sendMessage(targetName + "'s device ID is " + deviceId);
            return true;
        }
    }
}

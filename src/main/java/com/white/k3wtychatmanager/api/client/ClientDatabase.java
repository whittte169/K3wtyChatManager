package com.white.k3wtychatmanager.api.client;

import java.io.File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 
 * ClientDatabase interface
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * @version 1.0.0
 */
public interface ClientDatabase {
    /**
     * Get database
     * @return database {@link Connection}
     */
    Connection getConnection();

    /**
     * Check is database opened
     * @return is database opened {@link Boolean}
     */
    default boolean isDatabaseOpened() {
        try {
            Connection connection = getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Open database
     * initialize this method just
     * one time to main database variable
     * then override getConnection to
     * it will return a database variable
     * @param folder {@link String}
     * @return connection {@link Connection}
     * @throws SQLException
     */
    default Connection openDatabase(String folder) throws SQLException {
        if (folder == null) return null;
        File dataFolder = new File(folder);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
        File databaseFile = new File(dataFolder, "database.db");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS banned_players_by_xuid (xuid TEXT PRIMARY KEY, name TEXT)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS muted_players (name TEXT PRIMARY KEY, time INTEGER, reason TEXT, admin TEXT)");
        }
        return connection;
    }

    /**
     * Close database
     * @param force {@link Boolean}
     */
    default void closeDatabase(Boolean force) {
        if (force == null || !force) return;
        try {
            Connection connection = getConnection();
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {}
    }

    /**
     * Ban player by XUID
     * @param xuid {@link String}
     * @throws SQLException
     */
    default void banPlayerByXUID(String xuid, String name) throws SQLException {
        if (xuid == null || xuid.isEmpty()) return;
        if (name == null) name = "";
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT OR IGNORE INTO banned_players_by_xuid (xuid, name) VALUES (?, ?)")) {
            statement.setString(1, xuid);
            statement.setString(2, name);
            statement.executeUpdate();
        }
    }

    /**
     * Unban player by XUID
     * @param xuid {@link String}
     * @throws SQLException
     */
    default void unBanPlayerByXUID(String xuid, String name) throws SQLException {
        if (xuid != null) {
            try (PreparedStatement statement = getConnection().prepareStatement(
                "DELETE FROM banned_players_by_xuid WHERE xuid = ?")) {
                statement.setString(1, xuid);
                statement.executeUpdate();
            }
            return;
        }
        if (name != null) {
            try (PreparedStatement statement = getConnection().prepareStatement(
                "DELETE FROM banned_players_by_xuid WHERE name = ?")) {
                statement.setString(1, name);
                statement.executeUpdate();
            }
        }
    }

    /**
     * Check is player banned by XUID
     * @param xuid {@link String}
     * @return is player banned by XUID {@link Boolean}
     * @throws SQLException
     */
    default boolean isPlayerBannedByXUID(String xuid, String name) throws SQLException {
        if (xuid != null) {
            try (PreparedStatement statement = getConnection().prepareStatement(
                    "SELECT 1 FROM banned_players_by_xuid WHERE xuid = ?")) {
                statement.setString(1, xuid);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
        if (name != null) {
            try (PreparedStatement statement = getConnection().prepareStatement(
                    "SELECT 1 FROM banned_players_by_xuid WHERE name = ?")) {
                statement.setString(1, name);
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        }
        return false;
    }

    /**
     * Mute player
     * @param name {@link String}
     * @param time {@link Integer} in seconds
     * @param reason {@link String}
     * @param admin {@link String}
     * @throws SQLException
     */
    default void mutePlayer(String name, int time, String reason, String admin) throws SQLException {
        if (name == null) return;
        if (time < 0) return;
        if (reason == null) reason = "No reason";
        if (admin == null) admin = "Console";
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT OR REPLACE INTO muted_players (name, time, reason, admin) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, name);
            statement.setLong(2, System.currentTimeMillis() + (time * 1000L));
            statement.setString(3, reason);
            statement.setString(4, admin);
            statement.executeUpdate();
        }
    }

    /**
     * Unmute player
     * @param name {@link String}
     * @throws SQLException
     */
    default void unMutePlayer(String name) throws SQLException {
        if (name == null) return;
        try (PreparedStatement statement = getConnection().prepareStatement(
            "DELETE FROM muted_players WHERE name = ?")) {
            statement.setString(1, name);
            statement.executeUpdate();
        }
    }

    /**
     * Check is player muted
     * @param name {@link String}
     * @return is player muted {@link Boolean}
     * @throws SQLException
     */
    default boolean isPlayerMuted(String name) throws SQLException {
        if (name != null) {
            try (PreparedStatement statement = getConnection().prepareStatement(
                    "SELECT time FROM muted_players WHERE name = ?")) {
                statement.setString(1, name);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        long time = result.getLong("time");
                        if (System.currentTimeMillis() > time) {
                            try (PreparedStatement state = getConnection().prepareStatement(
                                "DELETE FROM muted_players WHERE name = ?")) {
                                state.setString(1, name);
                                state.executeUpdate();
                            }
                            return false;
                        }
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Get player's mute info
     * @param name {@link String}
     * @return player's mute info {@link Arrays}
     * @throws SQLException
     */
    default String[] getPlayerMuteInfo(String name) throws SQLException {
        if (name != null) {
            try (PreparedStatement statement = getConnection().prepareStatement(
                    "SELECT time, reason, admin FROM muted_players WHERE name = ?")) {
                statement.setString(1, name);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        long time = result.getLong("time");
                        String admin = result.getString("admin");
                        String reason = result.getString("reason");
                        if ((time - System.currentTimeMillis()) / 1000 <= 0 ) {
                            try (PreparedStatement state = getConnection().prepareStatement(
                                "DELETE FROM muted_players WHERE name = ?")) {
                                state.setString(1, name);
                                state.executeUpdate();
                            }
                            return new String[]{name};
                        }
                        long formatedTime = (time - System.currentTimeMillis()) / 1000;
                        return new String[]{name, String.valueOf(formatedTime), reason, admin};
                    }
                    return new String[]{name};
                }
            }
        }
        return new String[]{name};
    }
}

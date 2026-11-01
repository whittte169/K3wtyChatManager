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
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS banned_players_by_xuid (xuid TEXT PRIMARY KEY)");
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
    default void banPlayerByXUID(String xuid) throws SQLException {
        if (xuid == null || xuid.isEmpty()) return;
        try (PreparedStatement statement = getConnection().prepareStatement(
                "INSERT OR IGNORE INTO banned_players_by_xuid (xuid) VALUES (?)")) {
            statement.setString(1, xuid);
            statement.executeUpdate();
        }
    }

    /**
     * Unban player by XUID
     * @param xuid {@link String}
     * @throws SQLException
     */
    default void unBanPlayerByXUID(String xuid) throws SQLException {
        if (xuid == null || xuid.isEmpty()) return;
        try (PreparedStatement statement = getConnection().prepareStatement(
                "DELETE FROM banned_players_by_xuid WHERE xuid = ?")) {
            statement.setString(1, xuid);
            statement.executeUpdate();
        }
    }

    /**
     * 
     * @param xuid {@link String}
     * @return is player banned by XUID {@link Boolean}
     * @throws SQLException
     */
    default boolean isPlayerBannedByXUID(String xuid) throws SQLException {
        if (xuid == null || xuid.isEmpty()) return false;
        try (PreparedStatement statement = getConnection().prepareStatement(
                "SELECT 1 FROM banned_players_by_xuid WHERE xuid = ?")) {
            statement.setString(1, xuid);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }
}

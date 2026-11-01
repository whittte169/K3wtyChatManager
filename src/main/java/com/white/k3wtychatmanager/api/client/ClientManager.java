package com.white.k3wtychatmanager.api.client;

import cn.nukkit.Player;
import cn.nukkit.network.protocol.types.InputMode;

import java.sql.Connection;
import java.sql.SQLException;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * 
 * ClientManager
 * library for method with Player
 * also a task handler involving the Player's device
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * @version 1.0.1
 * 
 */
public class ClientManager implements ClientDatabase {
    private static final ClientManager INSTANCE = new ClientManager();
    private String folder;
    private Connection database;

    private ClientManager() {}

    /**
     * Get an instance of ClientManager
     * <h4>This method cannot return null</h4>
     * @return INSTANCE {@link ClientManager}
     */
    public static @NonNull ClientManager getInstance() throws IllegalStateException {
        if (INSTANCE == null) throw new IllegalStateException("ClientManager is not initialized.");
        return INSTANCE;
    }

    /**
     * Load a data folder to database
     * @param loadedFolder {@link String}
     */
    public void loadDatabaseFolder(String loadedFolder) {
        if (loadedFolder == null || isDatabaseOpened()) return;
        this.folder = loadedFolder;
        try {
            this.database = openDatabase(this.folder);
        } catch (SQLException e) {}
    }

    /**
     * Get ClientDatabase database
     * @return connection {@link Connection}
     */
    @Override
    public Connection getConnection() {
        return this.database;
    }

    /**
     * Get player's device OS
     * @param player {@link Player}
     * @return player's device OS {@link DeviceOS}
     */
    public DeviceOS getPlayerDeviceOs(Player player) {
        if (player == null) return DeviceOS.UNDEFINED;
        int index = player.getLoginChainData().getDeviceOS();
        return DeviceOS.fromOrdinal(index);
    }

    /**
     * Get player's input mode
     * @param player {@link Player}
     * @return player's current input mode
     */
    public InputMode getPlayerInputMode(Player player) {
        if (player == null) return InputMode.UNDEFINED;
        int index = player.getLoginChainData().getCurrentInputMode();
        return InputMode.fromOrdinal(index);
    }

    /**
     * Get player's XUID
     * @param player {@link Player}
     * @return xuid {@link String}
     */
    public String getPlayerXUID(Player player) {
        if (player == null) return "";
        return player.getLoginChainData().getXUID();
    }
}
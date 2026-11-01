package com.white.k3wtychatmanager;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.event.Listener;
import cn.nukkit.utils.TextFormat;

import org.checkerframework.checker.nullness.qual.NonNull;

import com.white.k3wtychatmanager.api.client.ClientManager;

/**
 * 
 * Main class of K3wtyChatManager
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * 
 */
public class Main extends PluginBase implements Listener, ClientManagerHandler, ChatManagerHandler {
    private static Main INSTANCE;
    public static @NonNull Main getInstance() throws IllegalStateException { 
        if (INSTANCE == null) throw new IllegalStateException("Main is not initialized.");
        return INSTANCE;
    }
    private ClientManager clientManager = ClientManager.getInstance();

    @Override 
    public Plugin getPlugin() {
        return this;
    }

    @Override 
    public void onLoad() {
        clientManager.loadDatabaseFolder(this.getDataFolder().toString());
    }

    @Override
    public void onEnable() {
        try {
            INSTANCE = this;
            reloadConfigLabels();
            this.getServer().getPluginManager().registerEvents(this, (Plugin) this);
            ResourcePackManager.register((Main) INSTANCE);
            CommandManager.getInstance().registerCommands(INSTANCE);
            updateNameTag();
            checkInputMode();
            this.getLogger().info("K3wtyChatManager has successfuly loaded!");
            this.getLogger().info(TextFormat.GOLD + "MIT License");
            this.getLogger().info("GitHub repository: " + TextFormat.GREEN + "https://github.com/whittte169/K3wtyChatManager");
        } catch (Exception e) {
            this.getLogger().error("Failed to load K3wtyChatManager! " + e.getMessage());
        }
    }

    @Override
    public void onDisable() {
        ResourcePackManager.unregister((Main) INSTANCE);
        clientManager.closeDatabase(true);
    }
}
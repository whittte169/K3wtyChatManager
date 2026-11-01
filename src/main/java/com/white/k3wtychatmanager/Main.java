package com.white.k3wtychatmanager;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.event.Listener;

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
    public Main getPlugin() {
        return this;
    }

    @Override 
    public void onLoad() {
        clientManager.loadDatabaseFolder(this.getDataFolder().toString());
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        saveDefaultConfig();
        reloadConfig();
        this.getServer().getPluginManager().registerEvents(this, (Plugin) this);
        ResourcePackManager.register((Main) INSTANCE);
        CommandManager.getInstance().registerCommands(INSTANCE);
    }

    @Override
    public void onDisable() {
        ResourcePackManager.unregister((Main) INSTANCE);
        clientManager.closeDatabase(true);
    }
}
package white;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.event.Listener;
import cn.nukkit.Player;
import cn.nukkit.Server;

/**
 * 
 * Main class of ChatManagerSystem
 * listening tasks from ChatManager class
 * @author whittte169
 * 
 */
public class Main extends PluginBase implements Listener {
    private DeviceManager deviceManager;
    private ChatManager chatManager;
    private static Main instance;
    public static Main getInstance() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        reloadConfig();
        this.deviceManager = new DeviceManager(this.getConfig());
        this.chatManager = new ChatManager(this.getConfig(), this.deviceManager);
        this.getServer().getPluginManager().registerEvents(this, (Plugin) this);
        this.getServer().getPluginManager().registerEvents(this.chatManager, (Plugin) this);
        this.getServer().getPluginManager().registerEvents(this.deviceManager, (Plugin) this);
        updatePlayerNameTag();
    }

    private void updatePlayerNameTag() {
        this.getServer().getScheduler().scheduleRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (Main.this.getConfig().getBoolean("nametag_enabled", true)) {
                    for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                        chatManager.playerUpdateNameTag(player);
                    }
                }
            }
        }, 20);
    }
}
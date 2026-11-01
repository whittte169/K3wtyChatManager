package com.white.k3wtychatmanager;

import cn.nukkit.resourcepacks.ZippedResourcePack;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.resourcepacks.ResourcePack;
import cn.nukkit.resourcepacks.loader.ResourcePackLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;

/**
 * 
 * ResourcePackManager
 * @author <a href="https://github.com/whittte169">whittte169</a>
 * 
 */
public class ResourcePackManager {
    private static final String PACK_NAME = "font_pack.zip";
    private static PackLoader loader;

    public static synchronized void register(Plugin plugin) {
        File dataFolder = plugin.getDataFolder();
        File packFile = new File(dataFolder, PACK_NAME);
        dataFolder.mkdirs();
        try (InputStream in = plugin.getResource(PACK_NAME)) {
            if (in == null) {
                plugin.getLogger().error("Resource pack not found in plugin jar: " + PACK_NAME);
                return;
            }
            Files.copy(in, packFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to extract resource pack: " + e.getMessage());
            return;
        }
        if (loader == null) {
            loader = new PackLoader(plugin);
            plugin.getServer().getResourcePackManager().registerPackLoader(loader);
        }
        loader.setPackFile(packFile);
        plugin.getServer().getResourcePackManager().reloadPacks();
    }

    public static synchronized void unregister(Plugin plugin) {
        if (loader != null) {
            loader.setPackFile(null);
            plugin.getServer().getResourcePackManager().reloadPacks();
        }
    }
    
    private static class PackLoader implements ResourcePackLoader {
        private final Plugin plugin;
        private volatile File packFile;

        PackLoader(Plugin plugin) {
            this.plugin = plugin;
        }

        void setPackFile(File packFile) {
            this.packFile = packFile;
        }

        @Override
        public List<ResourcePack> loadPacks() {
            File file = this.packFile;
            if (file == null || !file.isFile()) {
                return Collections.emptyList();
            }
            try {
                return Collections.singletonList(new ZippedResourcePack(file));
            } catch (RuntimeException e) {
                plugin.getLogger().error("Resource pack install error: " + e.getMessage());
                return Collections.emptyList();
            }
        }
    }
}
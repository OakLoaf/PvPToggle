package org.beaconmc.pvptoggle.storage;

import org.beaconmc.pvptoggle.PVPToggle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class YmlStorage implements Storage {
    private final PVPToggle plugin = PVPToggle.getInstance();
    private File dataFile;
    private YamlConfiguration config;
    private final ReentrantLock fileLock = new ReentrantLock();

    @Override
    public boolean hasPVPEnabled(UUID playerUUID) {
        ConfigurationSection playerSection = config.getConfigurationSection(playerUUID.toString());
        if (playerSection == null) return PVPToggle.configManager.getDefaultPVPMode();
        return playerSection.getBoolean("pvp-enabled");
    }

    @Override
    public void savePVPUser(UUID playerUUID, boolean pvpEnabled) {
        fileLock.lock();
        ConfigurationSection playerSection = config.createSection(playerUUID.toString());
        playerSection.set("pvp-enabled", pvpEnabled);
        try {
            config.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            fileLock.unlock();
        }
    }

    @Override
    public boolean init() {
        File dataFile = new File(plugin.getDataFolder(),"data.yml");
        try {
            if (dataFile.createNewFile()) plugin.getLogger().info("File Created: data.yml");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        this.dataFile = dataFile;
        config = YamlConfiguration.loadConfiguration(dataFile);
        return true;
    }
}

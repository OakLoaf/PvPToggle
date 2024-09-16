package org.lushplugins.pvptoggle.datamanager;

import org.lushplugins.pvptoggle.PvpTogglePlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.enchantedskies.EnchantedStorage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YmlStorage implements Storage<PvpUser> {
    private final PvpTogglePlugin plugin = PvpTogglePlugin.getInstance();
    private final File dataFolder = new File(plugin.getDataFolder(), "data");

    public YmlStorage() {
        // TODO: Remove in future
        dataUpdater();
    }

    @Override
    public PvpUser load(UUID uuid) {
        ConfigurationSection configurationSection = loadOrCreateFile(uuid);
        String name = configurationSection.getString("name");
        boolean pvpEnabled = configurationSection.getBoolean("pvp-enabled");
        return new PvpUser(uuid, name, pvpEnabled);
    }

    @Override
    public void save(PvpUser pvpUser) {
        YamlConfiguration yamlConfiguration = loadOrCreateFile(pvpUser.getUUID());

        String username = pvpUser.getUsername();
        if (username == null) username = "Error: Could not get username, will load when the player next joins";
        yamlConfiguration.set("name", username);
        yamlConfiguration.set("pvp-enabled", pvpUser.isPvpEnabled());
        File file = new File(dataFolder, pvpUser.getUUID().toString() + ".yml");
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public YamlConfiguration loadOrCreateFile(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        if (yamlConfiguration.getString("name") == null) {
            Player player = Bukkit.getPlayer(uuid);

            String username;
            if (player != null) username = player.getName();
            else username = "Error: Could not get username, will load when the player next joins";
            yamlConfiguration.set("name", username);
            yamlConfiguration.set("pvp-enabled", PvpTogglePlugin.getConfigManager().getDefaultPvpMode());
            try {
                yamlConfiguration.save(file);
            } catch (IOException err) {
                err.printStackTrace();
            }
        }
        return yamlConfiguration;
    }

    public void dataUpdater() {
        if (dataFolder.exists()) return;
        File file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) return;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            PvpUser pvpUser = new PvpUser(uuid, Bukkit.getOfflinePlayer(uuid).getName(), config.getBoolean(uuidStr + "pvp-enabled"));
            save(pvpUser);
        }

        File newFile = new File(plugin.getDataFolder(), "data.yml-outdated");
        boolean renameSuccess = file.renameTo(newFile);
        if (!renameSuccess) plugin.getLogger().severe("Failed to rename outdated data.yml file, this no longer works.");
    }
}

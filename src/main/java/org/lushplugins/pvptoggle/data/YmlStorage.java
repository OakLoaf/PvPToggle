package org.lushplugins.pvptoggle.data;

import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.enchantedskies.EnchantedStorage.Storage;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class YmlStorage implements Storage<PvPUser, UUID> {
    private final File dataFolder = new File(PvPToggle.getInstance().getDataFolder(), "data");

    @Override
    public PvPUser load(UUID uuid) {
        ConfigurationSection configurationSection = loadOrCreateFile(uuid);
        return new PvPUser(
            uuid,
            configurationSection.getString("name"),
            configurationSection.getBoolean("pvp-enabled")
        );
    }

    @Override
    public void save(PvPUser pvpUser) {
        YamlConfiguration yamlConfiguration = loadOrCreateFile(pvpUser.getUUID());

        String username = pvpUser.getUsername();
        if (username != null) {
            yamlConfiguration.set("name", username);
        }

        yamlConfiguration.set("pvp-enabled", pvpUser.isPvPEnabled());

        File file = new File(dataFolder, pvpUser.getUUID().toString() + ".yml");
        try {
            yamlConfiguration.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private YamlConfiguration loadOrCreateFile(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(file);
        if (yamlConfiguration.getString("name") == null) {
            Player player = Bukkit.getPlayer(uuid);

            String username = player != null ? player.getName() : "Error: Could not get username, will load when the player next joins";
            yamlConfiguration.set("name", username);
            yamlConfiguration.set("pvp-enabled", PvPToggle.getInstance().getConfigManager().getDefaultPvPState());
            try {
                yamlConfiguration.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return yamlConfiguration;
    }
}

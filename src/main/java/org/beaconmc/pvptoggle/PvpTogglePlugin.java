package org.beaconmc.pvptoggle;

import org.beaconmc.pvptoggle.apis.PlaceholderAPIHook;
import org.beaconmc.pvptoggle.datamanager.ConfigManager;
import org.beaconmc.pvptoggle.datamanager.CooldownManager;
import org.beaconmc.pvptoggle.datamanager.DataManager;
import org.beaconmc.pvptoggle.listeners.PlayerEvents;
import org.beaconmc.pvptoggle.listeners.PvpEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PvpTogglePlugin extends JavaPlugin {
    private static PvpTogglePlugin plugin;
    private static CooldownManager cooldownManager;
    private static ConfigManager configManager;
    private static DataManager dataManager;

    @Override
    public void onEnable() {
        plugin = this;
        cooldownManager = new CooldownManager();
        configManager = new ConfigManager();
        dataManager = new DataManager();

        Listener[] listeners = new Listener[] {
            new PlayerEvents(),
            new PvpEvents()
        };
        registerEvents(listeners);

        getCommand("pvp").setExecutor(new PvpCommand());

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("PlaceholderAPI") != null) new PlaceholderAPIHook().register();
        else getLogger().info("PlaceholderAPI plugin not found. Continuing without PlaceholderAPI.");
    }

    @Override
    public void onDisable() {
        dataManager.getIoHandler().disableIOHandler();
    }

    private void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public static PvpTogglePlugin getInstance() {
        return plugin;
    }

    public static CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static DataManager getDataManager() {
        return dataManager;
    }
}

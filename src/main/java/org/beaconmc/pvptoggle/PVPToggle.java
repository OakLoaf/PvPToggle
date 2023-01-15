package org.beaconmc.pvptoggle;

import org.beaconmc.pvptoggle.apis.PlaceholderAPIHook;
import org.beaconmc.pvptoggle.datamanager.ConfigManager;
import org.beaconmc.pvptoggle.datamanager.CooldownManager;
import org.beaconmc.pvptoggle.datamanager.DataManager;
import org.beaconmc.pvptoggle.listeners.PVPEvents;
import org.beaconmc.pvptoggle.listeners.PlayerEvents;
import org.beaconmc.pvptoggle.storage.Storage;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class PVPToggle extends JavaPlugin {
    private static PVPToggle plugin;
    public static CooldownManager cooldownManager;
    public static ConfigManager configManager;
    public static DataManager dataManager;

    private void setThreadIOName() {
        Storage.SERVICE.submit(() -> Thread.currentThread().setName("PVPToggle IO Thread"));
    }

    @Override
    public void onEnable() {
        plugin = this;
        setThreadIOName();
        cooldownManager = new CooldownManager();
        configManager = new ConfigManager();
        dataManager = new DataManager();
        dataManager.initAsync((successful) -> {
            Listener[] listeners = new Listener[] {
                new PlayerEvents(),
                new PVPEvents()
            };
            registerEvents(listeners);

            getCommand("pvp").setExecutor(new PVPCommand());

            PluginManager pluginManager = getServer().getPluginManager();
            if (pluginManager.getPlugin("PlaceholderAPI") != null) new PlaceholderAPIHook().register();
            else getLogger().info("PlaceholderAPI plugin not found. Continuing without PlaceholderAPI.");
        });
    }

    private void registerEvents(Listener[] listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public static PVPToggle getInstance() {
        return plugin;
    }
}

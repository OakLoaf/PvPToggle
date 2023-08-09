package me.dave.pvptoggle;

import me.dave.pvptoggle.hooks.custom.PlaceholderAPIHook;
import me.dave.pvptoggle.hooks.custom.WorldGuardHook;
import me.dave.pvptoggle.datamanager.ConfigManager;
import me.dave.pvptoggle.datamanager.CooldownManager;
import me.dave.pvptoggle.datamanager.DataManager;
import me.dave.pvptoggle.hooks.Hooks;
import me.dave.pvptoggle.listeners.PlayerEvents;
import me.dave.pvptoggle.listeners.PvpEvents;
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
        if (pluginManager.getPlugin("PlaceholderAPI") != null) Hooks.register("PlaceholderAPI", new PlaceholderAPIHook());
        if (pluginManager.getPlugin("WorldGuard") != null) Hooks.register("WorldGuard", new WorldGuardHook());
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

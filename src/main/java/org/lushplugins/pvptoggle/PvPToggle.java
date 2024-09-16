package org.lushplugins.pvptoggle;

import org.lushplugins.pvptoggle.hooks.Hook;
import org.lushplugins.pvptoggle.hooks.PlaceholderAPIHook;
import org.lushplugins.pvptoggle.hooks.WorldGuardHook;
import org.lushplugins.pvptoggle.config.ConfigManager;
import org.lushplugins.pvptoggle.datamanager.CooldownManager;
import org.lushplugins.pvptoggle.datamanager.DataManager;
import org.lushplugins.pvptoggle.listeners.PlayerListener;
import org.lushplugins.pvptoggle.listeners.PvPListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class PvPToggle extends JavaPlugin {
    private static PvPToggle plugin;
    private static CooldownManager cooldownManager;
    private static ConfigManager configManager;
    private static DataManager dataManager;
    private static final HashMap<String, Hook> hooks = new HashMap<>();

    @Override
    public void onEnable() {
//        plugin = this;
        cooldownManager = new CooldownManager();
        configManager = new ConfigManager();
        dataManager = new DataManager();

        Listener[] listeners = new Listener[] {
            new PlayerListener(),
            new PvPListener()
        };
        registerEvents(listeners);

        getCommand("pvp").setExecutor(new PvPCommand());

        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("PlaceholderAPI") != null) registerHook("PlaceholderAPI", new PlaceholderAPIHook());
    }

    @Override
    public void onLoad() {
        plugin = this;
        PluginManager pluginManager = getServer().getPluginManager();
        if (pluginManager.getPlugin("WorldGuard") != null) registerHook("WorldGuard", new WorldGuardHook());
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

    public static void registerHook(String hookName, Hook hook) {
        hooks.put(hookName, hook);
        hook.enable();
        PvPToggle.getInstance().getLogger().info(hookName + " hook has been registered.");
    }

    public static Hook getHook(String hookName) {
        return hooks.get(hookName);
    }


    public static PvPToggle getInstance() {
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

package me.dave.pvptoggle;

import me.dave.pvptoggle.hooks.Hook;
import me.dave.pvptoggle.hooks.PlaceholderAPIHook;
import me.dave.pvptoggle.hooks.WorldGuardHook;
import me.dave.pvptoggle.datamanager.ConfigManager;
import me.dave.pvptoggle.datamanager.CooldownManager;
import me.dave.pvptoggle.datamanager.DataManager;
import me.dave.pvptoggle.listeners.PlayerEvents;
import me.dave.pvptoggle.listeners.PvpEvents;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public final class PvpTogglePlugin extends JavaPlugin {
    private static PvpTogglePlugin plugin;
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
            new PlayerEvents(),
            new PvpEvents()
        };
        registerEvents(listeners);

        getCommand("pvp").setExecutor(new PvpCommand());

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
        PvpTogglePlugin.getInstance().getLogger().info(hookName + " hook has been registered.");
    }

    public static Hook getHook(String hookName) {
        return hooks.get(hookName);
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

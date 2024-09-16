package org.lushplugins.pvptoggle;

import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.lushlib.plugin.SpigotPlugin;
import org.lushplugins.pvptoggle.hooks.PlaceholderAPIHook;
import org.lushplugins.pvptoggle.hooks.WorldGuardHook;
import org.lushplugins.pvptoggle.config.ConfigManager;
import org.lushplugins.pvptoggle.data.CooldownManager;
import org.lushplugins.pvptoggle.data.DataManager;
import org.lushplugins.pvptoggle.listeners.PlayerListener;
import org.lushplugins.pvptoggle.listeners.PvPListener;

public final class PvPToggle extends SpigotPlugin {
    private static PvPToggle plugin;

    private CooldownManager cooldownManager;
    private ConfigManager configManager;
    private DataManager dataManager;

    @Override
    public void onLoad() {
        plugin = this;

        addHook("WorldGuard", () -> registerHook(new WorldGuardHook()));
    }

    @Override
    public void onEnable() {
        cooldownManager = new CooldownManager();

        configManager = new ConfigManager();
        configManager.reloadConfig();

        dataManager = new DataManager();
        dataManager.enable();

        new PlayerListener().registerListeners();
        new PvPListener().registerListeners();

        getCommand("pvp").setExecutor(new PvPCommand());

        addHook("PlaceholderAPI", () -> registerHook(new PlaceholderAPIHook()));
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.disable();
            dataManager = null;
        }
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public void registerHook(Hook hook) {
        hooks.put(hook.getId(), hook);
        hook.enable();
    }

    public static PvPToggle getInstance() {
        return plugin;
    }
}

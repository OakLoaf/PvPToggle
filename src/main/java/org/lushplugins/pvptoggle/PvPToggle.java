package org.lushplugins.pvptoggle;

import org.lushplugins.lushlib.LushLib;
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.lushlib.plugin.SpigotPlugin;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pvptoggle.command.PvPCommand;
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
    private Updater updater;

    @Override
    public void onLoad() {
        plugin = this;
        LushLib.getInstance().enable(this);

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

        if (configManager.isUpdaterEnabled()) {
            updater = new Updater.Builder(this)
                .spigot("107427")
                .checkSchedule(900)
                .notificationPermission("pvptoggle.update.notifications")
                .notify(true)
                .build();
        }
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

    public Updater getUpdater() {
        return updater;
    }

    public void registerHook(Hook hook) {
        hooks.put(hook.getId(), hook);
        hook.enable();
    }

    public static PvPToggle getInstance() {
        return plugin;
    }
}

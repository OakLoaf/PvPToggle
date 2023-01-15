package org.beaconmc.pvptoggle.datamanager;

import me.dave.chatcolorhandler.ChatColorHandler;
import org.beaconmc.pvptoggle.PVPToggle;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

public class ConfigManager {
    private final PVPToggle plugin = PVPToggle.getInstance();
    private FileConfiguration config;
    private final List<String> defaultWorldList;

    public ConfigManager() {
        plugin.saveDefaultConfig();
        reloadConfig();
        defaultWorldList = reloadWorldList();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    private List<String> reloadWorldList() {
        return config.getStringList("worlds");
    }

    // Config sections
    public boolean isPVPStateRemembered() {
        return config.getBoolean("remember-pvp-state");
    }

    public long getCommandCooldown() {
        return config.getLong("command-cooldown");
    }

    public int getPVPCooldown() {
        return config.getInt("pvp-cooldown");
    }

    public int getCommandWaitTime() {
        return config.getInt("command-wait");
    }

    public boolean getDefaultPVPMode() {
        return config.getBoolean("default-pvp");
    }

    public int getParticlesDisplayMode() {
        return config.getInt("particles");
    }

    public boolean isWorldEnabled(String worldName) {
        return !defaultWorldList.contains(worldName);
    }


    public String getPVPEnabledPlaceholder() {
        return config.getString("placeholder-api.pvp-enabled");
    }

    public String getPVPDisabledPlaceholder() {
        return config.getString("placeholder-api.pvp-disabled");
    }

    public String getLangMessage(String messageName, String parameter, boolean pvpState) {
        String message = config.getString("messages." + messageName.toUpperCase());
        if (message == null) return null;
        if (parameter != null) message = message.replaceAll("<parameter>", parameter);
        if (pvpState) message = message.replaceAll("<pvpstate>", "on");
        else message = message.replaceAll("<pvpstate>", "off");
        message = config.getString("messages.PREFIX") + message;
        message = ChatColorHandler.translateAlternateColorCodes(message);
        return message;
    }

    public void sendLangMessage(Player player, String messageName) {
        ChatColorHandler.sendMessage(player, getLangMessage(messageName, null, false));
    }

    public void sendLangMessage(Player player, String messageName, String parameter) {
        ChatColorHandler.sendMessage(player, getLangMessage(messageName, parameter, false));
    }

    public void sendLangMessage(Player player, String messageName, boolean pvpState) {
        ChatColorHandler.sendMessage(player, getLangMessage(messageName, null, pvpState));
    }

    public void sendLangMessage(Player player, String messageName, String parameter, boolean pvpState) {
        ChatColorHandler.sendMessage(player, getLangMessage(messageName, parameter, pvpState));
    }
}

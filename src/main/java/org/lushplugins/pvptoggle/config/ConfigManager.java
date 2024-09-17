package org.lushplugins.pvptoggle.config;

import org.bukkit.configuration.ConfigurationSection;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.hooks.WorldGuardHook;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ConfigManager {
    private static final Pattern OUTDATED_MESSAGE_NAME = Pattern.compile("[A-Z_]");

    private boolean rememberPvPState;
    private long commandCooldown;
    private long pvpCooldown;
    private int commandWaitTime;
    private boolean defaultPvPState;
    private int particlesDisplayMode;
    private List<String> ignoredWorlds = new ArrayList<>();
    private String placeholderPvPEnabled;
    private String placeholderPvPDisabled;
    private final ConcurrentHashMap<String, String> messages = new ConcurrentHashMap<>();

    public ConfigManager() {
        PvPToggle.getInstance().saveDefaultConfig();
    }

    public void reloadConfig() {
        PvPToggle plugin = PvPToggle.getInstance();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        rememberPvPState = config.getBoolean("remember-pvp-state", true);
        commandCooldown = config.getLong("command-cooldown", 0);
        pvpCooldown = config.getLong("pvp-cooldown", 0);
        commandWaitTime = config.getInt("command-wait", 0);
        defaultPvPState = config.getBoolean("default-pvp", true);
        particlesDisplayMode = config.getInt("particles", -1);

        ignoredWorlds = new ArrayList<>(config.getStringList("ignored-worlds"));
        if (config.contains("worlds")) {
            ignoredWorlds.addAll(config.getStringList("worlds"));
            plugin.getLogger().warning("Deprecated: The 'worlds' config option has been renamed to 'ignored-worlds' and is scheduled for removal");
        }

        placeholderPvPEnabled = config.getString("placeholder-api.pvp-enabled");
        placeholderPvPDisabled = config.getString("placeholder-api.pvp-disabled");

        messages.clear();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            messagesSection.getValues(false).forEach((key, value) -> {
                if (OUTDATED_MESSAGE_NAME.matcher(key).find()) {
                    plugin.getLogger().warning("Deprecated: The message '" + key + "' contains uppercase or underscore characters which are no longer used in message names and are scheduled for removal. Please note that message names and placeholders were changed in version 2 and will need adjusting");
                    key = key.toLowerCase()
                        .replace("_", "-")
                        .replace("others", "other");
                }

                messages.put(key, (String) value);
            });
        }
    }

    public boolean isPvPStateRemembered() {
        return rememberPvPState;
    }

    public long getCommandCooldown() {
        return commandCooldown;
    }

    public long getPvPCooldown() {
        return pvpCooldown;
    }

    public int getCommandWaitTime() {
        return commandWaitTime;
    }

    public boolean getDefaultPvPState() {
        return defaultPvPState;
    }

    public int getParticlesDisplayMode() {
        return particlesDisplayMode;
    }

    public boolean isLocationIgnored(World world, Location location) {
        if (isWorldIgnored(world.getName())) {
            return true;
        }

        if (PvPToggle.getInstance().getHook("WorldGuard").orElse(null) instanceof WorldGuardHook worldGuardHook) {
            return !worldGuardHook.isRegionEnabled(world, location);
        }

        return false;
    }

    public boolean isWorldIgnored(String worldName) {
        return ignoredWorlds.contains(worldName);
    }

    public String getPvPEnabledPlaceholder() {
        return placeholderPvPEnabled;
    }

    public String getPvPDisabledPlaceholder() {
        return placeholderPvPDisabled;
    }

    public String getMessage(String messageName) {
        String message = messages.get(messageName);
        if (message == null || message.isBlank()) {
            return null;
        }

        return message.replace("%prefix%", messages.getOrDefault("prefix", ""));
    }

    public void sendMessage(CommandSender sender, String messageName) {
        ChatColorHandler.sendMessage(sender, getMessage(messageName));
    }
}

package org.lushplugins.pvptoggle.datamanager;

import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    public final HashMap<UUID, Long> commandCooldown = new HashMap<>();
    public final HashMap<UUID, Long> pvpCooldown = new HashMap<>();

    public void setCooldown(Player player, String cooldownType) {
        switch (cooldownType.toUpperCase()) {
            case "COMMAND" -> commandCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case "PVP" -> pvpCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case "ALL" -> {
                commandCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
                pvpCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }
        }
    }

    public void removeCooldown(Player player, String cooldownType) {
        switch (cooldownType.toUpperCase()) {
            case "COMMAND" -> commandCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case "PVP" -> pvpCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case "ALL" -> {
                commandCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
                pvpCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }
        }
    }

    public long getCooldown(Player player) {
        long commandCooldown = getCooldown(player, "COMMAND");
        long pvpCooldown = getCooldown(player, "PVP");
        return Math.max(commandCooldown, pvpCooldown);
    }

    public long getCooldown(Player player, String cooldownType) {
        if (player.hasPermission("pvptoggle.bypasscooldown")) return -1;
        HashMap<UUID, Long> cooldownCheck = null;
        switch (cooldownType.toUpperCase()) {
            case "COMMAND" -> cooldownCheck = commandCooldown;
            case "PVP" -> cooldownCheck = pvpCooldown;
        }
        if (cooldownCheck == null) return -1;
        UUID playerUUID = player.getUniqueId();
        if (!cooldownCheck.containsKey(playerUUID)) return -1;
        long startTime = cooldownCheck.get(playerUUID);
        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long seconds = currentTime - startTime;

        switch (cooldownType.toUpperCase()) {
            case "COMMAND" -> {
                if (seconds >= PvPToggle.getConfigManager().getCommandCooldown()) {
                    removeCooldown(player, cooldownType);
                    return -1;
                } else return PvPToggle.getConfigManager().getCommandCooldown() - seconds;
            }
            case "PVP" -> {
                if (seconds >= PvPToggle.getConfigManager().getPvpCooldown()) {
                    removeCooldown(player, cooldownType);
                    return -1;
                } else return PvPToggle.getConfigManager().getPvpCooldown() - seconds;
            }
        }
        return seconds;
    }
}

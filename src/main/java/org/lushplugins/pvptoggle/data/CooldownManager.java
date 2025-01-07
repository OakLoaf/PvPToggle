package org.lushplugins.pvptoggle.data;

import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {
    public final HashMap<UUID, Long> commandCooldown = new HashMap<>();
    public final HashMap<UUID, Long> pvpCooldown = new HashMap<>();

    @Deprecated
    public void setCooldown(Player player, String cooldownType) {
        setCooldown(player, CooldownType.valueOf(cooldownType.toUpperCase()));
    }

    public void setCooldown(Player player, CooldownType cooldownType) {
        switch (cooldownType) {
            case COMMAND -> commandCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case PVP -> pvpCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case ALL -> {
                commandCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
                pvpCooldown.put(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }
        }
    }

    @Deprecated
    public void removeCooldown(Player player, String cooldownType) {
        removeCooldown(player, CooldownType.valueOf(cooldownType.toUpperCase()));
    }

    public void removeCooldown(Player player, CooldownType cooldownType) {
        switch (cooldownType) {
            case COMMAND -> commandCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case PVP -> pvpCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            case ALL -> {
                commandCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
                pvpCooldown.remove(player.getUniqueId(), LocalDateTime.now().toEpochSecond(ZoneOffset.UTC));
            }
        }
    }

    public long getCooldown(Player player) {
        long commandCooldown = getCooldown(player, CooldownType.COMMAND);
        long pvpCooldown = getCooldown(player, CooldownType.PVP);
        return Math.max(commandCooldown, pvpCooldown);
    }

    @Deprecated
    public long getCooldown(Player player, String cooldownType) {
        return getCooldown(player, CooldownType.valueOf(cooldownType.toUpperCase()));
    }

    public long getCooldown(Player player, CooldownType cooldownType) {
        if (player.hasPermission("pvptoggle.bypasscooldown")) return -1;
        HashMap<UUID, Long> cooldownCheck = null;
        switch (cooldownType) {
            case COMMAND -> cooldownCheck = commandCooldown;
            case PVP -> cooldownCheck = pvpCooldown;
        }
        if (cooldownCheck == null) return -1;
        UUID playerUUID = player.getUniqueId();
        if (!cooldownCheck.containsKey(playerUUID)) return -1;
        long startTime = cooldownCheck.get(playerUUID);
        long currentTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long seconds = currentTime - startTime;

        switch (cooldownType) {
            case COMMAND -> {
                if (seconds >= PvPToggle.getInstance().getConfigManager().getCommandCooldown()) {
                    removeCooldown(player, cooldownType);
                    return -1;
                } else {
                    return PvPToggle.getInstance().getConfigManager().getCommandCooldown() - seconds;
                }
            }
            case PVP -> {
                if (seconds >= PvPToggle.getInstance().getConfigManager().getPvPCooldown()) {
                    removeCooldown(player, cooldownType);
                    return -1;
                } else {
                    return PvPToggle.getInstance().getConfigManager().getPvPCooldown() - seconds;
                }
            }
        }
        return seconds;
    }

    public enum CooldownType {
        ALL,
        COMMAND,
        PVP
    }
}

package org.lushplugins.pvptoggle.data;

import org.lushplugins.lushlib.manager.Manager;
import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;
import org.enchantedskies.EnchantedStorage.IOHandler;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pvptoggle.config.ConfigManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager extends Manager {
    private IOHandler<PvPUser, UUID> ioHandler;
    private final HashMap<UUID, PvPUser> uuidToPvPUser = new HashMap<>();
    private final HashSet<UUID> pvpEnabledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        if (PvPToggle.getInstance().getConfigManager().isPvPStateRemembered()) {
            ioHandler = new IOHandler<>(new YmlStorage());
            ioHandler.enable();
        }
    }

    @Override
    public void onDisable() {
        if (ioHandler != null) {
            ioHandler.disable();
            ioHandler = null;
        }
    }

    public PvPUser getPvPUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        PvPUser pvpUser = uuidToPvPUser.get(uuid);
        if (pvpUser == null) {
            pvpUser = new PvPUser(uuid, player.getName(), PvPToggle.getInstance().getConfigManager().getDefaultPvPState());
            uuidToPvPUser.put(uuid, pvpUser);
        }

        return pvpUser;
    }

    public CompletableFuture<PvPUser> loadPvPUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        if (ioHandler == null) {
            PvPUser pvpUser = new PvPUser(uuid, player.getName(), PvPToggle.getInstance().getConfigManager().getDefaultPvPState());
            uuidToPvPUser.put(uuid, pvpUser);
            return CompletableFuture.completedFuture(pvpUser);
        }

        return ioHandler.loadData(uuid).thenApply(pvpUser -> {
            ConfigManager configManager = PvPToggle.getInstance().getConfigManager();
            if (!configManager.isPvPStateRemembered()) {
                pvpUser.setPvPEnabled(configManager.getDefaultPvPState());
            }

            uuidToPvPUser.put(uuid, pvpUser);
            if (pvpUser.isPvPEnabled()) {
                addPvPEnabledPlayer(uuid);
            }

            return pvpUser;
        });
    }

    public void unloadPvPUser(UUID uuid) {
        uuidToPvPUser.remove(uuid);
        removePvPEnabledPlayer(uuid);
    }

    public void savePvPUser(PvPUser user) {
        if (ioHandler != null) {
            ioHandler.saveData(user);
        }
    }

    public HashSet<UUID> getPvPEnabledPlayers() {
        return pvpEnabledPlayers;
    }

    public void addPvPEnabledPlayer(UUID uuid) {
        pvpEnabledPlayers.add(uuid);
    }

    public void removePvPEnabledPlayer(UUID uuid) {
        pvpEnabledPlayers.remove(uuid);
    }

    @Deprecated
    public IOHandler<PvPUser, UUID> getIoHandler() {
        return ioHandler;
    }
}

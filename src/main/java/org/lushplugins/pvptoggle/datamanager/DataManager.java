package org.lushplugins.pvptoggle.datamanager;

import org.lushplugins.pvptoggle.PvpTogglePlugin;
import org.bukkit.entity.Player;
import org.enchantedskies.EnchantedStorage.IOHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    private final IOHandler<PvpUser> ioHandler = new IOHandler<>(new YmlStorage());
    private final HashMap<UUID, PvpUser> uuidToPvpUser = new HashMap<>();
    private final HashSet<UUID> pvpEnabledPlayers = new HashSet<>();

    public PvpUser getPvpUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        PvpUser pvpUser = uuidToPvpUser.get(uuid);
        if (pvpUser == null) {
            pvpUser = new PvpUser(uuid, player.getName(), PvpTogglePlugin.getConfigManager().getDefaultPvpMode());
            uuidToPvpUser.put(uuid, pvpUser);
        }
        return pvpUser;
    }

    public CompletableFuture<PvpUser> loadPvpUser(UUID uuid) {
        return ioHandler.loadPlayer(uuid).thenApply(pvpUser -> {
            if (!PvpTogglePlugin.getConfigManager().isPvpStateRemembered()) pvpUser.setPvpEnabled(PvpTogglePlugin.getConfigManager().getDefaultPvpMode());
            uuidToPvpUser.put(uuid, pvpUser);
            if (pvpUser.isPvpEnabled()) addPvpEnabledPlayer(uuid);
            return pvpUser;
        });
    }

    public void unloadPvpUser(UUID uuid) {
        uuidToPvpUser.remove(uuid);
        removePvpEnabledPlayer(uuid);
    }

    public void savePvpUser(PvpUser user) {
        ioHandler.savePlayer(user);
    }

    public HashSet<UUID> getPvpEnabledPlayers() {
        return pvpEnabledPlayers;
    }

    public void addPvpEnabledPlayer(UUID uuid) {
        pvpEnabledPlayers.add(uuid);
    }

    public void removePvpEnabledPlayer(UUID uuid) {
        pvpEnabledPlayers.remove(uuid);
    }

    public IOHandler<PvpUser> getIoHandler() {
        return ioHandler;
    }
}

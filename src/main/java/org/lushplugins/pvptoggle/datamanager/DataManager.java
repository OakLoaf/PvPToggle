package org.lushplugins.pvptoggle.datamanager;

import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;
import org.enchantedskies.EnchantedStorage.IOHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DataManager {
    private final IOHandler<PvPUser> ioHandler = new IOHandler<>(new YmlStorage());
    private final HashMap<UUID, PvPUser> uuidToPvpUser = new HashMap<>();
    private final HashSet<UUID> pvpEnabledPlayers = new HashSet<>();

    public PvPUser getPvpUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        PvPUser pvpUser = uuidToPvpUser.get(uuid);
        if (pvpUser == null) {
            pvpUser = new PvPUser(uuid, player.getName(), PvPToggle.getConfigManager().getDefaultPvpMode());
            uuidToPvpUser.put(uuid, pvpUser);
        }
        return pvpUser;
    }

    public CompletableFuture<PvPUser> loadPvpUser(UUID uuid) {
        return ioHandler.loadPlayer(uuid).thenApply(pvPUser -> {
            if (!PvPToggle.getConfigManager().isPvpStateRemembered()) pvPUser.setPvpEnabled(PvPToggle.getConfigManager().getDefaultPvpMode());
            uuidToPvpUser.put(uuid, pvPUser);
            if (pvPUser.isPvpEnabled()) addPvpEnabledPlayer(uuid);
            return pvPUser;
        });
    }

    public void unloadPvpUser(UUID uuid) {
        uuidToPvpUser.remove(uuid);
        removePvpEnabledPlayer(uuid);
    }

    public void savePvpUser(PvPUser user) {
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

    public IOHandler<PvPUser> getIoHandler() {
        return ioHandler;
    }
}

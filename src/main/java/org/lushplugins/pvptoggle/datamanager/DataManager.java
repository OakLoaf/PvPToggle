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
    private final HashMap<UUID, PvPUser> uuidToPvPUser = new HashMap<>();
    private final HashSet<UUID> pvpEnabledPlayers = new HashSet<>();

    public PvPUser getPvPUser(@NotNull Player player) {
        UUID uuid = player.getUniqueId();

        PvPUser pvpUser = uuidToPvPUser.get(uuid);
        if (pvpUser == null) {
            pvpUser = new PvPUser(uuid, player.getName(), PvPToggle.getConfigManager().getDefaultPvPState());
            uuidToPvPUser.put(uuid, pvpUser);
        }
        return pvpUser;
    }

    public CompletableFuture<PvPUser> loadPvPUser(UUID uuid) {
        return ioHandler.loadPlayer(uuid).thenApply(pvPUser -> {
            if (!PvPToggle.getConfigManager().isPvPStateRemembered()) pvPUser.setPvPEnabled(PvPToggle.getConfigManager().getDefaultPvPState());
            uuidToPvPUser.put(uuid, pvPUser);
            if (pvPUser.isPvPEnabled()) addPvPEnabledPlayer(uuid);
            return pvPUser;
        });
    }

    public void unloadPvPUser(UUID uuid) {
        uuidToPvPUser.remove(uuid);
        removePvPEnabledPlayer(uuid);
    }

    public void savePvPUser(PvPUser user) {
        ioHandler.savePlayer(user);
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

    public IOHandler<PvPUser> getIoHandler() {
        return ioHandler;
    }
}

package org.beaconmc.pvptoggle.datamanager;

import org.beaconmc.pvptoggle.PVPToggle;
import org.beaconmc.pvptoggle.storage.Storage;
import org.beaconmc.pvptoggle.storage.YmlStorage;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class DataManager {
    private Storage storage;
    private final HashMap<UUID, Boolean> pvpPlayers = new HashMap<>();
    private final HashSet<UUID> pvpEnabledPlayers = new HashSet<>();

    public void initAsync(Consumer<Boolean> onComplete) {
        Storage.SERVICE.submit(() -> {
            storage = new YmlStorage();
            final boolean init = storage.init();
            new BukkitRunnable() {
                @Override
                public void run() {
                    onComplete.accept(init);
                }
            }.runTask(PVPToggle.getInstance());
        });
    }

    public boolean hasPVPEnabled(UUID uuid) {
        return pvpPlayers.getOrDefault(uuid, loadPVPUser(uuid));
    }

    public boolean loadPVPUser(UUID playerUUID) {
        boolean hasPVPEnabled = storage.hasPVPEnabled(playerUUID);
        if (!PVPToggle.configManager.isPVPStateRemembered()) hasPVPEnabled = PVPToggle.configManager.getDefaultPVPMode();
        updatePVPUser(playerUUID, hasPVPEnabled);
        return hasPVPEnabled;
    }

    public void savePVPUser(UUID playerUUID) {
        storage.savePVPUser(playerUUID, hasPVPEnabled(playerUUID));
    }

    public void updatePVPUser(UUID uuid, boolean hasPVPEnabled) {
        if (hasPVPEnabled) pvpEnabledPlayers.add(uuid);
        else pvpEnabledPlayers.remove(uuid);
        pvpPlayers.put(uuid, hasPVPEnabled);
        storage.savePVPUser(uuid, hasPVPEnabled);
    }

    public void removePVPUser(UUID playerUUID) {
        pvpPlayers.remove(playerUUID);
        pvpEnabledPlayers.remove(playerUUID);
    }

    public HashSet<UUID> getPVPEnabledPlayers() {
        return pvpEnabledPlayers;
    }
}

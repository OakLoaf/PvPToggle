package org.beaconmc.pvptoggle.storage;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface Storage {
    ExecutorService SERVICE = Executors.newSingleThreadExecutor();
    boolean hasPVPEnabled(UUID uuid);
    void savePVPUser(UUID uuid, boolean pvpEnabled);
    boolean init();
}

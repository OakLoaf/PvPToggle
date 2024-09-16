package org.lushplugins.pvptoggle.data;

import org.lushplugins.pvptoggle.PvPToggle;

import java.util.UUID;

public class PvPUser {
    private final UUID uuid;
    private String username;
    private boolean pvpEnabled;

    public PvPUser(UUID uuid, String username, boolean pvpEnabled) {
        this.uuid = uuid;
        this.username = username;
        this.pvpEnabled = pvpEnabled;
    }

    public void setUsername(String username) {
        this.username = username;
        PvPToggle.getInstance().getDataManager().savePvPUser(this);
    }

    public void setPvPEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;

        DataManager dataManager = PvPToggle.getInstance().getDataManager();
        if (pvpEnabled) {
            dataManager.addPvPEnabledPlayer(uuid);
        } else {
            dataManager.removePvPEnabledPlayer(uuid);
        }

        dataManager.savePvPUser(this);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isPvPEnabled() {
        return this.pvpEnabled;
    }
}

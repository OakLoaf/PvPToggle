package org.lushplugins.pvptoggle.datamanager;

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
        PvPToggle.getDataManager().savePvPUser(this);
    }

    public void setPvPEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;

        if (pvpEnabled) PvPToggle.getDataManager().addPvPEnabledPlayer(uuid);
        else PvPToggle.getDataManager().removePvPEnabledPlayer(uuid);

        PvPToggle.getDataManager().savePvPUser(this);
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

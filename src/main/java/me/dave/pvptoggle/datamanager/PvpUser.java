package me.dave.pvptoggle.datamanager;

import me.dave.pvptoggle.PvpTogglePlugin;

import java.util.UUID;

public class PvpUser {
    private final UUID uuid;
    private String username;
    private boolean pvpEnabled;

    public PvpUser(UUID uuid, String username, boolean pvpEnabled) {
        this.uuid = uuid;
        this.username = username;
        this.pvpEnabled = pvpEnabled;
    }

    public void setUsername(String username) {
        this.username = username;
        PvpTogglePlugin.getDataManager().savePvpUser(this);
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;

        if (pvpEnabled) PvpTogglePlugin.getDataManager().addPvpEnabledPlayer(uuid);
        else PvpTogglePlugin.getDataManager().removePvpEnabledPlayer(uuid);

        PvpTogglePlugin.getDataManager().savePvpUser(this);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isPvpEnabled() {
        return this.pvpEnabled;
    }
}

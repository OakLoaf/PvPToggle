package org.lushplugins.pvptoggle.data;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.pvptoggle.PvPToggle;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PvPUser {
    private final UUID uuid;
    private String username;
    private boolean pvpEnabled;
    private List<UUID> blockedUsers;

    @Deprecated
    public PvPUser(UUID uuid, String username, boolean pvpEnabled) {
        this(uuid, username, pvpEnabled, new ArrayList<>());
    }

    public PvPUser(UUID uuid, String username, boolean pvpEnabled, List<UUID> blockedUsers) {
        this.uuid = uuid;
        this.username = username;
        this.pvpEnabled = pvpEnabled;
        this.blockedUsers = blockedUsers;
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

    public String isPvPEnabledFriendly() {
        return this.pvpEnabled ? "enabled" : "disabled";
    }

    public List<UUID> getBlockedUsers() {
        return blockedUsers;
    }

    public boolean isBlockedUser(UUID uuid) {
        return blockedUsers.contains(uuid);
    }

    public void addBlockedUser(UUID uuid) {
        this.blockedUsers.add(uuid);
    }

    public void removeBlockedUser(UUID uuid) {
        this.blockedUsers.remove(uuid);
    }

    public void setBlockedUsers(List<UUID> blockedUsers) {
        this.blockedUsers = blockedUsers;
    }

    public boolean canPvPWith(@NotNull PvPUser user) {
        return pvpEnabled && user.isPvPEnabled() && !isBlockedUser(user.getUUID()) && !user.isBlockedUser(uuid);
    }

    public InteractionState canDamage(@NotNull PvPUser user) {
        if (user == this) {
            return InteractionState.SAME_PLAYER;
        } else if (!pvpEnabled) {
            return InteractionState.ORIGIN_PVP_DISABLED;
        } else if (!user.isPvPEnabled()) {
            return InteractionState.OTHER_PVP_DISABLED;
        } else if (!user.isBlockedUser(uuid)) {
            return InteractionState.ORIGIN_BLOCKED;
        } else if (!isBlockedUser(user.getUUID())) {
            return InteractionState.OTHER_BLOCKED;
        } else {
            return InteractionState.ALLOWED;
        }
    }

    public enum InteractionState {
        /**
         * PvP is allowed
         */
        ALLOWED,
        /**
         * The origin player has PvP disabled
         */
        ORIGIN_PVP_DISABLED,
        /**
         * The other player has the origin player blocked
         */
        ORIGIN_BLOCKED,
        /**
         * The other player has PvP disabled
         */
        OTHER_PVP_DISABLED,
        /**
         * The origin player has the other player blocked
         */
        OTHER_BLOCKED,
        /**
         * The origin player and other player are the same player
         */
        SAME_PLAYER
    }
}

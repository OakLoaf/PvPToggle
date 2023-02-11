package org.beaconmc.pvptoggle.apis;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.beaconmc.pvptoggle.PVPToggle;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final PVPToggle plugin = PVPToggle.getInstance();

    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "null";
        if (params.equals("pvp_state")) return PVPToggle.dataManager.hasPVPEnabled(player.getUniqueId()) ? PVPToggle.configManager.getPVPDisabledPlaceholder() : PVPToggle.configManager.getPVPEnabledPlaceholder();
        return "null";
    }

    public boolean persist() {
        return true;
    }

    public boolean canRegister() {
        return true;
    }

    public String getIdentifier() {
        return "pvptoggle";
    }

    public String getAuthor() {
        return this.plugin.getDescription().getAuthors().toString();
    }

    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }
}

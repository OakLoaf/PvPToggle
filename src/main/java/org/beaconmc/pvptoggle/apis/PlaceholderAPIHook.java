package org.beaconmc.pvptoggle.apis;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.beaconmc.pvptoggle.PvpTogglePlugin;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final PvpTogglePlugin plugin = PvpTogglePlugin.getInstance();

    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) return "null";
        if (params.equals("pvp_state")) return PvpTogglePlugin.getDataManager().getPvpUser(player.getUniqueId()).isPvpEnabled() ? PvpTogglePlugin.getConfigManager().getPvpDisabledPlaceholder() : PvpTogglePlugin.getConfigManager().getPvpEnabledPlaceholder();
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

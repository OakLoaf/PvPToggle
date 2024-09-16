package org.lushplugins.pvptoggle.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends Hook {
    private PvpToggleExpansion pvpToggleExpansion;

    @Override
    public void onEnable() {
        pvpToggleExpansion = new PvpToggleExpansion();
        pvpToggleExpansion.register();
    }

    @Override
    public void onDisable() {
        if (pvpToggleExpansion != null) pvpToggleExpansion.unregister();
    }

    public static class PvpToggleExpansion extends PlaceholderExpansion {

        public String onPlaceholderRequest(Player player, String params) {
            if (player == null) return "null";
            if (params.equals("pvp_state")) return PvPToggle.getDataManager().getPvpUser(player).isPvpEnabled() ? PvPToggle.getConfigManager().getPvpEnabledPlaceholder() : PvPToggle.getConfigManager().getPvpDisabledPlaceholder();
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
            return PvPToggle.getInstance().getDescription().getAuthors().toString();
        }

        public String getVersion() {
            return PvPToggle.getInstance().getDescription().getVersion();
        }
    }
}

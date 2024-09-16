package org.lushplugins.pvptoggle.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends Hook {
    private PvPToggleExpansion pvpToggleExpansion;

    public PlaceholderAPIHook() {
        super("PlaceholderAPI");
    }

    @Override
    public void onEnable() {
        pvpToggleExpansion = new PvPToggleExpansion();
        pvpToggleExpansion.register();
    }

    @Override
    public void onDisable() {
        if (pvpToggleExpansion != null) {
            pvpToggleExpansion.unregister();
        }
    }

    public static class PvPToggleExpansion extends PlaceholderExpansion {

        public String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) {
                return null;
            }

            if (params.equals("pvp_state")) {
                return PvPToggle.getInstance().getDataManager().getPvPUser(player).isPvPEnabled() ?
                    PvPToggle.getInstance().getConfigManager().getPvPEnabledPlaceholder() :
                    PvPToggle.getInstance().getConfigManager().getPvPDisabledPlaceholder();
            }

            return null;
        }

        public boolean persist() {
            return true;
        }

        public boolean canRegister() {
            return true;
        }

        public @NotNull String getIdentifier() {
            return "pvptoggle";
        }

        public @NotNull String getAuthor() {
            return PvPToggle.getInstance().getDescription().getAuthors().toString();
        }

        public @NotNull String getVersion() {
            return PvPToggle.getInstance().getDescription().getVersion();
        }
    }
}

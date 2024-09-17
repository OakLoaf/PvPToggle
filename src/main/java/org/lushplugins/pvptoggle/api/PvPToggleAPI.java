package org.lushplugins.pvptoggle.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pvptoggle.PvPToggle;

@SuppressWarnings("unused")
public class PvPToggleAPI {
    
    public static boolean isPvPEnabled(@NotNull Player player) {
        return PvPToggle.getInstance().getDataManager().getPvPUser(player).isPvPEnabled();
    }
}

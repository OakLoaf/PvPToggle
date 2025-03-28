package org.lushplugins.pvptoggle.api;

import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.Metadatable;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.data.PvPUser;

@SuppressWarnings("unused")
public class PvPToggleAPI {
    private static final String METADATA_KEY = "RESPECT_PVP_TOGGLE";
    private static final FixedMetadataValue METADATA_VALUE = new FixedMetadataValue(PvPToggle.getInstance(), null);

    public static PvPUser getPvPUser(@NotNull Player player) {
        return PvPToggle.getInstance().getDataManager().getPvPUser(player);
    }

    public static boolean isPvPEnabled(@NotNull Player player) {
        return getPvPUser(player).isPvPEnabled();
    }

    public static String getMetadataKey() {
        return METADATA_KEY;
    }

    public static void respectPvPToggle(@NotNull Metadatable metadatable) {
        metadatable.setMetadata(METADATA_KEY, METADATA_VALUE);
    }
}

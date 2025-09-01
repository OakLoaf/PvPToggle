package org.lushplugins.pvptoggle.placeholder;

import org.jetbrains.annotations.Nullable;
import org.lushplugins.placeholderhandler.annotation.Placeholder;
import org.lushplugins.placeholderhandler.annotation.SubPlaceholder;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.config.ConfigManager;
import org.lushplugins.pvptoggle.data.PvPUser;

@SuppressWarnings("unused")
@Placeholder("pvptoggle")
public class Placeholders {

    @SubPlaceholder("pvp_state")
    public String pvpState(@Nullable PvPUser user) {
        if (user == null) {
            return null;
        }

        ConfigManager configManager = PvPToggle.getInstance().getConfigManager();
        return user.isPvPEnabled() ? configManager.getPvPEnabledPlaceholder() : configManager.getPvPDisabledPlaceholder();
    }
}

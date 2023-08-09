package me.dave.pvptoggle.hooks.custom;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.dave.pvptoggle.PvpTogglePlugin;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class WorldGuardHook extends Hook {
    private static StateFlag DISABLE_PVP_TOGGLE_FLAG;

    @Override
    public void onEnable() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("disable-pvp-toggle-flag", false);
            registry.register(flag);
            DISABLE_PVP_TOGGLE_FLAG = flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get("disable-pvp-toggle-flag");
            if (existing instanceof StateFlag) {
                DISABLE_PVP_TOGGLE_FLAG = (StateFlag) existing;
            }
        }
    }

    @Override
    public void onDisable() {}

    public void checkPvpRegion(@NotNull Player player) {
        World world = player.getWorld();
        boolean playerHasPvpEnabled = PvpTogglePlugin.getDataManager().getPvpUser(player).isPvpEnabled();
        if (!isRegionEnabled(player) && !playerHasPvpEnabled) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(player, "PVP_REGION_CHANGE_DISABLED");
            return;
        }
        if (!PvpTogglePlugin.getConfigManager().isWorldEnabled(world.getName()) && isRegionEnabled(player) && playerHasPvpEnabled) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(player, "PVP_REGION_CHANGE_REQUIRED");
        }
    }

    public boolean isRegionEnabled(@NotNull Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        if (regions == null) return false;

        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(player.getLocation()).toVector().toBlockPoint());
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        StateFlag.State flag = set.queryValue(localPlayer, DISABLE_PVP_TOGGLE_FLAG);
        if (flag == null) return false;

        return flag.equals(StateFlag.State.ALLOW);
    }

    public boolean isRegionEnabled(@NotNull World world, @NotNull Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(world));
        if (regions == null) return false;

        ApplicableRegionSet set = regions.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        StateFlag.State flag = set.queryValue(null, DISABLE_PVP_TOGGLE_FLAG);
        if (flag == null) return false;

        return flag.equals(StateFlag.State.ALLOW);
    }
}

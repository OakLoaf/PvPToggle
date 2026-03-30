package org.lushplugins.pvptoggle.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;

public class WorldGuardHook {
    private static StateFlag PVP_TOGGLE_FLAG;

    public WorldGuardHook() {
        if (PVP_TOGGLE_FLAG == null) {
            PVP_TOGGLE_FLAG = registerStateFlag("pvp-toggle", true);
        }
    }

    public void checkPvPRegion(@NotNull Player player) {
        World world = player.getWorld();
        if (PvPToggle.getInstance().getConfigManager().isWorldIgnored(world.getName())) {
            return;
        }

        PvPToggle.getInstance().getConfigManager().sendMessage(player, isRegionEnabled(player) ? "pvp-region-enabled" : "pvp-region-disabled");
    }

    private boolean isFlagEnabled(@NotNull World world, @NotNull Location location, @NotNull StateFlag flag) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            return true;
        }

        ApplicableRegionSet set = regionManager.getApplicableRegions(BukkitAdapter.adapt(location).toVector().toBlockPoint());
        List<ProtectedRegion> regions = set.getRegions().stream().sorted(Comparator.comparing(ProtectedRegion::getPriority)).toList();
        if (regions.isEmpty()) {
            return true;
        }

        ProtectedRegion region = regions.getFirst();
        StateFlag.State state = region.getFlag(flag);
        return state == null || state.equals(StateFlag.State.ALLOW);
    }

    public boolean isRegionEnabled(@NotNull World world, @NotNull Location location) {
        return isFlagEnabled(world, location, PVP_TOGGLE_FLAG);
    }

    public boolean isRegionEnabled(@NotNull Player player) {
        return isRegionEnabled(player.getWorld(), player.getLocation());
    }

    private static StateFlag registerStateFlag(@NotNull String name, boolean def) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(name, def);
            registry.register(flag);
            return flag;
        } catch (FlagConflictException e) {
            Flag<?> existing = registry.get(name);
            return existing instanceof StateFlag flag ? flag : null;
        }
    }
}

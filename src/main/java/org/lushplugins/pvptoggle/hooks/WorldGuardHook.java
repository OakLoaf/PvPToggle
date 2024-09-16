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
import org.lushplugins.lushlib.hook.Hook;
import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.EventListener;
import java.util.List;

public class WorldGuardHook extends Hook implements EventListener {
    private static StateFlag PVP_TOGGLE_FLAG;

    public WorldGuardHook() {
        super("WorldGuard");
    }

    @Override
    public void onEnable() {
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

    public boolean isRegionEnabled(@NotNull Player player) {
        return isRegionEnabled(player.getWorld(), player.getLocation());
    }

    public boolean isRegionEnabled(@NotNull World world, @NotNull Location location) {
        return getRegionFlagState(world, location, PVP_TOGGLE_FLAG);
    }

    private boolean getRegionFlagState(@NotNull World world, @NotNull Location location, @NotNull StateFlag flag) {
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

        ProtectedRegion region = regions.get(0);
        StateFlag.State state = region.getFlag(flag);
        return state == null || state.equals(StateFlag.State.ALLOW);
    }

    private StateFlag registerStateFlag(@NotNull String name, boolean def) {
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

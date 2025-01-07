package org.lushplugins.pvptoggle.listeners;

import org.lushplugins.lushlib.listener.EventListener;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.hooks.WorldGuardHook;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements EventListener {
    private final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);

    public PlayerListener() {
        PvPToggle plugin = PvPToggle.getInstance();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PvPToggle.getInstance().getDataManager().loadPvPUser(player).thenAccept((ignored) -> Bukkit.getScheduler().runTask(plugin, () -> checkPvPWorld(player)));
        }

        Bukkit.getScheduler().runTaskTimer(PvPToggle.getInstance(), () -> {
            HashSet<UUID> pvpEnabledPlayers = PvPToggle.getInstance().getDataManager().getPvPEnabledPlayers();
            for (UUID playerUUID : pvpEnabledPlayers) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) displayParticles(player);
                else PvPToggle.getInstance().getDataManager().removePvPEnabledPlayer(playerUUID);
            }
        }, 0, 4);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PvPToggle.getInstance().getDataManager().loadPvPUser(player).thenAccept((pvpUser) -> Bukkit.getScheduler().runTask(PvPToggle.getInstance(), () -> {
            pvpUser.setUsername(player.getName());
            checkPvPWorld(player);
        }));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        PvPToggle.getInstance().getDataManager().unloadPvPUser(playerUUID);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        checkPvPWorld(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (PvPToggle.getInstance().getConfigManager().isWorldIgnored(event.getPlayer().getWorld().getName())) {
            return;
        }

        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        if (PvPToggle.getInstance().getHook("WorldGuard").orElse(null) instanceof WorldGuardHook worldGuardHook) {
            Player player = event.getPlayer();
            if (worldGuardHook.isRegionEnabled(player.getWorld(), event.getFrom()) != worldGuardHook.isRegionEnabled(player.getWorld(), event.getTo())) {
                Bukkit.getScheduler().runTaskLater(PvPToggle.getInstance(), () -> worldGuardHook.checkPvPRegion(player), 1);
            }
        }
    }

    private void checkPvPWorld(@NotNull Player player) {
        World world = player.getWorld();
        boolean playerHasPvPEnabled = PvPToggle.getInstance().getDataManager().getPvPUser(player).isPvPEnabled();
        if (!world.getPVP() && !playerHasPvPEnabled) {
            PvPToggle.getInstance().getConfigManager().sendMessage(player, "pvp-world-change-disabled");
            return;
        }

        if (PvPToggle.getInstance().getConfigManager().isWorldIgnored(world.getName()) && world.getPVP() && playerHasPvPEnabled) {
            PvPToggle.getInstance().getConfigManager().sendMessage(player, "pvp-world-change-required");
        }
    }

    private void displayParticles(@NotNull Player player) {
        int particlesMode = PvPToggle.getInstance().getConfigManager().getParticlesDisplayMode();
        switch (particlesMode) {
            case 0 -> player.getWorld().spawnParticle(Particle.DUST, player.getEyeLocation().add(0, 0.5, 0), 0, 0.0D, 1.0D, 0.0D, dustOptions);
            case 1 -> {
                Location pLoc = player.getLocation();
                HashSet<UUID> pvpEnabledPlayers = PvPToggle.getInstance().getDataManager().getPvPEnabledPlayers();
                List<Entity> nearbyEntities = player.getNearbyEntities(pLoc.getX(), pLoc.getY(), pLoc.getZ());
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer && pvpEnabledPlayers.contains(nearbyPlayer.getUniqueId())) {
                        nearbyPlayer.spawnParticle(Particle.DUST, player.getEyeLocation().add(0, 0.5, 0), 0, 0.0D, 1.0D, 0.0D, dustOptions);
                    }
                }
            }
        }
    }
}

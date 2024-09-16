package org.lushplugins.pvptoggle.listeners;

import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.hooks.WorldGuardHook;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerListener implements Listener {
    private final PvPToggle plugin;
    private final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);

    public PlayerListener() {
        plugin = PvPToggle.getInstance();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PvPToggle.getDataManager().loadPvpUser(player.getUniqueId()).thenAccept((ignored) -> new BukkitRunnable() {
                @Override
                public void run() {
                    checkPvpWorld(player);
                }
            }.runTask(plugin));
        }

        Bukkit.getScheduler().runTaskTimer(PvPToggle.getInstance(), () -> {
            HashSet<UUID> pvpEnabledPlayers = PvPToggle.getDataManager().getPvpEnabledPlayers();
            for (UUID playerUUID : pvpEnabledPlayers) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) displayParticles(player);
                else PvPToggle.getDataManager().removePvpEnabledPlayer(playerUUID);
            }
        }, 0, 4);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PvPToggle.getDataManager().loadPvpUser(player.getUniqueId()).thenAccept((pvpUser) -> new BukkitRunnable() {
            @Override
            public void run() {
                pvpUser.setUsername(player.getName());
                checkPvpWorld(player);
            }
        }.runTask(plugin));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        PvPToggle.getDataManager().unloadPvpUser(playerUUID);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        checkPvpWorld(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!PvPToggle.getConfigManager().isWorldEnabled(event.getPlayer().getWorld().getName())) return;
        if (event.getTo() == null || event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        if (PvPToggle.getHook("WorldGuard") instanceof WorldGuardHook wgHook) {
            Player player = event.getPlayer();
            if (wgHook.isRegionEnabled(player.getWorld(), event.getFrom()) != wgHook.isRegionEnabled(player.getWorld(), event.getTo())) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> wgHook.checkPvpRegion(player), 1);
            }
        }
    }

    private void checkPvpWorld(@NotNull Player player) {
        World world = player.getWorld();
        boolean playerHasPvpEnabled = PvPToggle.getDataManager().getPvpUser(player).isPvpEnabled();
        if (!world.getPVP() && !playerHasPvpEnabled) {
            PvPToggle.getConfigManager().sendLangMessage(player, "PVP_WORLD_CHANGE_DISABLED");
            return;
        }
        if (!PvPToggle.getConfigManager().isWorldEnabled(world.getName()) && world.getPVP() && playerHasPvpEnabled) {
            PvPToggle.getConfigManager().sendLangMessage(player, "PVP_WORLD_CHANGE_REQUIRED");
        }
    }

    private void displayParticles(@NotNull Player player) {
        int particlesMode = PvPToggle.getConfigManager().getParticlesDisplayMode();
        switch (particlesMode) {
            case 0 -> player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation().add(0, 0.5, 0), 0, 0.0D, 1.0D, 0.0D, dustOptions);
            case 1 -> {
                Location pLoc = player.getLocation();
                HashSet<UUID> pvpEnabledPlayers = PvPToggle.getDataManager().getPvpEnabledPlayers();
                List<Entity> nearbyEntities = player.getNearbyEntities(pLoc.getX(), pLoc.getY(), pLoc.getZ());
                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Player nearbyPlayer && pvpEnabledPlayers.contains(nearbyPlayer.getUniqueId())) {
                        nearbyPlayer.spawnParticle(Particle.REDSTONE, player.getEyeLocation().add(0, 0.5, 0), 0, 0.0D, 1.0D, 0.0D, dustOptions);
                    }
                }
            }
        }
    }
}

package me.dave.pvptoggle.listeners;

import me.dave.pvptoggle.PvpTogglePlugin;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerEvents implements Listener {
    private final PvpTogglePlugin plugin;
    private final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);

    public PlayerEvents() {
        plugin = PvpTogglePlugin.getInstance();

        for (Player player : Bukkit.getOnlinePlayers()) {
            PvpTogglePlugin.getDataManager().loadPvpUser(player.getUniqueId()).thenAccept((ignored) -> new BukkitRunnable() {
                @Override
                public void run() {
                    checkPVPWorld(player);
                }
            }.runTask(plugin));
        }

        Bukkit.getScheduler().runTaskTimer(PvpTogglePlugin.getInstance(), () -> {
            HashSet<UUID> pvpEnabledPlayers = PvpTogglePlugin.getDataManager().getPvpEnabledPlayers();
            for (UUID playerUUID : pvpEnabledPlayers) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null) displayParticles(player);
                else PvpTogglePlugin.getDataManager().removePvpEnabledPlayer(playerUUID);
            }
        }, 0, 4);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        PvpTogglePlugin.getDataManager().loadPvpUser(player.getUniqueId()).thenAccept((pvpUser) -> new BukkitRunnable() {
            @Override
            public void run() {
                pvpUser.setUsername(player.getName());
                checkPVPWorld(player);
            }
        }.runTask(plugin));
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        PvpTogglePlugin.getDataManager().unloadPvpUser(playerUUID);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        checkPVPWorld(player);
    }

    private void checkPVPWorld(@NotNull Player player) {
        World world = player.getWorld();
        boolean playerHasPvpEnabled = PvpTogglePlugin.getDataManager().getPvpUser(player).isPvpEnabled();
        if (!world.getPVP() && !playerHasPvpEnabled) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(player, "PVP_WORLD_CHANGE_DISABLED");
            return;
        }
        if (!PvpTogglePlugin.getConfigManager().isWorldEnabled(world.getName()) && world.getPVP() && playerHasPvpEnabled) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(player, "PVP_WORLD_CHANGE_REQUIRED");
        }
    }

    private void displayParticles(@NotNull Player player) {
        int particlesMode = PvpTogglePlugin.getConfigManager().getParticlesDisplayMode();
        switch (particlesMode) {
            case 0 -> player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation().add(0, 0.5, 0), 0, 0.0D, 1.0D, 0.0D, dustOptions);
            case 1 -> {
                Location pLoc = player.getLocation();
                HashSet<UUID> pvpEnabledPlayers = PvpTogglePlugin.getDataManager().getPvpEnabledPlayers();
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

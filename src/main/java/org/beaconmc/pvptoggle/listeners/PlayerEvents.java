package org.beaconmc.pvptoggle.listeners;

import org.beaconmc.pvptoggle.PVPToggle;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class PlayerEvents implements Listener {
    private final Particle.DustOptions dustOptions = new Particle.DustOptions(Color.RED, 1.0F);

    public PlayerEvents() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PVPToggle.dataManager.loadPVPUser(player.getUniqueId());
            checkPVPWorld(player);
        }

        Bukkit.getScheduler().runTaskTimer(PVPToggle.getInstance(), () -> {
            HashSet<UUID> pvpEnabledPlayers = PVPToggle.dataManager.getPVPEnabledPlayers();
            for (UUID playerUUID : pvpEnabledPlayers) {
                displayParticles(Bukkit.getPlayer(playerUUID));
            }
        }, 0, 4);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        PVPToggle.dataManager.loadPVPUser(playerUUID);
        checkPVPWorld(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        UUID playerUUID = event.getPlayer().getUniqueId();
        if (PVPToggle.configManager.isPVPStateRemembered()) PVPToggle.dataManager.savePVPUser(playerUUID);
        PVPToggle.dataManager.removePVPUser(playerUUID);
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        checkPVPWorld(player);
    }

    private void checkPVPWorld(Player player) {
        UUID playerUUID = player.getUniqueId();
        World world = player.getWorld();
        boolean playerHasPVPEnabled = PVPToggle.dataManager.hasPVPEnabled(playerUUID);
        if (!world.getPVP() && !playerHasPVPEnabled) {
            PVPToggle.configManager.sendLangMessage(player, "PVP_WORLD_CHANGE_DISABLED");
            return;
        }
        if (!PVPToggle.configManager.isWorldEnabled(world.getName()) && world.getPVP() && playerHasPVPEnabled) {
            PVPToggle.configManager.sendLangMessage(player, "PVP_WORLD_CHANGE_REQUIRED");
        }
    }

    private void displayParticles(Player player) {
        int particlesMode = PVPToggle.configManager.getParticlesDisplayMode();
        switch (particlesMode) {
            case 0 -> player.getWorld().spawnParticle(Particle.REDSTONE, player.getEyeLocation().add(0, 0.5, 0), 0, 0.0D, 1.0D, 0.0D, dustOptions);
            case 1 -> {
                Location pLoc = player.getLocation();
                HashSet<UUID> pvpEnabledPlayers = PVPToggle.dataManager.getPVPEnabledPlayers();
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

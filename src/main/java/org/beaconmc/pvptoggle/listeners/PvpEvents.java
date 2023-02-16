package org.beaconmc.pvptoggle.listeners;

import org.beaconmc.pvptoggle.PvpTogglePlugin;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.Iterator;

public class PvpEvents implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (!PvpTogglePlugin.getConfigManager().isWorldEnabled(event.getEntity().getWorld().getName())) return;
        if (!(event.getEntity() instanceof Player attacked)) return;
        boolean attackedHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(attacked.getUniqueId()).isPvpEnabled();

        Entity damageCause = event.getDamager();
        Player damager = null;
        if (damageCause instanceof Player eventDamager) {
            damager = eventDamager;
        } else if (damageCause instanceof Projectile projectile && projectile.getShooter() instanceof Player eventDamager) {
            damager = eventDamager;
        } else if (damageCause instanceof ThrownPotion potion && potion.getShooter() instanceof Player eventDamager) {
            damager = eventDamager;
        } else if (damageCause instanceof  LightningStrike lightning && lightning.getMetadata("TRIDENT").size() >= 1) {
            if (!attackedHasPVPEnabled) event.setCancelled(true);
            return;
        } else if (damageCause instanceof Firework) {
            if (!attackedHasPVPEnabled) event.setCancelled(true);
            return;
        }

        if (damager == null || damager == attacked) return;

        boolean damagerHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(damager.getUniqueId()).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            event.setCancelled(true);
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
        } else if (!attackedHasPVPEnabled) {
            event.setCancelled(true);
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
        } else {
            PvpTogglePlugin.getCooldownManager().setCooldown(damager, "PVP");
            PvpTogglePlugin.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlameArrow(EntityCombustByEntityEvent event) {
        if (!PvpTogglePlugin.getConfigManager().isWorldEnabled(event.getEntity().getWorld().getName())) return;
        if (!(event.getCombuster() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player damager) || !(event.getEntity() instanceof Player attacked)) return;
        boolean damagerHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(damager.getUniqueId()).isPvpEnabled();
        boolean attackedHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(attacked.getUniqueId()).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            event.setCancelled(true);
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
        } else if (!attackedHasPVPEnabled) {
            event.setCancelled(true);
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
        } else {
            PvpTogglePlugin.getCooldownManager().setCooldown(damager, "PVP");
            PvpTogglePlugin.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!PvpTogglePlugin.getConfigManager().isWorldEnabled(event.getEntity().getWorld().getName())) return;
        if (!(event.getPotion().getShooter() instanceof Player damager)) return;
        boolean damagerHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(damager.getUniqueId()).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (!(entity instanceof Player effected) || damager == effected) continue;
                event.setIntensity(effected, 0.0D);
            }
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
            return;
        }

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player attacked) || damager == attacked) continue;
            boolean attackedHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(attacked.getUniqueId()).isPvpEnabled();
            if (!attackedHasPVPEnabled) {
                event.setIntensity(attacked, 0.0D);
                PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
                continue;
            }
            PvpTogglePlugin.getCooldownManager().setCooldown(damager, "PVP");
            PvpTogglePlugin.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCloudEffects(AreaEffectCloudApplyEvent event) {
        if (!PvpTogglePlugin.getConfigManager().isWorldEnabled(event.getEntity().getWorld().getName())) return;
        if (!(event.getEntity().getSource() instanceof Player damager)) return;
        boolean damagerHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(damager.getUniqueId()).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            for (Iterator<LivingEntity> it = event.getAffectedEntities().iterator(); it.hasNext();) {
                LivingEntity entity = it.next();
                if (!(entity instanceof Player effected) || damager == effected) continue;
                it.remove();
            }
            return;
        }

        for (Iterator<LivingEntity> it = event.getAffectedEntities().iterator(); it.hasNext();) {
            LivingEntity entity = it.next();
            if (!(entity instanceof Player affected) || damager == affected) continue;
            boolean affectedHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(affected.getUniqueId()).isPvpEnabled();
            if (!affectedHasPVPEnabled) {
                it.remove();
                continue;
            }
            PvpTogglePlugin.getCooldownManager().setCooldown(damager, "PVP");
            PvpTogglePlugin.getCooldownManager().setCooldown(affected, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFishing(PlayerFishEvent event) {
        if (PvpTogglePlugin.getConfigManager().isWorldEnabled(event.getPlayer().getWorld().getName())) return;
        Player damager = event.getPlayer();
        if (!(event.getCaught() instanceof Player attacked)) return;
        if (damager.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD && damager.getInventory().getItemInOffHand().getType() != Material.FISHING_ROD) return;
        boolean damagerHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(damager.getUniqueId()).isPvpEnabled();
        boolean attackedHasPVPEnabled = PvpTogglePlugin.getDataManager().getPvpUser(attacked.getUniqueId()).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            event.setCancelled(true);
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
        } else if (!attackedHasPVPEnabled) {
            event.setCancelled(true);
            PvpTogglePlugin.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
        } else {
            PvpTogglePlugin.getCooldownManager().setCooldown(damager, "PVP");
            PvpTogglePlugin.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

//    @EventHandler(ignoreCancelled = true)
//    public void onLightningStrike(LightningStrikeEvent event) {
//        if (event.getCause() == LightningStrikeEvent.Cause.TRIDENT) {
//            event.getLightning().setMetadata("TRIDENT", new FixedMetadataValue(PVPToggle.getInstance(), event.getLightning().getLocation()));
//        }
//    }
}

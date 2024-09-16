package org.lushplugins.pvptoggle.listeners;

import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.datamanager.PvPUser;
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

public class PvPListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (!PvPToggle.getConfigManager().isPluginEnabledAt(event.getEntity().getWorld(), event.getEntity().getLocation())) return;
        if (!(event.getEntity() instanceof Player attacked)) return;
        PvPUser pvpUser = PvPToggle.getDataManager().getPvpUser(attacked);
        if (pvpUser == null) return;
        boolean attackedHasPVPEnabled = pvpUser.isPvpEnabled();

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

        boolean damagerHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(damager).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            event.setCancelled(true);
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
        } else if (!attackedHasPVPEnabled) {
            event.setCancelled(true);
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
        } else {
            PvPToggle.getCooldownManager().setCooldown(damager, "PVP");
            PvPToggle.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlameArrow(EntityCombustByEntityEvent event) {
        if (!PvPToggle.getConfigManager().isPluginEnabledAt(event.getEntity().getWorld(), event.getEntity().getLocation())) return;
        if (!(event.getCombuster() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player damager) || !(event.getEntity() instanceof Player attacked)) return;
        boolean damagerHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(damager).isPvpEnabled();
        boolean attackedHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(attacked).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            event.setCancelled(true);
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
        } else if (!attackedHasPVPEnabled) {
            event.setCancelled(true);
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
        } else {
            PvPToggle.getCooldownManager().setCooldown(damager, "PVP");
            PvPToggle.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (!PvPToggle.getConfigManager().isPluginEnabledAt(event.getEntity().getWorld(), event.getEntity().getLocation())) return;
        if (!(event.getPotion().getShooter() instanceof Player damager)) return;
        boolean damagerHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(damager).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (!(entity instanceof Player effected) || damager == effected) continue;
                event.setIntensity(effected, 0.0D);
            }
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
            return;
        }

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player attacked) || damager == attacked) continue;
            boolean attackedHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(attacked).isPvpEnabled();
            if (!attackedHasPVPEnabled) {
                event.setIntensity(attacked, 0.0D);
                PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
                continue;
            }
            PvPToggle.getCooldownManager().setCooldown(damager, "PVP");
            PvPToggle.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCloudEffects(AreaEffectCloudApplyEvent event) {
        if (!PvPToggle.getConfigManager().isPluginEnabledAt(event.getEntity().getWorld(), event.getEntity().getLocation())) return;
        if (!(event.getEntity().getSource() instanceof Player damager)) return;
        boolean damagerHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(damager).isPvpEnabled();
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
            boolean affectedHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(affected).isPvpEnabled();
            if (!affectedHasPVPEnabled) {
                it.remove();
                continue;
            }
            PvPToggle.getCooldownManager().setCooldown(damager, "PVP");
            PvPToggle.getCooldownManager().setCooldown(affected, "PVP");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFishing(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught == null) return;
        if (!PvPToggle.getConfigManager().isPluginEnabledAt(caught.getWorld(), caught.getLocation())) return;
        Player damager = event.getPlayer();
        if (!(caught instanceof Player attacked)) return;
        if (damager.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD && damager.getInventory().getItemInOffHand().getType() != Material.FISHING_ROD) return;
        boolean damagerHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(damager).isPvpEnabled();
        boolean attackedHasPVPEnabled = PvPToggle.getDataManager().getPvpUser(attacked).isPvpEnabled();
        if (!damagerHasPVPEnabled) {
            event.setCancelled(true);
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED");
        } else if (!attackedHasPVPEnabled) {
            event.setCancelled(true);
            PvPToggle.getConfigManager().sendLangMessage(damager, "PVP_DISABLED_OTHERS", attacked.getName());
        } else {
            PvPToggle.getCooldownManager().setCooldown(damager, "PVP");
            PvPToggle.getCooldownManager().setCooldown(attacked, "PVP");
        }
    }

//    @EventHandler(ignoreCancelled = true)
//    public void onLightningStrike(LightningStrikeEvent event) {
//        if (event.getCause() == LightningStrikeEvent.Cause.TRIDENT) {
//            event.getLightning().setMetadata("TRIDENT", new FixedMetadataValue(PVPToggle.getInstance(), event.getLightning().getLocation()));
//        }
//    }
}

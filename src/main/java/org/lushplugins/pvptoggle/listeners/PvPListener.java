package org.lushplugins.pvptoggle.listeners;

import org.bukkit.damage.DamageSource;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.api.PvPToggleAPI;
import org.lushplugins.pvptoggle.data.CooldownManager;
import org.lushplugins.pvptoggle.data.PvPUser;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

public class PvPListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
        if (PvPToggle.getInstance().getConfigManager().isLocationIgnored(event.getEntity().getWorld(), event.getEntity().getLocation())) {
            return;
        }

        if (!(event.getEntity() instanceof Player damagedPlayer)) {
            return;
        }

        PvPUser damagedUser = getPvPUserFromEntity(damagedPlayer);
        if (damagedUser == null) {
            return;
        }

        if (event.getDamager().hasMetadata(PvPToggleAPI.getMetadataKey())) {
            if (damagedUser.isPvPEnabled()) {
                event.setCancelled(true);
            }

            return;
        }

        DamageSource source = event.getDamageSource();
        Entity damagerEntity = source.getCausingEntity();
        boolean silent = false;

        if (damagerEntity == null && event.getDamager() instanceof LightningStrike lightning) {
            damagerEntity = lightning.getCausingPlayer();
            silent = true;
        }

        if (!(damagerEntity instanceof Player damagerPlayer) || damagerPlayer == damagedPlayer) {
            return;
        }

        allowOrCancel(damagerPlayer, damagedPlayer, event, silent);
    }

    @EventHandler(ignoreCancelled = true)
    public void onFlameArrow(EntityCombustByEntityEvent event) {
        if (PvPToggle.getInstance().getConfigManager().isLocationIgnored(event.getEntity().getWorld(), event.getEntity().getLocation())) {
            return;
        }

        if (!(event.getCombuster() instanceof Arrow arrow)) {
            return;
        }

        if (!(arrow.getShooter() instanceof Player damagerPlayer) || !(event.getEntity() instanceof Player damagedPlayer)) {
            return;
        }

        allowOrCancel(damagerPlayer, damagedPlayer, event);
    }

    // TODO: Update to support blocking
    @EventHandler(ignoreCancelled = true)
    public void onPotionSplash(PotionSplashEvent event) {
        if (PvPToggle.getInstance().getConfigManager().isLocationIgnored(event.getEntity().getWorld(), event.getEntity().getLocation())) {
            return;
        }

        if (!(event.getPotion().getShooter() instanceof Player damagerPlayer)) {
            return;
        }

        boolean damagerHasPvPEnabled = PvPToggle.getInstance().getDataManager().getPvPUser(damagerPlayer).isPvPEnabled();
        if (!damagerHasPvPEnabled) {
            for (LivingEntity entity : event.getAffectedEntities()) {
                if (!(entity instanceof Player effected) || damagerPlayer == effected) {
                    continue;
                }

                event.setIntensity(effected, 0.0D);
            }

            PvPToggle.getInstance().getConfigManager().sendMessage(damagerPlayer, "pvp-disabled");
            return;
        }

        for (LivingEntity entity : event.getAffectedEntities()) {
            if (!(entity instanceof Player damagedPlayer) || damagerPlayer == damagedPlayer) {
                continue;
            }

            PvPUser damaged = PvPToggle.getInstance().getDataManager().getPvPUser(damagedPlayer);
            if (!damaged.isPvPEnabled() ) {
                event.setIntensity(damagedPlayer, 0.0D);
                ChatColorHandler.sendMessage(damagerPlayer,
                    PvPToggle.getInstance().getConfigManager().getMessage("pvp-disabled-other")
                        .replace("%player%", damagedPlayer.getName()));
                continue;
            }

            PvPToggle.getInstance().getCooldownManager().setCooldown(damagerPlayer, CooldownManager.CooldownType.PVP);
            PvPToggle.getInstance().getCooldownManager().setCooldown(damagedPlayer, CooldownManager.CooldownType.PVP);
        }
    }

    // TODO: Update to support blocking
    @EventHandler(ignoreCancelled = true)
    public void onCloudEffects(AreaEffectCloudApplyEvent event) {
        if (PvPToggle.getInstance().getConfigManager().isLocationIgnored(event.getEntity().getWorld(), event.getEntity().getLocation())) {
            return;
        }

        if (!(event.getEntity().getSource() instanceof Player damagerPlayer)) {
            return;
        }

        PvPUser damager = PvPToggle.getInstance().getDataManager().getPvPUser(damagerPlayer);
        if (!damager.isPvPEnabled()) {
            event.getAffectedEntities().removeIf(damagedEntity -> {
                //noinspection CodeBlock2Expr
                return !(damagedEntity instanceof Player damagedPlayer) || damagerPlayer == damagedPlayer;
            });

            return;
        }

        event.getAffectedEntities().removeIf(entity -> {
            if (!(entity instanceof Player damagedPlayer) || damagedPlayer == damagerPlayer) {
                return false;
            }

            PvPUser damaged = PvPToggle.getInstance().getDataManager().getPvPUser(damagedPlayer);
            if (!damaged.isPvPEnabled()) {
                return true;
            }

            PvPToggle.getInstance().getCooldownManager().setCooldown(damagerPlayer, CooldownManager.CooldownType.PVP);
            PvPToggle.getInstance().getCooldownManager().setCooldown(damagedPlayer, CooldownManager.CooldownType.PVP);
            return false;
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerFishing(PlayerFishEvent event) {
        Entity caught = event.getCaught();
        if (caught == null || PvPToggle.getInstance().getConfigManager().isLocationIgnored(caught.getWorld(), caught.getLocation())) {
            return;
        }

        Player damagerPlayer = event.getPlayer();
        if (!(caught instanceof Player damagedPlayer)) {
            return;
        }

        if (damagerPlayer.getInventory().getItemInMainHand().getType() != Material.FISHING_ROD && damagerPlayer.getInventory().getItemInOffHand().getType() != Material.FISHING_ROD) {
            return;
        }

        allowOrCancel(damagerPlayer, damagedPlayer, event);
    }

    private void allowOrElse(Player damagerPlayer, Player damagedPlayer, boolean silent, Runnable orElse) {
        PvPUser damager = PvPToggle.getInstance().getDataManager().getPvPUser(damagerPlayer);
        PvPUser damaged = PvPToggle.getInstance().getDataManager().getPvPUser(damagedPlayer);
        if (damager == null || damaged == null) {
            return;
        }

        switch (damager.canDamage(damaged)) {
            case ORIGIN_PVP_DISABLED -> {
                orElse.run();

                if (!silent) {
                    PvPToggle.getInstance().getConfigManager().sendMessage(damagerPlayer, "pvp-disabled");
                }
            }
            case OTHER_BLOCKED -> {
                orElse.run();

                if (!silent) {
                    PvPToggle.getInstance().getConfigManager().sendMessage(damagerPlayer, "pvp-blocked");
                }
            }
            case OTHER_PVP_DISABLED -> {
                orElse.run();

                if (!silent) {
                    ChatColorHandler.sendMessage(damagerPlayer, PvPToggle.getInstance().getConfigManager().getMessage("pvp-disabled-other")
                        .replace("%player%", damagedPlayer.getName()));
                }
            }
            case ORIGIN_BLOCKED -> {
                orElse.run();

                if (!silent) {
                    ChatColorHandler.sendMessage(damagerPlayer, PvPToggle.getInstance().getConfigManager().getMessage("pvp-blocked-other")
                        .replace("%player%", damagedPlayer.getName()));
                }
            }
            default -> {
                PvPToggle.getInstance().getCooldownManager().setCooldown(damagerPlayer, CooldownManager.CooldownType.PVP);
                PvPToggle.getInstance().getCooldownManager().setCooldown(damagedPlayer, CooldownManager.CooldownType.PVP);
            }
        }
    }

    private void allowOrCancel(Player damagerPlayer, Player damagedPlayer, Cancellable event, boolean silent) {
        allowOrElse(damagerPlayer, damagedPlayer, silent, () -> event.setCancelled(true));
    }

    private void allowOrCancel(Player damagerPlayer, Player damagedPlayer, Cancellable event) {
        allowOrCancel(damagerPlayer, damagedPlayer, event, false);
    }

    private @Nullable PvPUser getPvPUserFromEntity(@NotNull Entity entity) {
        if (entity instanceof Player attackedPlayer) {
            return PvPToggle.getInstance().getDataManager().getPvPUser(attackedPlayer);
        } else {
            return null;
        }
    }
}

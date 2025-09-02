package org.lushplugins.pvptoggle.command;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.data.CooldownManager;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.HashSet;
import java.util.UUID;

@SuppressWarnings("unused")
@Command("pvp")
public class ToggleCommand {
    private final HashSet<UUID> processing = new HashSet<>();

    public String toggle(BukkitCommandActor actor, boolean newState, @Optional Player target) {
        int timeTillExecute;
        if (target != null) {
            if (!actor.sender().hasPermission("pvptoggle.admin.others")) {
                return PvPToggle.getInstance().getConfigManager().getMessage("no-permission");
            }

            timeTillExecute = 0;
        } else {
            target = actor.requirePlayer();

            if (!target.hasPermission("pvptoggle.bypasscooldown")) {
                if (processing.contains(target.getUniqueId())) {
                    return PvPToggle.getInstance().getConfigManager().getMessage("command-running");
                }

                long cooldown = PvPToggle.getInstance().getCooldownManager().getCooldown(target);
                if (cooldown >= 0) {
                    return PvPToggle.getInstance().getConfigManager().getMessage("pvp-cooldown")
                        .replace("%seconds%", String.valueOf(cooldown));
                }

                timeTillExecute = PvPToggle.getInstance().getConfigManager().getCommandWaitTime();
            } else {
                timeTillExecute = 0;
            }
        }

        UUID targetUUID = target.getUniqueId();
        if (timeTillExecute > 0) {
            processing.add(targetUUID);
            ChatColorHandler.sendMessage(target, PvPToggle.getInstance().getConfigManager().getMessage("command-timer")
                .replace("%seconds%", String.valueOf(timeTillExecute)));
        }

        Player finalTarget = target;
        Bukkit.getScheduler().runTaskLater(PvPToggle.getInstance(), () -> {
            PvPToggle.getInstance().getDataManager().getPvPUser(finalTarget).setPvPEnabled(newState);
            PvPToggle.getInstance().getCooldownManager().setCooldown(finalTarget, CooldownManager.CooldownType.COMMAND);
            processing.remove(targetUUID);

            if (newState) {
                PvPToggle.getInstance().getConfigManager().sendMessage(finalTarget, "pvp-state-enabled");
            } else {
                PvPToggle.getInstance().getConfigManager().sendMessage(finalTarget, "pvp-state-disabled");
            }

            if (actor.asPlayer() != finalTarget) {
                ChatColorHandler.sendMessage(actor.sender(), PvPToggle.getInstance().getConfigManager().getMessage("pvp-state-changed-other")
                    .replace("%player%", finalTarget.getName())
                    .replace("%pvp_state%", String.valueOf(newState)));
            }
        }, timeTillExecute * 20L);
        return null;
    }

    @Subcommand("toggle")
    // TODO: Permission lock `target` tab-completions
    public String toggle(BukkitCommandActor actor, @Optional Player target) {
        boolean newState = !PvPToggle.getInstance().getDataManager().getPvPUser(target != null ? target : actor.requirePlayer()).isPvPEnabled();
        return toggle(actor, newState, target);
    }

    @Subcommand("on")
    // TODO: Permission lock `target` tab-completions
    public String on(BukkitCommandActor actor, @Optional Player target) {
        return toggle(actor, true, target);
    }

    @Subcommand("off")
    // TODO: Permission lock `target` tab-completions
    public String off(BukkitCommandActor actor, @Optional Player target) {
        return toggle(actor, false, target);
    }
}

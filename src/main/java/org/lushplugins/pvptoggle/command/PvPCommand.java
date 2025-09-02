package org.lushplugins.pvptoggle.command;

import org.bukkit.plugin.PluginDescriptionFile;
import org.lushplugins.pvptoggle.PvPToggle;
import org.bukkit.entity.Player;
import org.lushplugins.pvptoggle.data.PvPUser;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@SuppressWarnings("unused")
@Command("pvp")
public class PvPCommand {

    @Command("pvp")
    public String pvp(BukkitCommandActor actor) {
        return status(actor, null);
    }

    @Subcommand("status")
    public String status(BukkitCommandActor actor, @Optional Player target) {
        String messageKey;
        if (target != null) {
            if (!actor.sender().hasPermission("pvptoggle.admin.others")) {
                return PvPToggle.getInstance().getConfigManager().getMessage("no-permission");
            }

            messageKey = actor.asPlayer() != target ? "pvp-status-other" : "pvp-status";
        } else {
            target = actor.requirePlayer();
            messageKey = "pvp-status";
        }

        return PvPToggle.getInstance().getConfigManager().getMessage(messageKey)
                .replace("%player%", target.getName())
                .replace("%pvp_state%", String.valueOf(PvPToggle.getInstance().getDataManager().getPvPUser(target).isPvPEnabledFriendly()));
    }

    @Subcommand("block")
    @CommandPermission("pvptoggle.block")
    public String block(BukkitCommandActor actor, Player target) {
        Player player = actor.requirePlayer();
        if (target == player) {
            return PvPToggle.getInstance().getConfigManager().getMessage("unknown-player")
                .replace("%player%", target.getName());
        }

        PvPUser pvpUser = PvPToggle.getInstance().getDataManager().getPvPUser(player);
        if (pvpUser.hasBlockedUser(target.getUniqueId())) {
            return PvPToggle.getInstance().getConfigManager().getMessage("already-blocked")
                .replace("%player%", target.getName());
        }

        pvpUser.addBlockedUser(target.getUniqueId());
        return PvPToggle.getInstance().getConfigManager().getMessage("blocked-player")
            .replace("%player%", target.getName());
    }

    @Subcommand("unblock")
    @CommandPermission("pvptoggle.block")
    public String unblock(BukkitCommandActor actor, Player target) {
        Player player = actor.requirePlayer();
        if (target == player) {
            return PvPToggle.getInstance().getConfigManager().getMessage("unknown-player")
                .replace("%player%", target.getName());
        }

        PvPUser pvpUser = PvPToggle.getInstance().getDataManager().getPvPUser(player);
        if (!pvpUser.hasBlockedUser(target.getUniqueId())) {
            return PvPToggle.getInstance().getConfigManager().getMessage("not-blocked")
                .replace("%player%", target.getName());
        }

        pvpUser.removeBlockedUser(target.getUniqueId());
        return PvPToggle.getInstance().getConfigManager().getMessage("unblocked-player")
            .replace("%player%", target.getName());
    }

    @Subcommand("reload")
    @CommandPermission("pvptoggle.admin.reload")
    public String reload() {
        PvPToggle.getInstance().getConfigManager().reloadConfig();
        return PvPToggle.getInstance().getConfigManager().getMessage("reload");
    }

    @Subcommand("version")
    @CommandPermission("pvptoggle.version")
    public String version() {
        PluginDescriptionFile description = PvPToggle.getInstance().getDescription();
        return "&#a8e1ffYou are currently running &#58b1e0%s &#a8e1ffversion &#58b1e0%s"
            .formatted(description.getName(), description.getVersion());
    }
}

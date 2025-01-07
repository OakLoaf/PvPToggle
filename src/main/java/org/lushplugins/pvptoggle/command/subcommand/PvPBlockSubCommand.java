package org.lushplugins.pvptoggle.command.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.data.PvPUser;

// TODO: Update classes from "...SubCommand" to "...Command"
public class PvPBlockSubCommand extends SubCommand {

    public PvPBlockSubCommand() {
        super("block");
        addRequiredPermission("pvptoggle.block");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }

        if (args.length != 1) {
            ChatColorHandler.sendMessage(player, PvPToggle.getInstance().getConfigManager().getMessage("incorrect-usage")
                .replace("%command_usage%", "/pvp block <player>"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            ChatColorHandler.sendMessage(sender,
                PvPToggle.getInstance().getConfigManager().getMessage("unknown-player")
                    .replace("%player%", targetName));
            return true;
        }

        PvPUser pvpUser = PvPToggle.getInstance().getDataManager().getPvPUser(player);
        if (pvpUser.isBlockedUser(player.getUniqueId())) {
            ChatColorHandler.sendMessage(player, PvPToggle.getInstance().getConfigManager().getMessage("already-blocked")
                .replace("%player%", targetName));
            return true;
        }

        pvpUser.addBlockedUser(player.getUniqueId());
        return true;
    }
}

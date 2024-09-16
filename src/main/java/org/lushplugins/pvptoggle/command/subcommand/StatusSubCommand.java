package org.lushplugins.pvptoggle.command.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.PvPToggle;

import java.util.List;

public class StatusSubCommand extends SubCommand {

    public StatusSubCommand() {
        super("status");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        Player target;
        if (args.length == 0) {
            if (sender instanceof Player player) {
                target = player;
            } else {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }
        } else {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvPToggle.getInstance().getConfigManager().sendMessage(sender, "no-permission");
                return true;
            }

            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                ChatColorHandler.sendMessage(sender,
                    PvPToggle.getInstance().getConfigManager().getMessage("unknown-player")
                        .replace("%player%", args[1]));
                return true;
            }
        }

        ChatColorHandler.sendMessage(sender,
            PvPToggle.getInstance().getConfigManager().getMessage(sender == target ? "pvp-status" : "pvp-status-other")
                .replace("%player%", target.getName())
                .replace("%pvp-state%", String.valueOf(PvPToggle.getInstance().getDataManager().getPvPUser(target).isPvPEnabled())));

        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }

        return null;
    }
}

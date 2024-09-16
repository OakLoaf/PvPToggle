package org.lushplugins.pvptoggle.command.subcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.pvptoggle.PvPToggle;

public class HelpSubCommand extends SubCommand {

    public HelpSubCommand() {
        super("help");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        PvPToggle.getInstance().getConfigManager().sendMessage(sender, "help-header");
        PvPToggle.getInstance().getConfigManager().sendMessage(sender, "help-general-usage");

        if (sender.hasPermission("pvptoggle.others")) {
            PvPToggle.getInstance().getConfigManager().sendMessage(sender, "help-view-other");
        }
        if (sender.hasPermission("pvptoggle.admin.others")) {
            PvPToggle.getInstance().getConfigManager().sendMessage(sender, "help-set-other");
        }

        return true;
    }
}

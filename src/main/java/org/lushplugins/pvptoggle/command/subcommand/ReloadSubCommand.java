package org.lushplugins.pvptoggle.command.subcommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.pvptoggle.PvPToggle;

public class ReloadSubCommand extends SubCommand {

    public ReloadSubCommand() {
        super("reload");
        addRequiredPermission("pvptoggle.admin.reload");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        PvPToggle.getInstance().getConfigManager().reloadConfig();
        PvPToggle.getInstance().getConfigManager().sendMessage(sender, "reload");
        return true;
    }
}

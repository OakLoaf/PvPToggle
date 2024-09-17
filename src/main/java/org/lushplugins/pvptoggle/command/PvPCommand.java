package org.lushplugins.pvptoggle.command;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.Command;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.PvPToggle;
import org.lushplugins.pvptoggle.command.subcommand.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PvPCommand extends Command {

    public PvPCommand() {
        super("pvp");
        addSubCommand(new ReloadSubCommand());
        addSubCommand(new StatusSubCommand());
        addSubCommand(new ToggleSubCommand("on"));
        addSubCommand(new ToggleSubCommand("off"));
        addSubCommand(new ToggleSubCommand("toggle"));
        addSubCommand(new VersionSubCommand(PvPToggle.getInstance()));

        if (PvPToggle.getInstance().getConfigManager().isUpdaterEnabled()) {
            addSubCommand(new UpdateSubCommand());
        }
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }

        ChatColorHandler.sendMessage(player,
            PvPToggle.getInstance().getConfigManager().getMessage("pvp-status")
                .replace("%pvp_state%", String.valueOf(PvPToggle.getInstance().getDataManager().getPvPUser(player).isPvPEnabledFriendly())));

        return true;
    }
}

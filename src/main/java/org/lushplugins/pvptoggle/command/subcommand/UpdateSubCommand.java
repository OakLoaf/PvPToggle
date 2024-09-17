package org.lushplugins.pvptoggle.command.subcommand;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pvptoggle.PvPToggle;

public class UpdateSubCommand extends SubCommand {

    public UpdateSubCommand() {
        super("update");
        addRequiredPermission("pvptoggle.update");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        Updater updater = PvPToggle.getInstance().getUpdater();
        if (updater == null) {
            ChatColorHandler.sendMessage(sender, "&#ff6969Updater is currently disabled!");
            return true;
        }

        if (updater.isAlreadyDownloaded() || !updater.isUpdateAvailable()) {
            ChatColorHandler.sendMessage(sender, "&#ff6969It looks like there is no new update available!");
            return true;
        }

        updater.attemptDownload().thenAccept(success -> {
            if (success) {
                ChatColorHandler.sendMessage(sender, "&#b7faa2Successfully updated PvPToggle, restart the server to apply changes!");
            } else {
                ChatColorHandler.sendMessage(sender, "&#ff6969Failed to update PvPToggle!");
            }
        });

        return true;
    }
}
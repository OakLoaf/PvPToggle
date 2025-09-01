package org.lushplugins.pvptoggle.command;

import org.lushplugins.pluginupdater.api.updater.Updater;
import org.lushplugins.pvptoggle.PvPToggle;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class UpdateCommand {

    @Command("pvp update")
    @CommandPermission("pvptoggle.update")
    public CompletableFuture<String> update() {
        Updater updater = PvPToggle.getInstance().getUpdater();
        if (updater == null) {
            return CompletableFuture.completedFuture("&#ff6969Updater is currently disabled!");
        }

        if (updater.isAlreadyDownloaded() || !updater.isUpdateAvailable()) {
            return CompletableFuture.completedFuture("&#ff6969It looks like there is no new update available!");
        }

        return updater.attemptDownload().thenApply(success -> {
            if (success) {
                return "&#b7faa2Successfully updated PvPToggle, restart the server to apply changes!";
            } else {
                return "&#ff6969Failed to update PvPToggle!";
            }
        });
    }
}
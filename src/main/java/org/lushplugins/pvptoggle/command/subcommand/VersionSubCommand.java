package org.lushplugins.pvptoggle.command.subcommand;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.command.SubCommand;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;

public class VersionSubCommand extends SubCommand {
    private final String pluginName;
    private final String version;

    public VersionSubCommand(@NotNull JavaPlugin plugin) {
        super("version");
        addRequiredPermission("lushrewards.version");

        PluginDescriptionFile description = plugin.getDescription();
        this.pluginName = description.getName();
        this.version = description.getVersion();
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args, @NotNull String[] fullArgs) {
        ChatColorHandler.sendMessage(sender, "&#a8e1ffYou are currently running &#58b1e0" + pluginName + " &#a8e1ffversion &#58b1e0" + version);
        return true;
    }
}

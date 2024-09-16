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
import org.lushplugins.pvptoggle.data.PvPUser;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class ToggleSubCommand extends SubCommand {
    private final HashSet<UUID> commandTimer = new HashSet<>();

    public ToggleSubCommand(String name) {
        super(name);
        addRequiredPermission("pvptoggle.use");
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

        int timeTillExecute = 0;
        UUID targetUUID = target.getUniqueId();
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvPToggle.getInstance().getConfigManager().sendMessage(sender, "no-permission");
                return true;
            }
        } else {
            if (!target.hasPermission("pvptoggle.bypasscooldown")) {
                if (commandTimer.contains(targetUUID)) {
                    PvPToggle.getInstance().getConfigManager().sendMessage(sender, "command-running");
                    return true;
                }

                long cooldown = PvPToggle.getInstance().getCooldownManager().getCooldown(target);
                if (cooldown >= 0) {
                    ChatColorHandler.sendMessage(target, PvPToggle.getInstance().getConfigManager().getMessage("pvp-cooldown")
                        .replace("%seconds%", String.valueOf(cooldown)));
                    return true;
                }
                timeTillExecute = PvPToggle.getInstance().getConfigManager().getCommandWaitTime();
            }
        }

        if (timeTillExecute > 0) {
            ChatColorHandler.sendMessage(target, PvPToggle.getInstance().getConfigManager().getMessage("command-timer")
                .replace("%seconds%", String.valueOf(timeTillExecute)));
            commandTimer.add(targetUUID);
        }

        boolean newPvPState;
        switch (this.getName()) {
            case "on" -> newPvPState = true;
            case "off" -> newPvPState = false;
            default -> newPvPState = !PvPToggle.getInstance().getDataManager().getPvPUser(target).isPvPEnabled();
        }

        Bukkit.getScheduler().runTaskLater(PvPToggle.getInstance(), () -> {
            PvPUser pvpUser = PvPToggle.getInstance().getDataManager().getPvPUser(target);
            pvpUser.setPvPEnabled(newPvPState);
            PvPToggle.getInstance().getCooldownManager().setCooldown(target, "COMMAND");
            commandTimer.remove(targetUUID);

            if (newPvPState) {
                PvPToggle.getInstance().getConfigManager().sendMessage(target, "pvp-state-enabled");
            } else {
                PvPToggle.getInstance().getConfigManager().sendMessage(target, "pvp-state-disabled");
            }

            if (sender != target) {
                ChatColorHandler.sendMessage(sender,
                    PvPToggle.getInstance().getConfigManager().getMessage("pvp-state-changed-other")
                        .replace("%player%", target.getName())
                        .replace("%pvp-state%", String.valueOf(newPvPState)));
            }
        }, timeTillExecute * 20L);

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

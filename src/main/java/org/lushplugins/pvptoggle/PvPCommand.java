package org.lushplugins.pvptoggle;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushlib.libraries.chatcolor.ChatColorHandler;
import org.lushplugins.pvptoggle.data.PvPUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

// TODO: Move over to LushLib
public class PvPCommand implements CommandExecutor, TabCompleter {
    private final PvPToggle plugin = PvPToggle.getInstance();
    private final HashSet<UUID> commandTimer = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }

            ChatColorHandler.sendMessage(player,
                PvPToggle.getInstance().getConfigManager().getMessage("pvp-status")
                    .replace("%pvp-state%", String.valueOf(PvPToggle.getInstance().getDataManager().getPvPUser(player).isPvPEnabled())));

        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "toggle" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    togglePvPCmd(player);
                }
                case "on" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    togglePvPCmd(player, true);
                }
                case "off" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    togglePvPCmd(player, false);
                }
                case "status" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    statusCmd(player);
                }
                case "help" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    helpCmd(player);
                }
                case "reload" -> reloadCmd(sender);
                default -> ChatColorHandler.sendMessage(sender,
                    PvPToggle.getInstance().getConfigManager().getMessage("incorrect-usage")
                        .replace("%command-usage%", "/pvp help"));
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                ChatColorHandler.sendMessage(sender,
                    PvPToggle.getInstance().getConfigManager().getMessage("unknown-player")
                        .replace("%player%", args[1]));
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "toggle" -> togglePvPCmd(sender, target);
                case "on" -> togglePvPCmd(sender, target, true);
                case "off" -> togglePvPCmd(sender, target, false);
                case "status" -> statusCmd(sender, target);
                default -> ChatColorHandler.sendMessage(sender,
                    PvPToggle.getInstance().getConfigManager().getMessage("incorrect-usage")
                        .replace("%command-usage%", "/pvp help"));
            }
        }
        return true;
    }

    private void helpCmd(Player player) {
        PvPToggle.getInstance().getConfigManager().sendMessage(player, "help-header");
        PvPToggle.getInstance().getConfigManager().sendMessage(player, "help-general-usage");
        if (player.hasPermission("pvptoggle.others"))
            PvPToggle.getInstance().getConfigManager().sendMessage(player, "help-view-other");
        if (player.hasPermission("pvptoggle.admin.others"))
            PvPToggle.getInstance().getConfigManager().sendMessage(player, "help-set-other");
    }

    private void reloadCmd(CommandSender sender) {
        if (!sender.hasPermission("pvptoggle.admin.reload")) {
            PvPToggle.getInstance().getConfigManager().sendMessage(sender, "no-permission");
            return;
        }
        PvPToggle.getInstance().getConfigManager().reloadConfig();
        PvPToggle.getInstance().getConfigManager().sendMessage(sender, "reload");
    }

    private void statusCmd(Player player) {
        statusCmd(player, player);
    }

    private void statusCmd(CommandSender sender, Player target) {
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvPToggle.getInstance().getConfigManager().sendMessage(sender, "no-permission");
                return;
            }

            ChatColorHandler.sendMessage(sender,
                PvPToggle.getInstance().getConfigManager().getMessage("pvp-status-other")
                    .replace("%player%", target.getName())
                    .replace("%pvp-state%", String.valueOf(PvPToggle.getInstance().getDataManager().getPvPUser(target).isPvPEnabled())));
        } else {
            ChatColorHandler.sendMessage(sender,
                PvPToggle.getInstance().getConfigManager().getMessage("pvp-status")
                    .replace("%pvp-state%", String.valueOf(PvPToggle.getInstance().getDataManager().getPvPUser(target).isPvPEnabled())));
        }
    }

    private void togglePvPCmd(Player player) {
        togglePvPCmd(player, player, !PvPToggle.getInstance().getDataManager().getPvPUser(player).isPvPEnabled());
    }

    private void togglePvPCmd(Player player, boolean newPvPState) {
        togglePvPCmd(player, player, newPvPState);
    }

    private void togglePvPCmd(CommandSender sender, Player target) {
        togglePvPCmd(sender, target, PvPToggle.getInstance().getDataManager().getPvPUser(target).isPvPEnabled());
    }

    private void togglePvPCmd(CommandSender sender, Player target, boolean newPvPState) {
        if (!sender.hasPermission("pvptoggle.use")) {
            PvPToggle.getInstance().getConfigManager().sendMessage(sender, "no-permission");
            return;
        }

        int timeTillExecute = 0;
        UUID targetUUID = target.getUniqueId();
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvPToggle.getInstance().getConfigManager().sendMessage(sender, "no-permission");
                return;
            }
        } else {
            if (!target.hasPermission("pvptoggle.bypasscooldown")) {
                if (commandTimer.contains(targetUUID)) {
                    PvPToggle.getInstance().getConfigManager().sendMessage(sender, "command-running");
                    return;
                }

                long cooldown = PvPToggle.getInstance().getCooldownManager().getCooldown(target);
                if (cooldown >= 0) {
                    ChatColorHandler.sendMessage(target, PvPToggle.getInstance().getConfigManager().getMessage("pvp-cooldown")
                        .replace("%seconds%", String.valueOf(cooldown)));
                    return;
                }
                timeTillExecute = PvPToggle.getInstance().getConfigManager().getCommandWaitTime();
            }
        }

        if (timeTillExecute > 0) {
            ChatColorHandler.sendMessage(target, PvPToggle.getInstance().getConfigManager().getMessage("command-timer")
                .replace("%seconds%", String.valueOf(timeTillExecute)));
            commandTimer.add(targetUUID);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
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
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;
        if (args.length == 1) {
            tabComplete.add("help");
            tabComplete.add("status");
            if (sender.hasPermission("pvptoggle.use")) {
                tabComplete.add("toggle");
                tabComplete.add("on");
                tabComplete.add("off");
            }
            if (sender.hasPermission("pvptoggle.admin.reload")) tabComplete.add("reload");
        } else if (args.length == 2) {
            if (sender.hasPermission("pvptoggle.admin.others")) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    tabComplete.add(player.getName());
                }
            }
        }

        for (String currTab : tabComplete) {
            int currArg = args.length - 1;
            if (currTab.startsWith(args[currArg])) {
                wordCompletion.add(currTab);
                wordCompletionSuccess = true;
            }
        }
        if (wordCompletionSuccess) return wordCompletion;
        return tabComplete;
    }
}

package org.beaconmc.pvptoggle;

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

public class PVPCommand implements CommandExecutor, TabCompleter {
    private final PVPToggle plugin = PVPToggle.getInstance();
    private final HashSet<UUID> commandTimer = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }

        if (args.length == 0) {
            PVPToggle.configManager.sendLangMessage(player, "PVP_STATUS", PVPToggle.dataManager.hasPVPEnabled(player.getUniqueId()));
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "toggle" -> togglePVPCmd(player);
                case "on" -> togglePVPCmd(player, true);
                case "off" -> togglePVPCmd(player, false);
                case "status" -> statusCmd(player);
                case "help" -> helpCmd(player);
                case "reload" -> reloadCmd(player);
                default -> PVPToggle.configManager.sendLangMessage(player, "COMMAND_INVALID", args[0]);
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                PVPToggle.configManager.sendLangMessage(player, "UNKNOWN_PLAYER", args[1]);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "toggle" -> togglePVPCmd(player, target);
                case "on" -> togglePVPCmd(player, target, true);
                case "off" -> togglePVPCmd(player, target, false);
                case "status" -> statusCmd(player, target);
                default -> PVPToggle.configManager.sendLangMessage(player, "COMMAND_INVALID", args[0]);
            }
        }
        return true;
    }

    private void helpCmd(Player player) {
        PVPToggle.configManager.sendLangMessage(player, "HELP_HEADER");
        PVPToggle.configManager.sendLangMessage(player, "HELP_GENERAL_USEAGE");
        if (player.hasPermission("pvptoggle.others")) PVPToggle.configManager.sendLangMessage(player, "HELP_VIEW_OTHERS");
        if (player.hasPermission("pvptoggle.admin.others")) PVPToggle.configManager.sendLangMessage(player, "HELP_SET_OTHERS");
    }

    private void reloadCmd(Player player) {
        if (!player.hasPermission("pvptoggle.admin.reload")) {
            PVPToggle.configManager.sendLangMessage(player, "NO_PERMISSION");
            return;
        }
        PVPToggle.configManager.reloadConfig();
        PVPToggle.configManager.sendLangMessage(player, "RELOAD_SUCCESS");
    }

    private void statusCmd(Player player) {
        statusCmd(player, player);
    }

    private void statusCmd(Player sender, Player target) {
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PVPToggle.configManager.sendLangMessage(sender, "NO_PERMISSION");
                return;
            }
            PVPToggle.configManager.sendLangMessage(sender, "PVP_STATUS_OTHERS", target.getName(), PVPToggle.dataManager.hasPVPEnabled(target.getUniqueId()));
        } else PVPToggle.configManager.sendLangMessage(sender, "PVP_STATUS", PVPToggle.dataManager.hasPVPEnabled(target.getUniqueId()));
    }

    private void togglePVPCmd(Player player) {
        togglePVPCmd(player, player, !PVPToggle.dataManager.hasPVPEnabled(player.getUniqueId()));
    }

    private void togglePVPCmd(Player player, boolean newPVPState) {
        togglePVPCmd(player, player, newPVPState);
    }

    private void togglePVPCmd(Player sender, Player target) {
        togglePVPCmd(sender, target, PVPToggle.dataManager.hasPVPEnabled(target.getUniqueId()));
    }

    private void togglePVPCmd(Player sender, Player target, boolean newPVPState) {
        if (!sender.hasPermission("pvptoggle.use")) {
            PVPToggle.configManager.sendLangMessage(sender, "NO_PERMISSION");
            return;
        }
        int timeTillExecute = 0;
        UUID targetUUID = target.getUniqueId();
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PVPToggle.configManager.sendLangMessage(sender, "NO_PERMISSION");
                return;
            }
        } else {
            if (!target.hasPermission("pvptoggle.bypasscooldown")) {
                if (commandTimer.contains(targetUUID)) {
                    PVPToggle.configManager.sendLangMessage(sender, "COMMAND_RUNNING");
                    return;
                }
                long cooldown = PVPToggle.cooldownManager.getCooldown(sender);
                if (cooldown >= 0) {
                    PVPToggle.configManager.sendLangMessage(sender, "PVP_COOLDOWN", String.valueOf(cooldown));
                    return;
                }
                timeTillExecute = PVPToggle.configManager.getCommandWaitTime();
            }
        }

        if (timeTillExecute > 0) {
            PVPToggle.configManager.sendLangMessage(target, "COMMAND_TIMER", String.valueOf(timeTillExecute));
            commandTimer.add(targetUUID);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PVPToggle.dataManager.updatePVPUser(target.getUniqueId(), newPVPState);
            PVPToggle.cooldownManager.setCooldown(target, "COMMAND");
            commandTimer.remove(targetUUID);
            if (newPVPState) PVPToggle.configManager.sendLangMessage(target, "PVP_STATE_ENABLED");
            else PVPToggle.configManager.sendLangMessage(target, "PVP_STATE_DISABLED");
            if (sender != target) PVPToggle.configManager.sendLangMessage(sender, "PVP_STATE_CHANGED_OTHERS", target.getName(), newPVPState);
        }, timeTillExecute * 20L);
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> tabComplete = new ArrayList<>();
        List<String> wordCompletion = new ArrayList<>();
        boolean wordCompletionSuccess = false;
        if (args.length == 1) {
            tabComplete.add("help");
            tabComplete.add("status");
            if (commandSender.hasPermission("pvptoggle.use")) {
                tabComplete.add("toggle");
                tabComplete.add("on");
                tabComplete.add("off");
            }
            if (commandSender.hasPermission("pvptoggle.admin.reload")) tabComplete.add("reload");
        } else if (args.length == 2) {
            if (commandSender.hasPermission("pvptoggle.admin.others")) {
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

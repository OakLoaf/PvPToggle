package org.lushplugins.pvptoggle;

import org.lushplugins.pvptoggle.datamanager.PvPUser;
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

public class PvPCommand implements CommandExecutor, TabCompleter {
    private final PvPToggle plugin = PvPToggle.getInstance();
    private final HashSet<UUID> commandTimer = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Console cannot run this command!");
                return true;
            }
            PvPToggle.getConfigManager().sendLangMessage(player, "PVP_STATUS", PvPToggle.getDataManager().getPvpUser(player).isPvpEnabled());
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "toggle" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    togglePvpCmd(player);
                }
                case "on" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    togglePvpCmd(player, true);
                }
                case "off" -> {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Console cannot run this command!");
                        return true;
                    }
                    togglePvpCmd(player, false);
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
                default -> PvPToggle.getConfigManager().sendLangMessage(sender, "COMMAND_INVALID", args[0]);
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                PvPToggle.getConfigManager().sendLangMessage(sender, "UNKNOWN_PLAYER", args[1]);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "toggle" -> togglePvpCmd(sender, target);
                case "on" -> togglePvpCmd(sender, target, true);
                case "off" -> togglePvpCmd(sender, target, false);
                case "status" -> statusCmd(sender, target);
                default -> PvPToggle.getConfigManager().sendLangMessage(sender, "COMMAND_INVALID", args[0]);
            }
        }
        return true;
    }

    private void helpCmd(Player player) {
        PvPToggle.getConfigManager().sendLangMessage(player, "HELP_HEADER");
        PvPToggle.getConfigManager().sendLangMessage(player, "HELP_GENERAL_USEAGE");
        if (player.hasPermission("pvptoggle.others")) PvPToggle.getConfigManager().sendLangMessage(player, "HELP_VIEW_OTHERS");
        if (player.hasPermission("pvptoggle.admin.others")) PvPToggle.getConfigManager().sendLangMessage(player, "HELP_SET_OTHERS");
    }

    private void reloadCmd(CommandSender sender) {
        if (!sender.hasPermission("pvptoggle.admin.reload")) {
            PvPToggle.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
            return;
        }
        PvPToggle.getConfigManager().reloadConfig();
        PvPToggle.getConfigManager().sendLangMessage(sender, "RELOAD_SUCCESS");
    }

    private void statusCmd(Player player) {
        statusCmd(player, player);
    }

    private void statusCmd(CommandSender sender, Player target) {
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvPToggle.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
                return;
            }
            PvPToggle.getConfigManager().sendLangMessage(sender, "PVP_STATUS_OTHERS", target.getName(), PvPToggle.getDataManager().getPvpUser(target).isPvpEnabled());
        } else PvPToggle.getConfigManager().sendLangMessage(sender, "PVP_STATUS", PvPToggle.getDataManager().getPvpUser(target).isPvpEnabled());
    }

    private void togglePvpCmd(Player player) {
        togglePvpCmd(player, player, !PvPToggle.getDataManager().getPvpUser(player).isPvpEnabled());
    }

    private void togglePvpCmd(Player player, boolean newPvpState) {
        togglePvpCmd(player, player, newPvpState);
    }

    private void togglePvpCmd(CommandSender sender, Player target) {
        togglePvpCmd(sender, target, PvPToggle.getDataManager().getPvpUser(target).isPvpEnabled());
    }

    private void togglePvpCmd(CommandSender sender, Player target, boolean newPvpState) {
        if (!sender.hasPermission("pvptoggle.use")) {
            PvPToggle.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
            return;
        }
        int timeTillExecute = 0;
        UUID targetUUID = target.getUniqueId();
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvPToggle.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
                return;
            }
        } else {
            if (!target.hasPermission("pvptoggle.bypasscooldown")) {
                if (commandTimer.contains(targetUUID)) {
                    PvPToggle.getConfigManager().sendLangMessage(sender, "COMMAND_RUNNING");
                    return;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Console cannot run this command!");
                    return;
                }
                long cooldown = PvPToggle.getCooldownManager().getCooldown(player);
                if (cooldown >= 0) {
                    PvPToggle.getConfigManager().sendLangMessage(sender, "PVP_COOLDOWN", String.valueOf(cooldown));
                    return;
                }
                timeTillExecute = PvPToggle.getConfigManager().getCommandWaitTime();
            }
        }

        if (timeTillExecute > 0) {
            PvPToggle.getConfigManager().sendLangMessage(target, "COMMAND_TIMER", String.valueOf(timeTillExecute));
            commandTimer.add(targetUUID);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PvPUser pvpUser = PvPToggle.getDataManager().getPvpUser(target);
            pvpUser.setPvpEnabled(newPvpState);
            PvPToggle.getCooldownManager().setCooldown(target, "COMMAND");
            commandTimer.remove(targetUUID);
            if (newPvpState) PvPToggle.getConfigManager().sendLangMessage(target, "PVP_STATE_ENABLED");
            else PvPToggle.getConfigManager().sendLangMessage(target, "PVP_STATE_DISABLED");
            if (sender != target) PvPToggle.getConfigManager().sendLangMessage(sender, "PVP_STATE_CHANGED_OTHERS", target.getName(), newPvpState);
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

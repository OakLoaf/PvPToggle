package org.beaconmc.pvptoggle;

import org.beaconmc.pvptoggle.datamanager.PvpUser;
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

public class PvpCommand implements CommandExecutor, TabCompleter {
    private final PvpTogglePlugin plugin = PvpTogglePlugin.getInstance();
    private final HashSet<UUID> commandTimer = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Console cannot run this command!");
            return true;
        }

        if (args.length == 0) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(player, "PVP_STATUS", PvpTogglePlugin.getDataManager().getPvpUser(player.getUniqueId()).isPvpEnabled());
        } else if (args.length == 1) {
            switch (args[0].toLowerCase()) {
                case "toggle" -> togglePvpCmd(player);
                case "on" -> togglePvpCmd(player, true);
                case "off" -> togglePvpCmd(player, false);
                case "status" -> statusCmd(player);
                case "help" -> helpCmd(player);
                case "reload" -> reloadCmd(player);
                default -> PvpTogglePlugin.getConfigManager().sendLangMessage(player, "COMMAND_INVALID", args[0]);
            }
        } else if (args.length == 2) {
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                PvpTogglePlugin.getConfigManager().sendLangMessage(player, "UNKNOWN_PLAYER", args[1]);
                return true;
            }
            switch (args[0].toLowerCase()) {
                case "toggle" -> togglePvpCmd(player, target);
                case "on" -> togglePvpCmd(player, target, true);
                case "off" -> togglePvpCmd(player, target, false);
                case "status" -> statusCmd(player, target);
                default -> PvpTogglePlugin.getConfigManager().sendLangMessage(player, "COMMAND_INVALID", args[0]);
            }
        }
        return true;
    }

    private void helpCmd(Player player) {
        PvpTogglePlugin.getConfigManager().sendLangMessage(player, "HELP_HEADER");
        PvpTogglePlugin.getConfigManager().sendLangMessage(player, "HELP_GENERAL_USEAGE");
        if (player.hasPermission("pvptoggle.others")) PvpTogglePlugin.getConfigManager().sendLangMessage(player, "HELP_VIEW_OTHERS");
        if (player.hasPermission("pvptoggle.admin.others")) PvpTogglePlugin.getConfigManager().sendLangMessage(player, "HELP_SET_OTHERS");
    }

    private void reloadCmd(Player player) {
        if (!player.hasPermission("pvptoggle.admin.reload")) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(player, "NO_PERMISSION");
            return;
        }
        PvpTogglePlugin.getConfigManager().reloadConfig();
        PvpTogglePlugin.getConfigManager().sendLangMessage(player, "RELOAD_SUCCESS");
    }

    private void statusCmd(Player player) {
        statusCmd(player, player);
    }

    private void statusCmd(Player sender, Player target) {
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
                return;
            }
            PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "PVP_STATUS_OTHERS", target.getName(), PvpTogglePlugin.getDataManager().getPvpUser(target.getUniqueId()).isPvpEnabled());
        } else PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "PVP_STATUS", PvpTogglePlugin.getDataManager().getPvpUser(target.getUniqueId()).isPvpEnabled());
    }

    private void togglePvpCmd(Player player) {
        togglePvpCmd(player, player, !PvpTogglePlugin.getDataManager().getPvpUser(player.getUniqueId()).isPvpEnabled());
    }

    private void togglePvpCmd(Player player, boolean newPvpState) {
        togglePvpCmd(player, player, newPvpState);
    }

    private void togglePvpCmd(Player sender, Player target) {
        togglePvpCmd(sender, target, PvpTogglePlugin.getDataManager().getPvpUser(target.getUniqueId()).isPvpEnabled());
    }

    private void togglePvpCmd(Player sender, Player target, boolean newPvpState) {
        if (!sender.hasPermission("pvptoggle.use")) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
            return;
        }
        int timeTillExecute = 0;
        UUID targetUUID = target.getUniqueId();
        if (sender != target) {
            if (!sender.hasPermission("pvptoggle.admin.others")) {
                PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "NO_PERMISSION");
                return;
            }
        } else {
            if (!target.hasPermission("pvptoggle.bypasscooldown")) {
                if (commandTimer.contains(targetUUID)) {
                    PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "COMMAND_RUNNING");
                    return;
                }
                long cooldown = PvpTogglePlugin.getCooldownManager().getCooldown(sender);
                if (cooldown >= 0) {
                    PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "PVP_COOLDOWN", String.valueOf(cooldown));
                    return;
                }
                timeTillExecute = PvpTogglePlugin.getConfigManager().getCommandWaitTime();
            }
        }

        if (timeTillExecute > 0) {
            PvpTogglePlugin.getConfigManager().sendLangMessage(target, "COMMAND_TIMER", String.valueOf(timeTillExecute));
            commandTimer.add(targetUUID);
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            PvpUser pvpUser = PvpTogglePlugin.getDataManager().getPvpUser(targetUUID);
            pvpUser.setPvpEnabled(newPvpState);
            PvpTogglePlugin.getCooldownManager().setCooldown(target, "COMMAND");
            commandTimer.remove(targetUUID);
            if (newPvpState) PvpTogglePlugin.getConfigManager().sendLangMessage(target, "PVP_STATE_ENABLED");
            else PvpTogglePlugin.getConfigManager().sendLangMessage(target, "PVP_STATE_DISABLED");
            if (sender != target) PvpTogglePlugin.getConfigManager().sendLangMessage(sender, "PVP_STATE_CHANGED_OTHERS", target.getName(), newPvpState);
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

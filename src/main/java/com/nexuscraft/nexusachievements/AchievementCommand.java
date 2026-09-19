package com.nexuscraft.nexusachievements;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/** {@code /achievements} (aliases {@code /ach}, {@code /nexusach}) -- player-facing list/info,
 *  plus admin grant/revoke/reload behind {@code nexusachievements.admin}. */
public final class AchievementCommand implements CommandExecutor, TabCompleter {

    private final JavaPlugin plugin;
    private final AchievementManager manager;
    private final ConfigReloadHook reloadHook;

    public interface ConfigReloadHook {
        void reload();
    }

    public AchievementCommand(JavaPlugin plugin, AchievementManager manager, ConfigReloadHook reloadHook) {
        this.plugin = plugin;
        this.manager = manager;
        this.reloadHook = reloadHook;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return listFor(sender);
        }

        switch (args[0].toLowerCase()) {
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /achievements info <id>");
                    return true;
                }
                return info(sender, args[1]);
            }
            case "reload" -> {
                if (!sender.hasPermission("nexusachievements.admin")) {
                    sender.sendMessage("§cYou don't have permission to do that.");
                    return true;
                }
                reloadHook.reload();
                sender.sendMessage("§aNexusAchievements config reloaded.");
                return true;
            }
            case "grant" -> {
                return adminGrantOrRevoke(sender, args, true);
            }
            case "revoke" -> {
                return adminGrantOrRevoke(sender, args, false);
            }
            case "listall" -> {
                if (!sender.hasPermission("nexusachievements.admin")) {
                    sender.sendMessage("§cYou don't have permission to do that.");
                    return true;
                }
                sender.sendMessage("§6All " + manager.registry().all().size() + " defined achievements:");
                for (AchievementDefinition definition : manager.registry().all().values()) {
                    sender.sendMessage("§7 - §f" + definition.id() + " §8(" + definition.sourcePlugin()
                            + ") §7" + definition.title());
                }
                return true;
            }
            default -> {
                return listFor(sender);
            }
        }
    }

    private boolean adminGrantOrRevoke(CommandSender sender, String[] args, boolean grant) {
        if (!sender.hasPermission("nexusachievements.admin")) {
            sender.sendMessage("§cYou don't have permission to do that.");
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage("§cUsage: /achievements " + (grant ? "grant" : "revoke") + " <player> <id>");
            return true;
        }
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String id = args[2];
        if (manager.registry().get(id) == null) {
            sender.sendMessage("§cNo achievement with id '" + id + "'.");
            return true;
        }

        if (grant) {
            Player online = Bukkit.getPlayerExact(args[1]);
            if (online == null) {
                sender.sendMessage("§cThat player needs to be online to grant a toast-worthy achievement to them.");
                return true;
            }
            boolean earned = manager.grant(online, id);
            sender.sendMessage(earned
                    ? "§aGranted '" + id + "' to " + online.getName() + "."
                    : "§e" + online.getName() + " already had '" + id + "' (or it needs more progress ticks).");
        } else {
            boolean revoked = manager.revoke(target.getUniqueId(), id);
            sender.sendMessage(revoked
                    ? "§aRevoked '" + id + "' from " + target.getName() + "."
                    : "§e" + target.getName() + " didn't have '" + id + "'.");
        }
        return true;
    }

    private boolean listFor(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly a player has achievements to list -- try /achievements listall.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        List<AchievementDefinition> defs = new ArrayList<>(manager.registry().all().values());
        defs.sort(Comparator.comparing(AchievementDefinition::sourcePlugin).thenComparing(AchievementDefinition::title));

        int earnedCount = 0;
        sender.sendMessage("§6§lYour Achievements");
        String currentSource = null;
        for (AchievementDefinition definition : defs) {
            boolean earned = manager.hasEarned(uuid, definition.id());
            if (earned) {
                earnedCount++;
            }
            if (definition.hidden() && !earned) {
                continue;
            }
            if (!definition.sourcePlugin().equals(currentSource)) {
                currentSource = definition.sourcePlugin();
                sender.sendMessage("§8§m       §r §7" + currentSource + " §8§m       ");
            }
            String mark = earned ? "§a✔" : "§8✖";
            String progressSuffix = "";
            if (!earned && definition.goalTarget() > 1) {
                progressSuffix = " §8(" + manager.progress(uuid, definition.id()) + "/" + definition.goalTarget() + ")";
            }
            sender.sendMessage(mark + " §" + (earned ? "f" : "7") + definition.title() + progressSuffix);
        }
        sender.sendMessage("§6" + earnedCount + "§7/§6" + defs.size() + " §7earned.");
        return true;
    }

    private boolean info(CommandSender sender, String id) {
        AchievementDefinition definition = manager.registry().get(id);
        if (definition == null) {
            sender.sendMessage("§cNo achievement with id '" + id + "'.");
            return true;
        }
        sender.sendMessage("§6§l" + definition.title() + " §8(" + definition.id() + ")");
        sender.sendMessage("§7" + definition.description());
        sender.sendMessage("§8Source: §7" + definition.sourcePlugin() + " §8| Frame: §7"
                + definition.frame() + " §8| Icon: §7" + definition.icon());
        if (sender instanceof Player player) {
            boolean earned = manager.hasEarned(player.getUniqueId(), id);
            sender.sendMessage(earned ? "§aEarned." : "§7Not earned yet.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> options = new ArrayList<>();
        if (args.length == 1) {
            options.add("info");
            options.add("listall");
            if (sender.hasPermission("nexusachievements.admin")) {
                options.add("grant");
                options.add("revoke");
                options.add("reload");
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("grant")
                || args[0].equalsIgnoreCase("revoke"))) {
            if (args[0].equalsIgnoreCase("info")) {
                for (AchievementDefinition definition : manager.registry().all().values()) {
                    options.add(definition.id());
                }
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("grant") || args[0].equalsIgnoreCase("revoke"))) {
            for (AchievementDefinition definition : manager.registry().all().values()) {
                options.add(definition.id());
            }
        }
        return options;
    }
}

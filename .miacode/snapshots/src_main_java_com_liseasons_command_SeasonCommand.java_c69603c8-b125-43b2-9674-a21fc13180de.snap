package com.liseasons.command;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonManager;
import com.liseasons.season.SeasonState;
import com.liseasons.season.SolarTerm;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class SeasonCommand implements CommandExecutor, TabCompleter {
    private final LISeasonsPlugin plugin;

    public SeasonCommand(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            this.plugin.getMessageManager().messageList("command.help").forEach(sender::sendMessage);
            return true;
        }

        String sub = args[0].toLowerCase();
        return switch (sub) {
            case "info" -> handleInfo(sender, args);
            case "calendar" -> handleCalendar(sender);
            case "reload" -> handleReload(sender);
            case "set" -> handleSet(sender, args);
            case "next" -> handleNext(sender, args);
            case "auto" -> handleAuto(sender, args);
            default -> {
                this.plugin.getMessageManager().messageList("command.help").forEach(sender::sendMessage);
                yield true;
            }
        };
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
        if (world == null) {
            return true;
        }
        SeasonManager manager = this.plugin.getSeasonManager();
        if (!manager.isEnabled(world)) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.world-disabled"));
            return true;
        }

        SeasonState state = manager.getState(world);
        MessageManager messages = this.plugin.getMessageManager();
        sender.sendMessage(messages.message(
                "command.info",
                MessageManager.Placeholder.of("world", world.getName()),
                MessageManager.Placeholder.of("season", messages.seasonName(state.season().key())),
                MessageManager.Placeholder.of("term", messages.solarTermName(state.solarTerm().key())),
                MessageManager.Placeholder.of("mode", manager.isAutomatic(world) ? "自动" : "手动")
        ));
        TemperatureSnapshot snapshot = this.plugin.getTemperatureService().snapshot(world, sender instanceof org.bukkit.entity.Player player ? player.getLocation() : world.getSpawnLocation());
        sender.sendMessage(messages.message(
                "command.info-temperature",
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot)),
                MessageManager.Placeholder.of("weather", snapshot.description())
        ));
        return true;
    }

    private boolean handleCalendar(CommandSender sender) {
        if (!(sender instanceof org.bukkit.entity.Player player)) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.player-only"));
            return true;
        }
        this.plugin.getCalendarMenu().open(player);
        return true;
    }

    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("liseasons.admin")) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.no-permission"));
            return true;
        }
        this.plugin.reloadPlugin();
        sender.sendMessage(this.plugin.getMessageManager().message("command.reloaded"));
        return true;
    }

    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.hasPermission("liseasons.admin")) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.no-permission"));
            return true;
        }
        if (args.length < 4) {
            this.plugin.getMessageManager().messageList("command.help").forEach(sender::sendMessage);
            return true;
        }

        String mode = args[1].toLowerCase();
        World world = Bukkit.getWorld(args[2]);
        if (world == null) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.unknown-world"));
            return true;
        }

        if ("season".equals(mode)) {
            return handleSetSeason(sender, world, args[3]);
        }
        if ("term".equals(mode)) {
            return handleSetTerm(sender, world, args[3]);
        }

        this.plugin.getMessageManager().messageList("command.help").forEach(sender::sendMessage);
        return true;
    }

    private boolean handleSetSeason(CommandSender sender, World world, String input) {
        Season season = Season.fromKey(input).orElse(null);
        if (season == null) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.unknown-season"));
            return true;
        }
        SeasonState state = this.plugin.getSeasonManager().setSeason(world, season);
        sender.sendMessage(this.plugin.getMessageManager().message(
                "command.set-season",
                MessageManager.Placeholder.of("world", world.getName()),
                MessageManager.Placeholder.of("season", this.plugin.getMessageManager().seasonName(state.season().key()))
        ));
        sender.sendMessage(this.plugin.getMessageManager().message("command.color-refresh"));
        return true;
    }

    private boolean handleSetTerm(CommandSender sender, World world, String input) {
        SolarTerm term = SolarTerm.fromKey(input).orElse(null);
        if (term == null) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.unknown-term"));
            return true;
        }
        SeasonState state = this.plugin.getSeasonManager().setSolarTerm(world, term);
        sender.sendMessage(this.plugin.getMessageManager().message(
                "command.set-term",
                MessageManager.Placeholder.of("world", world.getName()),
                MessageManager.Placeholder.of("term", this.plugin.getMessageManager().solarTermName(state.solarTerm().key()))
        ));
        return true;
    }

    private boolean handleNext(CommandSender sender, String[] args) {
        if (!sender.hasPermission("liseasons.admin")) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.no-permission"));
            return true;
        }
        World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
        if (world == null) {
            return true;
        }
        SeasonState state = this.plugin.getSeasonManager().advance(world);
        if (state == null) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.world-disabled"));
            return true;
        }
        sender.sendMessage(this.plugin.getMessageManager().message(
                "command.advanced",
                MessageManager.Placeholder.of("world", world.getName()),
                MessageManager.Placeholder.of("term", this.plugin.getMessageManager().solarTermName(state.solarTerm().key()))
        ));
        return true;
    }

    private boolean handleAuto(CommandSender sender, String[] args) {
        if (!sender.hasPermission("liseasons.admin")) {
            sender.sendMessage(this.plugin.getMessageManager().message("command.no-permission"));
            return true;
        }
        World world = resolveWorld(sender, args.length >= 2 ? args[1] : null);
        if (world == null) {
            return true;
        }
        this.plugin.getSeasonManager().setAutomatic(world, true);
        sender.sendMessage(this.plugin.getMessageManager().message(
                "command.set-auto",
                MessageManager.Placeholder.of("world", world.getName())
        ));
        return true;
    }

    private @Nullable World resolveWorld(CommandSender sender, @Nullable String worldName) {
        if (worldName != null && !worldName.isBlank()) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                sender.sendMessage(this.plugin.getMessageManager().message("command.unknown-world"));
            }
            return world;
        }
        if (sender instanceof org.bukkit.entity.Player player) {
            return player.getWorld();
        }
        sender.sendMessage(this.plugin.getMessageManager().message("command.unknown-world"));
        return null;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(List.of("help", "info", "calendar", "reload", "set", "next", "auto"), args[0]);
        }
        if (args.length == 2 && "set".equalsIgnoreCase(args[0])) {
            return filter(List.of("season", "term"), args[1]);
        }
        if (args.length == 2 && ("info".equalsIgnoreCase(args[0]) || "next".equalsIgnoreCase(args[0]) || "auto".equalsIgnoreCase(args[0]))) {
            return filter(this.plugin.getSeasonManager().loadedWorldNames().stream().sorted().toList(), args[1]);
        }
        if (args.length == 3 && "set".equalsIgnoreCase(args[0])) {
            return filter(this.plugin.getSeasonManager().loadedWorldNames().stream().sorted().toList(), args[2]);
        }
        if (args.length == 4 && "set".equalsIgnoreCase(args[0]) && "season".equalsIgnoreCase(args[1])) {
            return filter(List.of("spring", "summer", "autumn", "winter"), args[3]);
        }
        if (args.length == 4 && "set".equalsIgnoreCase(args[0]) && "term".equalsIgnoreCase(args[1])) {
            return filter(List.of(
                    "lichun", "yushui", "jingzhe", "chunfen", "qingming", "guyu",
                    "lixia", "xiaoman", "mangzhong", "xiazhi", "xiaoshu", "dashu",
                    "liqiu", "chushu", "bailu", "qiufen", "hanlu", "shuangjiang",
                    "lidong", "xiaoxue", "daxue", "dongzhi", "xiaohan", "dahan"
            ), args[3]);
        }
        return List.of();
    }

    private List<String> filter(List<String> values, String input) {
        String normalized = Objects.requireNonNullElse(input, "").toLowerCase();
        return values.stream()
                .filter(value -> value.toLowerCase().startsWith(normalized))
                .toList();
    }
}

package com.liseasons.calendar;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.season.Season;
import com.liseasons.season.SeasonState;
import com.liseasons.season.SolarTerm;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class CalendarMenu {
    private final LISeasonsPlugin plugin;

    public CalendarMenu(LISeasonsPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        open(player, CalendarPage.OVERVIEW, true);
    }

    public void open(Player player, CalendarPage page, boolean sendMessage) {
        SeasonState state = this.plugin.getSeasonManager().getState(player.getWorld());
        if (state == null) {
            player.sendMessage(this.plugin.getMessageManager().message("command.world-disabled"));
            return;
        }

        TemperatureSnapshot snapshot = this.plugin.getTemperatureService().snapshot(player);
        MessageManager messages = this.plugin.getMessageManager();
        String seasonName = messages.seasonName(state.season().key());
        String termName = messages.solarTermName(state.solarTerm().key());
        LocalDate today = LocalDate.now();

        int size = sizeFor(page);
        CalendarMenuHolder holder = new CalendarMenuHolder(page);
        Inventory inventory = Bukkit.createInventory(holder, size, messages.rawMessage("calendar.title"));
        holder.bind(inventory);

        switch (page) {
            case OVERVIEW -> renderOverview(inventory, messages, state, snapshot, seasonName, termName, player.getWorld().getName(), today);
            case MONTH_TERMS -> renderMonthTerms(inventory, messages, today, state);
            case YEAR_TERMS -> renderYearTerms(inventory, messages, state);
            case TEMPERATURE -> renderTemperaturePage(inventory, messages, snapshot, state, player.getWorld().getName());
        }

        renderSeasonFrame(inventory, state.season());
        renderStatusBar(inventory, messages, page, snapshot);
        renderNavigation(inventory, messages, page);
        player.openInventory(inventory);
        if (sendMessage) {
            player.sendMessage(messages.message("command.calendar-opened"));
        }
    }

    private void fill(Inventory inventory, ItemStack itemStack, int slot) {
        if (slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, itemStack);
        }
    }

    private ItemStack createInfoItem(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createSimpleItem(Material material, Component name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        stack.setItemMeta(meta);
        return stack;
    }

    private void renderOverview(Inventory inventory,
                                MessageManager messages,
                                SeasonState state,
                                TemperatureSnapshot snapshot,
                                String seasonName,
                                String termName,
                                String worldName,
                                LocalDate today) {
        fill(inventory, createInfoItem(
                materialFor(state.season()),
                messages.rawMessage("calendar.season-item"),
                messages.rawMessageList("calendar.lore-season", MessageManager.Placeholder.of("season", seasonName))
        ), 10);
        fill(inventory, createInfoItem(
                Material.AMETHYST_SHARD,
                messages.rawMessage("calendar.term-item"),
                messages.rawMessageList("calendar.lore-term", MessageManager.Placeholder.of("term", termName))
        ), 11);
        fill(inventory, createInfoItem(
                Material.BLAZE_POWDER,
                messages.rawMessage("calendar.temperature-item"),
                messages.rawMessageList(
                        "calendar.lore-temperature",
                        MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot) + " | " + snapshot.description())
                )
        ), 12);
        fill(inventory, createInfoItem(
                Material.MAP,
                messages.rawMessage("calendar.world-item"),
                messages.rawMessageList("calendar.lore-world", MessageManager.Placeholder.of("world", worldName))
        ), 13);
        fill(inventory, createInfoItem(
                Material.WATER_BUCKET,
                messages.rawMessage("calendar.weather-item"),
                messages.rawMessageList("calendar.lore-weather", MessageManager.Placeholder.of("weather", describeWeather(state)))
        ), 14);
        fill(inventory, createInfoItem(
                Material.BOOK,
                messages.rawMessage("calendar.next-item"),
                messages.rawMessageList(
                        "calendar.lore-next",
                        MessageManager.Placeholder.of("next", messages.solarTermName(state.solarTerm().next().key()) + " | " + formatMonthDay(state.solarTerm().next()))
                )
        ), 15);
        fill(inventory, createInfoItem(
                Material.ENDER_EYE,
                messages.rawMessage("calendar.visual-item"),
                messages.rawMessageList("calendar.lore-visual", MessageManager.Placeholder.of("visual", describeVisuals(state)))
        ), 16);
        fill(inventory, createInfoItem(
                Material.COMPASS,
                messages.rawMessage("calendar.month-item"),
                messages.rawMessageList(
                        "calendar.lore-month",
                        MessageManager.Placeholder.of("month", today.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA)),
                        MessageManager.Placeholder.of("terms", describeMonthTerms(today.getMonthValue(), messages))
                )
        ), 22);
    }

    private void renderMonthTerms(Inventory inventory, MessageManager messages, LocalDate today, SeasonState state) {
        List<SolarTerm> monthTerms = termsForMonth(today.getMonthValue());
        int slot = 10;
        for (SolarTerm term : monthTerms) {
            fill(inventory, createInfoItem(
                    term == state.solarTerm() ? Material.GLOW_BERRIES : materialFor(term),
                    messages.rawMessage(
                            "calendar.month-term-item",
                            MessageManager.Placeholder.of("term", messages.solarTermName(term.key()))
                    ),
                    messages.rawMessageList(
                            "calendar.lore-month-term",
                            MessageManager.Placeholder.of("date", formatMonthDay(term)),
                            MessageManager.Placeholder.of("season", messages.seasonName(term.season().key())),
                            MessageManager.Placeholder.of("current", term == state.solarTerm() ? "当前节气" : "尚未到来")
                    )
            ), slot++);
        }
        fill(inventory, createInfoItem(
                Material.COMPASS,
                messages.rawMessage("calendar.month-summary-item"),
                messages.rawMessageList(
                        "calendar.lore-month-summary",
                        MessageManager.Placeholder.of("month", today.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA)),
                        MessageManager.Placeholder.of("terms", describeMonthTerms(today.getMonthValue(), messages))
                )
        ), 31);
    }

    private void renderYearTerms(Inventory inventory, MessageManager messages, SeasonState state) {
        int[] slots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39
        };
        int index = 0;
        for (SolarTerm term : SolarTerm.values()) {
            fill(inventory, createInfoItem(
                    term == state.solarTerm() ? Material.ENCHANTED_BOOK : materialFor(term),
                    messages.rawMessage(
                            "calendar.year-term-item",
                            MessageManager.Placeholder.of("term", messages.solarTermName(term.key()))
                    ),
                    messages.rawMessageList(
                            "calendar.lore-year-term",
                            MessageManager.Placeholder.of("date", formatMonthDay(term)),
                            MessageManager.Placeholder.of("season", messages.seasonName(term.season().key())),
                            MessageManager.Placeholder.of("current", term == state.solarTerm() ? "当前节气" : "全年节气")
                    )
            ), slots[index++]);
        }
    }

    private void renderTemperaturePage(Inventory inventory,
                                       MessageManager messages,
                                       TemperatureSnapshot snapshot,
                                       SeasonState state,
                                       String worldName) {
        fill(inventory, createInfoItem(
                Material.BLAZE_POWDER,
                messages.rawMessage("calendar.temperature-item"),
                messages.rawMessageList(
                        "calendar.lore-temperature",
                        MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot) + " | " + snapshot.description()),
                        MessageManager.Placeholder.of("air", this.plugin.getTemperatureService().formatValue(snapshot.airTemperature())),
                        MessageManager.Placeholder.of("wetness", formatWetness(snapshot.wetness()))
                )
        ), 10);
        fill(inventory, createInfoItem(
                Material.CAMPFIRE,
                messages.rawMessage("calendar.temperature-base-item"),
                messages.rawMessageList(
                        "calendar.lore-temperature-base",
                        MessageManager.Placeholder.of("season", messages.seasonName(state.season().key())),
                        MessageManager.Placeholder.of("base", this.plugin.getTemperatureService().formatBaseTemperature(state.season()))
                )
        ), 12);
        fill(inventory, createInfoItem(
                Material.SPYGLASS,
                messages.rawMessage("calendar.temperature-weather-item"),
                messages.rawMessageList(
                        "calendar.lore-temperature-weather",
                        MessageManager.Placeholder.of("weather", describeWeather(state)),
                        MessageManager.Placeholder.of("world", worldName)
                )
        ), 14);
        fill(inventory, createInfoItem(
                Material.SNOWBALL,
                messages.rawMessage("calendar.temperature-guide-item"),
                messages.rawMessageList("calendar.lore-temperature-guide")
        ), 16);
        fill(inventory, createInfoItem(
                Material.BOOKSHELF,
                messages.rawMessage("calendar.temperature-note-item"),
                messages.rawMessageList(
                        "calendar.lore-temperature-note",
                        MessageManager.Placeholder.of("term", messages.solarTermName(state.solarTerm().key()))
                )
        ), 31);
    }

    private void renderNavigation(Inventory inventory, MessageManager messages, CalendarPage page) {
        fill(inventory, createInfoItem(
                page == CalendarPage.OVERVIEW ? Material.LODESTONE : Material.CLOCK,
                messages.rawMessage("calendar.page-overview"),
                messages.rawMessageList("calendar.lore-page-overview")
        ), 48);
        fill(inventory, createInfoItem(
                Material.BARRIER,
                messages.rawMessage("calendar.page-close"),
                messages.rawMessageList("calendar.lore-page-close")
        ), 49);
        fill(inventory, createInfoItem(
                page == CalendarPage.MONTH_TERMS ? Material.LODESTONE : Material.COMPASS,
                messages.rawMessage("calendar.page-month"),
                messages.rawMessageList("calendar.lore-page-month")
        ), 50);
        fill(inventory, createInfoItem(
                page == CalendarPage.YEAR_TERMS ? Material.LODESTONE : Material.BOOK,
                messages.rawMessage("calendar.page-year"),
                messages.rawMessageList("calendar.lore-page-year")
        ), 51);
        fill(inventory, createInfoItem(
                page == CalendarPage.TEMPERATURE ? Material.LODESTONE : Material.BLAZE_POWDER,
                messages.rawMessage("calendar.page-temperature"),
                messages.rawMessageList("calendar.lore-page-temperature")
        ), 52);
    }

    private void renderSeasonFrame(Inventory inventory, Season season) {
        Material frameMaterial = switch (season) {
            case SPRING -> Material.LIME_STAINED_GLASS_PANE;
            case SUMMER -> Material.YELLOW_STAINED_GLASS_PANE;
            case AUTUMN -> Material.ORANGE_STAINED_GLASS_PANE;
            case WINTER -> Material.LIGHT_BLUE_STAINED_GLASS_PANE;
        };
        Component blank = Component.text(" ");

        int[] slots = {
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 53
        };
        for (int slot : slots) {
            if (inventory.getItem(slot) == null) {
                fill(inventory, createSimpleItem(frameMaterial, blank), slot);
            }
        }
    }

    private void renderStatusBar(Inventory inventory, MessageManager messages, CalendarPage page, TemperatureSnapshot snapshot) {
        fill(inventory, createInfoItem(
                Material.NAME_TAG,
                messages.rawMessage("calendar.status-item"),
                messages.rawMessageList(
                        "calendar.lore-status",
                        MessageManager.Placeholder.of("page", pageName(messages, page)),
                        MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot)),
                        MessageManager.Placeholder.of("wetness", formatWetness(snapshot.wetness()))
                )
        ), 53);
    }

    private int sizeFor(CalendarPage page) {
        int configured = this.plugin.getLiConfig().calendarConfig().size();
        return Math.max(configured, 54);
    }

    private List<SolarTerm> termsForMonth(int month) {
        return this.plugin.getLiConfig().gregorianDates().entrySet().stream()
                .filter(entry -> entry.getValue().getMonthValue() == month)
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getDayOfMonth()))
                .map(java.util.Map.Entry::getKey)
                .toList();
    }

    private String describeMonthTerms(int month, MessageManager messages) {
        List<String> names = new ArrayList<>();
        for (SolarTerm term : termsForMonth(month)) {
            names.add(messages.solarTermName(term.key()));
        }
        return String.join("、", names);
    }

    private String formatMonthDay(SolarTerm term) {
        MonthDay date = this.plugin.getLiConfig().gregorianDates().getOrDefault(term, term.defaultDate());
        return date.getMonthValue() + "月" + date.getDayOfMonth() + "日";
    }

    private Material materialFor(SolarTerm term) {
        return materialFor(term.season());
    }

    private Material materialFor(Season season) {
        return switch (season) {
            case SPRING -> Material.FLOWERING_AZALEA;
            case SUMMER -> Material.SUNFLOWER;
            case AUTUMN -> Material.ACACIA_LEAVES;
            case WINTER -> Material.SNOWBALL;
        };
    }

    private String describeWeather(SeasonState state) {
        return switch (state.season()) {
            case SPRING -> "冰雪开始融化，气温回升，降雨略多。";
            case SUMMER -> "烈日偏强，作物生长更快，热带区域更容易出现干热感。";
            case AUTUMN -> "天气转凉，降雨趋缓，树叶和草地会逐步偏暖色。";
            case WINTER -> "降水减少，寒冷区域更容易落雪，露天静水会逐步结冰。";
        };
    }

    private String describeVisuals(SeasonState state) {
        return switch (state.season()) {
            case SPRING -> "草地转鲜绿，冰雪逐步退去，夜晚偶尔出现柔和萤光。";
            case SUMMER -> "草木颜色更浓，热带视觉更干燥，橡树偶尔掉落苹果。";
            case AUTUMN -> "桦树、针叶林和樱花树叶会分批染成暖橙色。";
            case WINTER -> "玩家周围群系会向雪原色调刷新，树冠会出现雪花和积雪效果。";
        };
    }

    private String pageName(MessageManager messages, CalendarPage page) {
        return switch (page) {
            case OVERVIEW -> plainText(messages.rawMessage("calendar.page-overview"));
            case MONTH_TERMS -> plainText(messages.rawMessage("calendar.page-month"));
            case YEAR_TERMS -> plainText(messages.rawMessage("calendar.page-year"));
            case TEMPERATURE -> plainText(messages.rawMessage("calendar.page-temperature"));
        };
    }

    private String plainText(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
    }

    private String formatWetness(double wetness) {
        return Math.round(Math.max(0.0D, Math.min(1.0D, wetness)) * 100.0D) + "%";
    }
}

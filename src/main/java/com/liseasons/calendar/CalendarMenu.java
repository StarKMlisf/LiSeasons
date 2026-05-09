package com.liseasons.calendar;

import com.liseasons.LISeasonsPlugin;
import com.liseasons.MessageManager;
import com.liseasons.climate.TemperatureSnapshot;
import com.liseasons.config.CalendarConfig;
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
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
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
        open(player, page, sendMessage, 0);
    }

    public void open(Player player, CalendarPage page, boolean sendMessage, int pageIndex) {
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

        CalendarConfig.PageConfig pageConfig = this.plugin.getLiConfig().calendarConfig().pageConfig(page);
        int size = sizeFor(page);
        CalendarMenuHolder holder = new CalendarMenuHolder(page, pageIndex);
        Inventory inventory = Bukkit.createInventory(holder, size, messages.parseRaw(pageConfig.title()));
        holder.bind(inventory);

        renderSeasonFrame(inventory, state.season());
        renderLayout(inventory, messages, state, snapshot, page, player.getWorld().getName(), today);

        switch (page) {
            case OVERVIEW -> renderOverview(inventory, messages, state, snapshot, seasonName, termName, player.getWorld().getName(), today);
            case MONTH_TERMS -> renderMonthTerms(inventory, messages, today, state);
            case YEAR_TERMS -> renderYearTerms(inventory, messages, state);
            case TEMPERATURE -> renderTemperaturePage(inventory, messages, snapshot, state, player.getWorld().getName());
            case SEASON_EVENTS -> renderSeasonEventsPage(inventory, messages, state, pageIndex);
            case FESTIVALS -> renderFestivalsPage(inventory, messages, today, pageIndex);
        }

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

    private void fillGuiItem(Inventory inventory, CalendarConfig.GuiItemConfig item, Material material, MessageManager.Placeholder... placeholders) {
        if (item == null || !item.enabled()) {
            return;
        }
        fill(inventory, createConfiguredItem(material == null ? item.material() : material, item, placeholders), item.slot());
    }

    private ItemStack createConfiguredItem(Material material, CalendarConfig.GuiItemConfig item, MessageManager.Placeholder... placeholders) {
        return createInfoItem(
                material == null ? Material.PAPER : material,
                this.plugin.getMessageManager().parseRaw(item.name(), placeholders),
                item.lore().stream()
                        .map(line -> this.plugin.getMessageManager().parseRaw(line, placeholders))
                        .toList()
        );
    }

    private ItemStack createInfoItem(Material material, Component name, List<Component> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        stack.setItemMeta(meta);
        return stack;
    }

    private ItemStack createSimpleItem(Material material, Component name) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        stack.setItemMeta(meta);
        return stack;
    }


    private void renderLayout(Inventory inventory,
                              MessageManager messages,
                              SeasonState state,
                              TemperatureSnapshot snapshot,
                              CalendarPage page,
                              String worldName,
                              LocalDate today) {
        CalendarConfig config = this.plugin.getLiConfig().calendarConfig();
        CalendarConfig.PageConfig pageConfig = config.pageConfig(page);
        List<String> layout = pageConfig.layout();
        if (layout == null || layout.isEmpty()) {
            return;
        }
        int maxRows = Math.min(layout.size(), inventory.getSize() / 9);
        for (int row = 0; row < maxRows; row++) {
            String line = layout.get(row);
            for (int column = 0; column < 9; column++) {
                char key = column < line.length() ? line.charAt(column) : ' ';
                if (key == ' ') {
                    continue;
                }
                CalendarConfig.LayoutIconConfig icon = pageConfig.icon(String.valueOf(key));
                if (icon == null || !icon.enabled() || icon.isAir()) {
                    continue;
                }
                int slot = row * 9 + column;
                fill(inventory, createLayoutItem(icon, messages, state, snapshot, page, worldName, today), slot);
            }
        }
    }

    private List<Integer> contentSlotsFor(CalendarPage page, String contentKey) {
        CalendarConfig config = this.plugin.getLiConfig().calendarConfig();
        CalendarConfig.PageConfig pageConfig = config.pageConfig(page);
        Set<Integer> slots = new LinkedHashSet<>();
        List<String> layout = pageConfig.layout();
        if (layout != null && !layout.isEmpty()) {
            int maxRows = Math.min(layout.size(), pageConfig.size() / 9);
            for (int row = 0; row < maxRows; row++) {
                String line = layout.get(row);
                for (int column = 0; column < 9; column++) {
                    char key = column < line.length() ? line.charAt(column) : ' ';
                    if (key == ' ') {
                        continue;
                    }
                    CalendarConfig.LayoutIconConfig icon = pageConfig.icon(String.valueOf(key));
                    if (icon != null && icon.enabled() && "content".equals(icon.type())) {
                        String iconContent = icon.content() == null || icon.content().isBlank() ? "default" : icon.content();
                        if (contentKey == null || contentKey.equals(iconContent)) {
                            slots.add(row * 9 + column);
                        }
                    }
                }
            }
        }
        if (!slots.isEmpty()) {
            return List.copyOf(slots);
        }
        return pageConfig.slots(contentKey == null ? "default" : contentKey, config);
    }

    private ItemStack createLayoutItem(CalendarConfig.LayoutIconConfig icon,
                                      MessageManager messages,
                                      SeasonState state,
                                      TemperatureSnapshot snapshot,
                                      CalendarPage page,
                                      String worldName,
                                      LocalDate today) {
        Material material = materialForLayoutIcon(icon, state, snapshot, page);
        CalendarConfig.GuiItemConfig item = icon.item();
        return createConfiguredItem(material, item,
                MessageManager.Placeholder.of("season", messages.seasonName(state.season().key())),
                MessageManager.Placeholder.of("term", messages.solarTermName(state.solarTerm().key())),
                MessageManager.Placeholder.of("date", today.getMonthValue() + "月" + today.getDayOfMonth() + "日"),
                MessageManager.Placeholder.of("month", today.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA)),
                MessageManager.Placeholder.of("terms", describeMonthTerms(today.getMonthValue(), messages)),
                MessageManager.Placeholder.of("world", worldName),
                MessageManager.Placeholder.of("page", pageName(messages, page)),
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot) + " | " + snapshot.description()),
                MessageManager.Placeholder.of("air", this.plugin.getTemperatureService().formatValue(snapshot.airTemperature())),
                MessageManager.Placeholder.of("wetness", formatWetness(snapshot.wetness())),
                MessageManager.Placeholder.of("weather", describeWeather(state)),
                MessageManager.Placeholder.of("next", messages.solarTermName(state.solarTerm().next().key()) + " | " + formatMonthDay(state.solarTerm().next())),
                MessageManager.Placeholder.of("visual", describeVisuals(state)));
    }

    private Material materialForLayoutIcon(CalendarConfig.LayoutIconConfig icon,
                                           SeasonState state,
                                           TemperatureSnapshot snapshot,
                                           CalendarPage page) {
        if ("season".equals(icon.type())) {
            Season season = icon.season() == null ? state.season() : icon.season();
            return materialFor(season);
        }
        if ("page".equals(icon.type()) && icon.page() != null && icon.activeMaterial() != null && icon.page() == page) {
            return icon.activeMaterial();
        }
        if ("temperature".equals(icon.type())) {
            if (snapshot.airTemperature() <= 5.0D && icon.coldMaterial() != null) {
                return icon.coldMaterial();
            }
            if (snapshot.airTemperature() >= 30.0D && icon.hotMaterial() != null) {
                return icon.hotMaterial();
            }
        }
        return icon.material() == null ? Material.PAPER : icon.material();
    }

    private void renderOverview(Inventory inventory,
                                MessageManager messages,
                                SeasonState state,
                                TemperatureSnapshot snapshot,
                                String seasonName,
                                String termName,
                                String worldName,
                                LocalDate today) {
        CalendarConfig config = this.plugin.getLiConfig().calendarConfig();
        fillGuiItem(inventory, config.overviewItem("season"), null,
                MessageManager.Placeholder.of("season", seasonName),
                MessageManager.Placeholder.of("date", today.getMonthValue() + "月" + today.getDayOfMonth() + "日"),
                MessageManager.Placeholder.of("world", worldName));
        fillGuiItem(inventory, config.overviewItem("world"), null,
                MessageManager.Placeholder.of("world", worldName));
        fillGuiItem(inventory, config.overviewItem("month"), materialFor(state.season()),
                MessageManager.Placeholder.of("month", today.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA)),
                MessageManager.Placeholder.of("terms", describeMonthTerms(today.getMonthValue(), messages)));
        fillGuiItem(inventory, config.overviewItem("term"), null,
                MessageManager.Placeholder.of("term", termName));

        CalendarConfig.GuiItemConfig temperatureItem = config.overviewItem("temperature");
        Material temperatureMaterial = temperatureMaterial(snapshot);
        if (snapshot.airTemperature() <= 0.0D && temperatureItem.coldMaterial() != null) {
            temperatureMaterial = temperatureItem.coldMaterial();
        } else if (snapshot.airTemperature() >= 30.0D && temperatureItem.hotMaterial() != null) {
            temperatureMaterial = temperatureItem.hotMaterial();
        }
        fillGuiItem(inventory, temperatureItem, temperatureMaterial,
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot) + " | " + snapshot.description()),
                MessageManager.Placeholder.of("air", this.plugin.getTemperatureService().formatValue(snapshot.airTemperature())),
                MessageManager.Placeholder.of("wetness", formatWetness(snapshot.wetness())));
        fillGuiItem(inventory, config.overviewItem("weather"), weatherMaterial(state),
                MessageManager.Placeholder.of("weather", describeWeather(state)));
        fillGuiItem(inventory, config.overviewItem("next-term"), null,
                MessageManager.Placeholder.of("next", messages.solarTermName(state.solarTerm().next().key()) + " | " + formatMonthDay(state.solarTerm().next())));
        fillGuiItem(inventory, config.overviewItem("visual"), seasonalKeepsake(state.season()),
                MessageManager.Placeholder.of("visual", describeVisuals(state)));
    }

    private void renderMonthTerms(Inventory inventory, MessageManager messages, LocalDate today, SeasonState state) {
        CalendarConfig calendarConfig = this.plugin.getLiConfig().calendarConfig();
        CalendarConfig.MonthTermsConfig config = calendarConfig.monthTerms();
        List<Integer> slots = contentSlotsFor(CalendarPage.MONTH_TERMS, "default");
        List<SolarTerm> monthTerms = termsForMonth(today.getMonthValue());
        int index = 0;
        for (SolarTerm term : monthTerms) {
            if (index >= slots.size()) {
                break;
            }
            fill(inventory, createInfoItem(
                    term == state.solarTerm() ? config.currentMaterial() : materialFor(term),
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
            ), slots.get(index++));
        }
        fillGuiItem(inventory, config.summary(), null,
                MessageManager.Placeholder.of("month", today.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA)),
                MessageManager.Placeholder.of("terms", describeMonthTerms(today.getMonthValue(), messages)));
    }

    private void renderYearTerms(Inventory inventory, MessageManager messages, SeasonState state) {
        CalendarConfig config = this.plugin.getLiConfig().calendarConfig();
        List<Integer> slots = contentSlotsFor(CalendarPage.YEAR_TERMS, "default");
        int index = 0;
        for (SolarTerm term : SolarTerm.values()) {
            if (index >= slots.size()) {
                break;
            }
            fill(inventory, createInfoItem(
                    term == state.solarTerm() ? config.yearTerms().currentMaterial() : materialFor(term),
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
            ), slots.get(index++));
        }
    }

    private void renderTemperaturePage(Inventory inventory,
                                       MessageManager messages,
                                       TemperatureSnapshot snapshot,
                                       SeasonState state,
                                       String worldName) {
        Map<String, CalendarConfig.GuiItemConfig> items = this.plugin.getLiConfig().calendarConfig().temperaturePage();
        fillGuiItem(inventory, items.get("current"), null,
                MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot) + " | " + snapshot.description()),
                MessageManager.Placeholder.of("air", this.plugin.getTemperatureService().formatValue(snapshot.airTemperature())),
                MessageManager.Placeholder.of("wetness", formatWetness(snapshot.wetness())));
        fillGuiItem(inventory, items.get("base"), null,
                MessageManager.Placeholder.of("season", messages.seasonName(state.season().key())),
                MessageManager.Placeholder.of("base", this.plugin.getTemperatureService().formatBaseTemperature(state.season())));
        fillGuiItem(inventory, items.get("weather"), null,
                MessageManager.Placeholder.of("weather", describeWeather(state)),
                MessageManager.Placeholder.of("world", worldName));
        fillGuiItem(inventory, items.get("guide"), null);
        fillGuiItem(inventory, items.get("note"), null,
                MessageManager.Placeholder.of("term", messages.solarTermName(state.solarTerm().key())));
    }

    private void renderNavigation(Inventory inventory, MessageManager messages, CalendarPage page) {
        CalendarConfig config = this.plugin.getLiConfig().calendarConfig();
        renderNavigationItem(inventory, config.navItem("season-events"), page == CalendarPage.SEASON_EVENTS);
        renderNavigationItem(inventory, config.navItem("festivals"), page == CalendarPage.FESTIVALS);
        renderNavigationItem(inventory, config.navItem("overview"), page == CalendarPage.OVERVIEW);
        renderNavigationItem(inventory, config.navItem("close"), false);
        renderNavigationItem(inventory, config.navItem("month-terms"), page == CalendarPage.MONTH_TERMS);
        renderNavigationItem(inventory, config.navItem("year-terms"), page == CalendarPage.YEAR_TERMS);
        renderNavigationItem(inventory, config.navItem("temperature"), page == CalendarPage.TEMPERATURE);
    }

    private void renderNavigationItem(Inventory inventory, CalendarConfig.GuiItemConfig item, boolean active) {
        if (item == null || !item.enabled()) {
            return;
        }
        fillGuiItem(inventory, item, item.materialForActive(active));
    }

    private void renderSeasonFrame(Inventory inventory, Season season) {
        CalendarConfig.FrameConfig frame = this.plugin.getLiConfig().calendarConfig().seasonFrame();
        if (frame == null || !frame.enabled()) {
            return;
        }
        CalendarConfig.FrameSeasonConfig seasonFrame = frame.forSeason(season);
        if (seasonFrame == null) {
            return;
        }
        Component blank = Component.text(" ");

        for (int slot : frame.frameSlots()) {
            if (inventory.getItem(slot) == null) {
                fill(inventory, createSimpleItem(seasonFrame.frameMaterial(), blank), slot);
            }
        }

        for (int slot : frame.cornerSlots()) {
            if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == seasonFrame.frameMaterial()) {
                fill(inventory, createSimpleItem(seasonFrame.cornerMaterial(), blank), slot);
            }
        }

        for (int slot : frame.accentSlots()) {
            if (inventory.getItem(slot) == null) {
                fill(inventory, createSimpleItem(seasonFrame.accentMaterial(), blank), slot);
            }
        }
    }

    private void renderStatusBar(Inventory inventory, MessageManager messages, CalendarPage page, TemperatureSnapshot snapshot) {
        CalendarConfig.StatusBarConfig status = this.plugin.getLiConfig().calendarConfig().statusBar();
        if (status == null || !status.enabled()) {
            return;
        }
        int slot = page == CalendarPage.SEASON_EVENTS || page == CalendarPage.FESTIVALS ? status.pagedSlot() : status.slot();
        List<Component> lore = status.lore().stream()
                .map(line -> messages.parseRaw(
                        line,
                        MessageManager.Placeholder.of("page", pageName(messages, page)),
                        MessageManager.Placeholder.of("temperature", this.plugin.getTemperatureService().format(snapshot)),
                        MessageManager.Placeholder.of("wetness", formatWetness(snapshot.wetness()))
                ))
                .toList();
        fill(inventory, createInfoItem(
                status.material(),
                messages.parseRaw(status.name()),
                lore
        ), slot);
    }

    private int sizeFor(CalendarPage page) {
        return this.plugin.getLiConfig().calendarConfig().pageConfig(page).size();
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


    private Material seasonalKeepsake(Season season) {
        return switch (season) {
            case SPRING -> Material.HONEY_BOTTLE;
            case SUMMER -> Material.APPLE;
            case AUTUMN -> Material.WHEAT;
            case WINTER -> Material.POWDER_SNOW_BUCKET;
        };
    }

    private Material temperatureMaterial(TemperatureSnapshot snapshot) {
        if (snapshot.airTemperature() <= 0.0D) {
            return Material.SNOWBALL;
        }
        if (snapshot.airTemperature() >= 30.0D) {
            return Material.BLAZE_POWDER;
        }
        return Material.HONEYCOMB;
    }

    private Material weatherMaterial(SeasonState state) {
        return switch (state.season()) {
            case SPRING -> Material.WATER_BUCKET;
            case SUMMER -> Material.CAMPFIRE;
            case AUTUMN -> Material.WHEAT;
            case WINTER -> Material.POWDER_SNOW_BUCKET;
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
            case SEASON_EVENTS -> plainText(messages.rawMessage("calendar.page-events"));
            case FESTIVALS -> plainText(messages.rawMessage("calendar.page-festivals"));
        };
    }

    private String plainText(Component component) {
        return net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(component);
    }

    private String formatWetness(double wetness) {
        return Math.round(Math.max(0.0D, Math.min(1.0D, wetness)) * 100.0D) + "%";
    }

    private void renderSeasonEventsPage(Inventory inventory, MessageManager messages, SeasonState state, int pageIndex) {
        // 显示全部季节事件，分页查看，避免事件数量增加后菜单截断。
        List<com.liseasons.event.SeasonEventConfig> seasonEvents = this.plugin.getSeasonEventManager()
                .getAllEventConfigs().stream()
                .filter(com.liseasons.event.SeasonEventConfig::enabled)
                .sorted(Comparator
                        .comparing((com.liseasons.event.SeasonEventConfig config) -> config.season().ordinal())
                        .thenComparing(Comparator.comparingInt(com.liseasons.event.SeasonEventConfig::priority).reversed()))
                .toList();
        CalendarConfig calendarConfig = this.plugin.getLiConfig().calendarConfig();
        List<Integer> slots = contentSlotsFor(CalendarPage.SEASON_EVENTS, "default");
        int totalPages = totalPages(seasonEvents.size(), slots);
        int currentPage = normalizePageIndex(pageIndex, totalPages);
        List<com.liseasons.event.SeasonEventConfig> pageEvents = pageSlice(seasonEvents, currentPage, slots);

        CalendarConfig.SeasonEventsConfig gui = calendarConfig.seasonEvents();
        fillGuiItem(inventory, gui.title(), null,
                MessageManager.Placeholder.of("season", messages.seasonName(state.season().key())),
                MessageManager.Placeholder.of("count", String.valueOf(seasonEvents.size())),
                MessageManager.Placeholder.of("page", (currentPage + 1) + "/" + totalPages));

        // 显示事件列表
        int index = 0;
        for (com.liseasons.event.SeasonEventConfig event : pageEvents) {
            Material material = materialFor(event.season());
            fill(inventory, createConfiguredItem(
                    material,
                    gui.item(),
                    MessageManager.Placeholder.of("event", event.type().getDisplayName()),
                    MessageManager.Placeholder.of("season", messages.seasonName(event.season().key())),
                    MessageManager.Placeholder.of("type", event.type().getDisplayName()),
                    MessageManager.Placeholder.of("description", event.description() == null ? "" : event.description()),
                    MessageManager.Placeholder.of("trigger", formatTriggerMode(event)),
                    MessageManager.Placeholder.of("chance", formatPercent(event.chancePercent())),
                    MessageManager.Placeholder.of("priority", String.valueOf(event.priority())),
                    MessageManager.Placeholder.of("cooldown", String.valueOf(event.cooldownMillis() / 1000)),
                    MessageManager.Placeholder.of("effects", String.join("；", event.effects()))
            ), slots.get(index++));
        }

        fillGuiItem(inventory, gui.guide(), null);
        renderPagination(inventory, messages, currentPage, totalPages);
    }

    private void renderFestivalsPage(Inventory inventory, MessageManager messages, LocalDate today, int pageIndex) {
        // 显示全年全部节日，分页查看，避免只能看到本月或前 21 个。
        List<com.liseasons.festival.FestivalConfig> allFestivals = this.plugin.getFestivalManager()
                .getAllYearFestivals();
        CalendarConfig calendarConfig = this.plugin.getLiConfig().calendarConfig();
        List<Integer> slots = contentSlotsFor(CalendarPage.FESTIVALS, "default");
        int totalPages = totalPages(allFestivals.size(), slots);
        int currentPage = normalizePageIndex(pageIndex, totalPages);
        List<com.liseasons.festival.FestivalConfig> pageFestivals = pageSlice(allFestivals, currentPage, slots);

        CalendarConfig.FestivalsConfig gui = calendarConfig.festivals();
        fillGuiItem(inventory, gui.title(), null,
                MessageManager.Placeholder.of("month", today.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA)),
                MessageManager.Placeholder.of("count", String.valueOf(allFestivals.size())),
                MessageManager.Placeholder.of("page", (currentPage + 1) + "/" + totalPages));

        // 显示节日列表
        int index = 0;
        for (com.liseasons.festival.FestivalConfig festival : pageFestivals) {
            Material material = materialForFestivalCategory(festival.category());
            MonthDay festivalDate = festival.date();
            boolean isToday = festivalDate.getMonthValue() == today.getMonthValue() && 
                            festivalDate.getDayOfMonth() == today.getDayOfMonth();

            CalendarConfig.GuiItemConfig festivalItem = gui.item();
            fill(inventory, createConfiguredItem(
                    isToday && festivalItem.activeMaterial() != null ? festivalItem.activeMaterial() : material,
                    festivalItem,
                    MessageManager.Placeholder.of("festival", festival.getDisplayName()),
                    MessageManager.Placeholder.of("date", festivalDate.getMonthValue() + "月" + festivalDate.getDayOfMonth() + "日"),
                    MessageManager.Placeholder.of("category", festival.getCategoryName()),
                    MessageManager.Placeholder.of("description", festival.description() == null ? "" : festival.description()),
                    MessageManager.Placeholder.of("today", isToday ? plainText(messages.rawMessage("calendar.festival-today")) : "")
            ), slots.get(index++));
        }

        for (CalendarConfig.GuiItemConfig category : gui.categories().values()) {
            fillGuiItem(inventory, category, null);
        }
        fillGuiItem(inventory, gui.guide(), null);
        renderPagination(inventory, messages, currentPage, totalPages);
    }

    private void renderPagination(Inventory inventory, MessageManager messages, int currentPage, int totalPages) {
        CalendarConfig.PaginationConfig pagination = this.plugin.getLiConfig().calendarConfig().pagination();
        if (pagination == null) {
            return;
        }
        if (currentPage > 0 && pagination.previous() != null && pagination.previous().enabled()) {
            fillGuiItem(inventory, pagination.previous(), pagination.previous().material());
        }
        if (currentPage + 1 < totalPages && pagination.next() != null && pagination.next().enabled()) {
            fillGuiItem(inventory, pagination.next(), pagination.next().material());
        }
    }

    private Material materialForFestivalCategory(com.liseasons.festival.FestivalType.FestivalCategory category) {
        return Material.NETHER_STAR;
    }

    private String formatTriggerMode(com.liseasons.event.SeasonEventConfig event) {
        return switch (event.triggerMode()) {
            case "season", "always" -> "季节自然触发";
            case "above" -> "温度 ≥ " + event.triggerTemperature() + "°C";
            case "below" -> "温度 ≤ " + event.triggerTemperature() + "°C";
            case "range" -> "温度 " + event.triggerTemperature() + "°C ~ " + event.triggerTemperatureMax() + "°C";
            default -> "未知触发条件";
        };
    }

    private String formatPercent(double value) {
        if (Math.rint(value) == value) {
            return String.valueOf((int) value) + "%";
        }
        return String.format(Locale.ROOT, "%.1f%%", value);
    }

    private int totalPages(int size, List<Integer> slots) {
        int pageSize = Math.max(1, slots.size());
        return Math.max(1, (int) Math.ceil(size / (double) pageSize));
    }

    private int normalizePageIndex(int pageIndex, int totalPages) {
        return Math.max(0, Math.min(pageIndex, totalPages - 1));
    }

    private <T> List<T> pageSlice(List<T> values, int pageIndex, List<Integer> slots) {
        int pageSize = Math.max(1, slots.size());
        int fromIndex = Math.min(values.size(), pageIndex * pageSize);
        int toIndex = Math.min(values.size(), fromIndex + pageSize);
        return values.subList(fromIndex, toIndex);
    }

}

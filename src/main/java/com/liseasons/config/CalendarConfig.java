package com.liseasons.config;

import com.liseasons.calendar.CalendarPage;
import com.liseasons.season.Season;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public record CalendarConfig(
        String title,
        int size,
        FrameConfig seasonFrame,
        Map<String, GuiItemConfig> navigation,
        PaginationConfig pagination,
        StatusBarConfig statusBar,
        List<String> layout,
        Map<String, LayoutIconConfig> icons,
        Map<String, List<Integer>> contentSlots,
        Map<String, GuiItemConfig> overview,
        MonthTermsConfig monthTerms,
        YearTermsConfig yearTerms,
        Map<String, GuiItemConfig> temperaturePage,
        SeasonEventsConfig seasonEvents,
        FestivalsConfig festivals,
        Map<CalendarPage, PageConfig> pages
) {
    public CalendarConfig {
        size = normalizeSize(size);
        title = title == null || title.isBlank() ? "<gradient:#6b4b2a:#4f6b3c>桃源季节手账</gradient>" : title;
    }

    public GuiItemConfig navItem(String key) {
        return navigation.get(key);
    }

    public GuiItemConfig overviewItem(String key) {
        return overview.get(key);
    }

    public LayoutIconConfig icon(String key) {
        return icons.get(key);
    }

    public List<Integer> slots(String key) {
        List<Integer> slots = contentSlots.get(key);
        if (slots == null || slots.isEmpty()) {
            return contentSlots.getOrDefault("default", List.of());
        }
        return slots;
    }

    public int slotForNavigation(String key) {
        GuiItemConfig item = navItem(key);
        return item == null ? -1 : item.slot();
    }

    public int slotForOverview(String key) {
        GuiItemConfig item = overviewItem(key);
        return item == null ? -1 : item.slot();
    }

    public PageConfig pageConfig(CalendarPage page) {
        if (pages == null) {
            return PageConfig.fallback(this);
        }
        return pages.getOrDefault(page, PageConfig.fallback(this));
    }

    private static int normalizeSize(int value) {
        int normalized = Math.max(9, Math.min(54, value));
        int remainder = normalized % 9;
        if (remainder == 0) {
            return normalized;
        }
        return normalized + (9 - remainder);
    }

    public record PageConfig(
            String title,
            int size,
            List<String> layout,
            Map<String, LayoutIconConfig> icons,
            Map<String, List<Integer>> contentSlots
    ) {
        public PageConfig {
            size = normalizeSize(size);
        }

        public LayoutIconConfig icon(String key) {
            return icons == null ? null : icons.get(key);
        }

        public List<Integer> slots(CalendarConfig fallback) {
            return slots("default", fallback);
        }

        public List<Integer> slots(String key, CalendarConfig fallback) {
            if (contentSlots != null) {
                List<Integer> slots = contentSlots.get(key);
                if (slots != null && !slots.isEmpty()) {
                    return slots;
                }
                slots = contentSlots.get("default");
                if (slots != null && !slots.isEmpty()) {
                    return slots;
                }
            }
            return fallback.slots(key);
        }

        public static PageConfig fallback(CalendarConfig config) {
            return new PageConfig(config.title(), config.size(), config.layout(), config.icons(), config.contentSlots());
        }
    }

    public record GuiItemConfig(
            boolean enabled,
            int slot,
            Material material,
            Material activeMaterial,
            Material coldMaterial,
            Material hotMaterial,
            String name,
            List<String> lore
    ) {
        public Material materialForActive(boolean active) {
            if (active && activeMaterial != null) {
                return activeMaterial;
            }
            return material;
        }
    }


    public record LayoutIconConfig(
            boolean enabled,
            Material material,
            Material activeMaterial,
            Material coldMaterial,
            Material hotMaterial,
            String type,
            Season season,
            CalendarPage page,
            String content,
            GuiItemConfig item
    ) {
        public boolean isAir() {
            return material == null || material == Material.AIR;
        }
    }

    public record FrameConfig(
            boolean enabled,
            List<Integer> frameSlots,
            List<Integer> cornerSlots,
            List<Integer> accentSlots,
            Map<Season, FrameSeasonConfig> seasons
    ) {
        public FrameSeasonConfig forSeason(Season season) {
            return seasons.get(season);
        }
    }

    public record FrameSeasonConfig(
            Material frameMaterial,
            Material cornerMaterial,
            Material accentMaterial
    ) {
    }

    public record PaginationConfig(
            GuiItemConfig previous,
            GuiItemConfig next
    ) {
    }

    public record StatusBarConfig(
            boolean enabled,
            int slot,
            int pagedSlot,
            Material material,
            String name,
            List<String> lore
    ) {
    }

    public record MonthTermsConfig(
            int startSlot,
            Material currentMaterial,
            Material termMaterial,
            GuiItemConfig summary
    ) {
    }

    public record YearTermsConfig(
            Material currentMaterial,
            Material termMaterial
    ) {
    }

    public record SeasonEventsConfig(
            GuiItemConfig title,
            GuiItemConfig item,
            GuiItemConfig guide
    ) {
    }

    public record FestivalsConfig(
            GuiItemConfig title,
            GuiItemConfig item,
            Map<String, GuiItemConfig> categories,
            GuiItemConfig guide
    ) {
    }

    public static String navigationKey(CalendarPage page) {
        return switch (page) {
            case OVERVIEW -> "overview";
            case MONTH_TERMS -> "month-terms";
            case YEAR_TERMS -> "year-terms";
            case TEMPERATURE -> "temperature";
            case SEASON_EVENTS -> "season-events";
            case FESTIVALS -> "festivals";
        };
    }
}

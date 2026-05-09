package com.liseasons.config;

public record CalendarConfig(
        String title,
        int size
) {
    public CalendarConfig {
        size = normalizeSize(size);
        title = title == null || title.isBlank() ? "<gradient:#7fd37f:#f7c873>桃源四季历</gradient>" : title;
    }

    private static int normalizeSize(int value) {
        int normalized = Math.max(9, Math.min(54, value));
        int remainder = normalized % 9;
        if (remainder == 0) {
            return normalized;
        }
        return normalized + (9 - remainder);
    }
}

package com.liseasons.season;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum Season {
    SPRING("spring"),
    SUMMER("summer"),
    AUTUMN("autumn"),
    WINTER("winter");

    private final String key;

    Season(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }

    public static Optional<Season> fromKey(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(season -> season.key.equals(normalized))
                .findFirst();
    }
}

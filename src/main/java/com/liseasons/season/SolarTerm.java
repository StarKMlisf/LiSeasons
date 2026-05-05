package com.liseasons.season;

import java.time.MonthDay;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum SolarTerm {
    LICHUN("lichun", Season.SPRING, MonthDay.of(2, 4)),
    YUSHUI("yushui", Season.SPRING, MonthDay.of(2, 19)),
    JINGZHE("jingzhe", Season.SPRING, MonthDay.of(3, 6)),
    CHUNFEN("chunfen", Season.SPRING, MonthDay.of(3, 21)),
    QINGMING("qingming", Season.SPRING, MonthDay.of(4, 5)),
    GUYU("guyu", Season.SPRING, MonthDay.of(4, 20)),
    LIXIA("lixia", Season.SUMMER, MonthDay.of(5, 6)),
    XIAOMAN("xiaoman", Season.SUMMER, MonthDay.of(5, 21)),
    MANGZHONG("mangzhong", Season.SUMMER, MonthDay.of(6, 6)),
    XIAZHI("xiazhi", Season.SUMMER, MonthDay.of(6, 21)),
    XIAOSHU("xiaoshu", Season.SUMMER, MonthDay.of(7, 7)),
    DASHU("dashu", Season.SUMMER, MonthDay.of(7, 23)),
    LIQIU("liqiu", Season.AUTUMN, MonthDay.of(8, 8)),
    CHUSHU("chushu", Season.AUTUMN, MonthDay.of(8, 23)),
    BAILU("bailu", Season.AUTUMN, MonthDay.of(9, 8)),
    QIUFEN("qiufen", Season.AUTUMN, MonthDay.of(9, 23)),
    HANLU("hanlu", Season.AUTUMN, MonthDay.of(10, 8)),
    SHUANGJIANG("shuangjiang", Season.AUTUMN, MonthDay.of(10, 24)),
    LIDONG("lidong", Season.WINTER, MonthDay.of(11, 8)),
    XIAOXUE("xiaoxue", Season.WINTER, MonthDay.of(11, 22)),
    DAXUE("daxue", Season.WINTER, MonthDay.of(12, 7)),
    DONGZHI("dongzhi", Season.WINTER, MonthDay.of(12, 22)),
    XIAOHAN("xiaohan", Season.WINTER, MonthDay.of(1, 5)),
    DAHAN("dahan", Season.WINTER, MonthDay.of(1, 20));

    private static final SolarTerm[] VALUES = values();

    private final String key;
    private final Season season;
    private final MonthDay defaultDate;

    SolarTerm(String key, Season season, MonthDay defaultDate) {
        this.key = key;
        this.season = season;
        this.defaultDate = defaultDate;
    }

    public String key() {
        return this.key;
    }

    public Season season() {
        return this.season;
    }

    public MonthDay defaultDate() {
        return this.defaultDate;
    }

    public SolarTerm next() {
        return VALUES[(this.ordinal() + 1) % VALUES.length];
    }

    public static SolarTerm firstOf(Season season) {
        return Arrays.stream(VALUES)
                .filter(term -> term.season == season)
                .findFirst()
                .orElse(LICHUN);
    }

    public static Optional<SolarTerm> fromKey(String input) {
        if (input == null || input.isBlank()) {
            return Optional.empty();
        }
        String normalized = input.toLowerCase(Locale.ROOT);
        return Arrays.stream(VALUES)
                .filter(term -> term.key.equals(normalized))
                .findFirst();
    }
}

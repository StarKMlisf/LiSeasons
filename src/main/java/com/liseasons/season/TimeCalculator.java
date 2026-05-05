package com.liseasons.season;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.EnumMap;
import java.util.Map;

public final class TimeCalculator {
    private final boolean useGregorianTrigger;
    private final Map<Season, Integer> seasonDurationDays;
    private final Map<Season, MonthDay> seasonStartDates;
    private final Map<SolarTerm, MonthDay> gregorianDates;

    public TimeCalculator(boolean useGregorianTrigger,
                          Map<Season, Integer> seasonDurationDays,
                          Map<Season, MonthDay> seasonStartDates,
                          Map<SolarTerm, MonthDay> gregorianDates) {
        this.useGregorianTrigger = useGregorianTrigger;
        this.seasonDurationDays = new EnumMap<>(seasonDurationDays);
        this.seasonStartDates = new EnumMap<>(seasonStartDates);
        this.gregorianDates = new EnumMap<>(gregorianDates);
    }

    public SeasonState calculate(long fullTime, LocalDate today) {
        if (this.useGregorianTrigger) {
            return calculateByGregorian(today);
        }
        return calculateByWorldTime(fullTime);
    }

    public SeasonState calculateByGregorian(LocalDate today) {
        MonthDay current = MonthDay.from(today);
        Season season = resolveSeason(current);
        SolarTerm matched = SolarTerm.XIAOHAN;

        for (SolarTerm term : SolarTerm.values()) {
            MonthDay configured = this.gregorianDates.getOrDefault(term, term.defaultDate());
            if (configured.getMonthValue() == 1) {
                if (current.getMonthValue() == 1 && isAfterOrSame(current, configured)) {
                    matched = term;
                }
                continue;
            }

            if (current.getMonthValue() == 1) {
                continue;
            }

            if (isAfterOrSame(current, configured)) {
                matched = term;
                continue;
            }
            break;
        }

        return new SeasonState(season, matched);
    }

    public SeasonState calculateByWorldTime(long fullTime) {
        long worldDays = Math.max(0L, fullTime / 24000L);
        int totalSeasonDays = this.seasonDurationDays.values().stream().mapToInt(Integer::intValue).sum();
        if (totalSeasonDays <= 0) {
            return new SeasonState(Season.SPRING, SolarTerm.LICHUN);
        }

        int dayInCycle = (int) (worldDays % totalSeasonDays);
        int cursor = 0;

        for (Season season : Season.values()) {
            int duration = Math.max(1, this.seasonDurationDays.getOrDefault(season, 24));
            if (dayInCycle < cursor + duration) {
                int dayInSeason = dayInCycle - cursor;
                return new SeasonState(season, resolveTermInSeason(season, dayInSeason, duration));
            }
            cursor += duration;
        }

        return new SeasonState(Season.SPRING, SolarTerm.LICHUN);
    }

    private SolarTerm resolveTermInSeason(Season season, int dayInSeason, int seasonDuration) {
        int termSpan = Math.max(1, seasonDuration / 6);
        int termIndex = Math.min(5, dayInSeason / termSpan);
        SolarTerm[] terms = SolarTerm.values();
        int found = 0;

        for (SolarTerm term : terms) {
            if (term.season() != season) {
                continue;
            }
            if (found == termIndex) {
                return term;
            }
            found++;
        }

        return SolarTerm.firstOf(season);
    }

    private boolean isAfterOrSame(MonthDay current, MonthDay target) {
        if (current.equals(target)) {
            return true;
        }
        return current.isAfter(target);
    }

    private Season resolveSeason(MonthDay current) {
        MonthDay springStart = this.seasonStartDates.getOrDefault(Season.SPRING, MonthDay.of(3, 4));
        MonthDay summerStart = this.seasonStartDates.getOrDefault(Season.SUMMER, MonthDay.of(6, 4));
        MonthDay autumnStart = this.seasonStartDates.getOrDefault(Season.AUTUMN, MonthDay.of(9, 4));
        MonthDay winterStart = this.seasonStartDates.getOrDefault(Season.WINTER, MonthDay.of(12, 4));

        if (isAfterOrSame(current, winterStart) || current.isBefore(springStart)) {
            return Season.WINTER;
        }
        if (isAfterOrSame(current, autumnStart)) {
            return Season.AUTUMN;
        }
        if (isAfterOrSame(current, summerStart)) {
            return Season.SUMMER;
        }
        return Season.SPRING;
    }
}

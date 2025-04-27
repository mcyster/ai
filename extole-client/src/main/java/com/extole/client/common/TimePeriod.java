package com.extole.client.common;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimePeriod {
    private static final Pattern PERIOD_ONLY_PATTERN = Pattern.compile("^P\\d+[YMWD].*$");
    private static final Pattern DATE_PERIOD_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2})/P\\d+[YMWD].*$");
    private static final Pattern DATE_RANGE_PATTERN = Pattern
            .compile("^(\\d{4}-\\d{2}-\\d{2})/(\\d{4}-\\d{2}-\\d{2})$");
    private static final Pattern NAMED_RANGE_PATTERN = Pattern.compile("^[A-Z0-9_]+$");

    private final String input;

    public TimePeriod(String input) {
        this.input = input;

    }

    public String convertToTimeRange() {
        Matcher periodOnlyMatcher = PERIOD_ONLY_PATTERN.matcher(input);
        Matcher datePeriodMatcher = DATE_PERIOD_PATTERN.matcher(input);
        Matcher dateRangeMatcher = DATE_RANGE_PATTERN.matcher(input);

        if (periodOnlyMatcher.matches()) {
            return processPeriodOnly(input);
        } else if (datePeriodMatcher.matches()) {
            return processDatePeriod(datePeriodMatcher);
        } else if (dateRangeMatcher.matches()) {
            return processDateRange(dateRangeMatcher);
        }

        if (NAMED_RANGE_PATTERN.matcher(input).matches()) {
            return input;
        }

        throw new IllegalArgumentException("Unsupported format: " + input);
    }

    private static String processPeriodOnly(String input) {
        Period period = Period.parse(input);
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minus(period);
        return formatRange(startDate, endDate);
    }

    private static String processDatePeriod(Matcher matcher) {
        LocalDate startDate = LocalDate.parse(matcher.group(1));
        Period period = Period.parse(matcher.group(0).split("/")[1]);
        LocalDate endDate = startDate.plus(period);
        return formatRange(startDate, endDate);
    }

    private static String processDateRange(Matcher matcher) {
        LocalDate startDate = LocalDate.parse(matcher.group(1));
        LocalDate endDate = LocalDate.parse(matcher.group(2));
        return formatRange(startDate, endDate);
    }

    private static String formatRange(LocalDate start, LocalDate end) {
        return start + "/" + end;
    }
}

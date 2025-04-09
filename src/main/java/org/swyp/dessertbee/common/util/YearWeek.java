package org.swyp.dessertbee.common.util;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class YearWeek {
    public static String from(LocalDate date) {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        int weekNumber = date.get(weekFields.weekOfWeekBasedYear());
        int year = date.get(weekFields.weekBasedYear());
        return String.format("%d-W%02d", year, weekNumber);
    }
}

package no.flaming_adventure;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Util {
    static public final Locale LOCALE = new Locale("NO", "no");

    static public DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);

    static public NumberFormat NUMBER_FORMAT_PERCENT = NumberFormat.getPercentInstance(LOCALE);
}

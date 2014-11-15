package no.flaming_adventure;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class Util {
    static public final Locale LOCALE = new Locale("NO", "no");

    static private DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);

    static public class DateCell<X> extends TableCell<X, LocalDate> {
        @Override protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                setText(item.format(DATE_TIME_FORMATTER));
            } else {
                setText(null);
            }
        }
    }

    static public class DateCellFactory<X> implements Callback<TableColumn<X, LocalDate>, TableCell<X, LocalDate>> {
        @Override public TableCell<X, LocalDate> call(TableColumn<X, LocalDate> param) {
            return new DateCell<>();
        }
    }
}

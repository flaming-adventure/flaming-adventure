package no.flaming_adventure;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.function.Consumer;

public class Util {
    static public final Locale LOCALE = new Locale("NO", "no");

    static private DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(LOCALE);

    static private NumberFormat NUMBER_FORMAT_PERCENT = NumberFormat.getPercentInstance(LOCALE);

    static public final class PercentageCell<X> extends TableCell<X, Number> {
        @Override protected void updateItem(Number item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                setText(NUMBER_FORMAT_PERCENT.format(item));
            } else {
                setText(null);
            }
        }
    }

    static public <T> ListChangeListener<T> listUpdateListener(Consumer<T> consumer) {
        return change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    change.getList().subList(change.getFrom(), change.getTo()).forEach(consumer);
                }
            }
        };
    }

    static public final class PercentageCellFactory<X>
            implements Callback<TableColumn<X, Number>, TableCell<X, Number>> {
        @Override public TableCell<X, Number> call(TableColumn<X, Number> param) {
            return new PercentageCell<>();
        }
    }

    static public final class DateCell<X> extends TableCell<X, LocalDate> {
        @Override protected void updateItem(LocalDate item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null) {
                setText(item.format(DATE_TIME_FORMATTER));
            } else {
                setText(null);
            }
        }
    }

    static public final class DateCellFactory<X>
            implements Callback<TableColumn<X, LocalDate>, TableCell<X, LocalDate>> {
        @Override public TableCell<X, LocalDate> call(TableColumn<X, LocalDate> param) {
            return new DateCell<>();
        }
    }
}

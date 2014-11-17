package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import no.flaming_adventure.App;

import java.time.LocalDate;

/**
 * A date cell displaying the a date formatted by the application's {@link no.flaming_adventure.App#DATE_TIME_FORMATTER
 * date formatter}.
 *
 * @param <T> the type of the table's records.
 */
public class DateCell<T> extends TableCell<T, LocalDate> {
    @Override protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            setText(item.format(App.DATE_TIME_FORMATTER));
        } else {
            setText(null);
        }
    }
}

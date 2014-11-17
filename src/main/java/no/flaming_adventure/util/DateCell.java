package no.flaming_adventure.util;

import javafx.scene.control.TableCell;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A date cell displaying the date with a given format.
 *
 * @param <T> the type of the table's records.
 */
public class DateCell<T> extends TableCell<T, LocalDate> {

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    private final DateTimeFormatter formatter;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Construct a DateCell using the given formatter.
     *
     * @param formatter a date formatter.
     */
    public DateCell(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    @Override protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            setText(item.format(formatter));
        } else {
            setText(null);
        }
    }
}

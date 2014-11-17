package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * A {@link no.flaming_adventure.util.DateCell date cell} factory producing date cells
 * with a given {@link java.time.format.DateTimeFormatter date formatter}.
 *
 * @param <T> the type of the table records.
 */
public class DateCellFactory<T>
        implements Callback<TableColumn<T, LocalDate>, TableCell<T, LocalDate>> {

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
     * Construct a factory producing date cells with the given formatter.
     *
     * @param formatter the formatter that should be used in the produced date cells.
     */
    public DateCellFactory(DateTimeFormatter formatter) {
        this.formatter = formatter;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Return a date cell using the factory's date formatter.
     *
     * @param param a table column.
     * @return a {@link no.flaming_adventure.util.DateCell date cell}.
     */
    @Override public TableCell<T, LocalDate> call(TableColumn<T, LocalDate> param) {
        return new DateCell<>(formatter);
    }
}

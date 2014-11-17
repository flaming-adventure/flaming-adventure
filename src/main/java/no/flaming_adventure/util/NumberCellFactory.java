package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.text.NumberFormat;

/**
 * A {@link no.flaming_adventure.util.NumberCell number cell} factory producing number
 * cells with a given {@link java.text.NumberFormat format}.
 *
 * @param <T> the type of the table records.
 */
public class NumberCellFactory<T>
        implements Callback<TableColumn<T, Number>, TableCell<T, Number>> {

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    private final NumberFormat numberFormat;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Construct a number cell factory using the given number format.
     *
     * @param numberFormat the number format to be used for all cells produced by this
     *                     factory.
     */
    public NumberCellFactory(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Return a number cell using this factory's number format.
     *
     * @param param a table column.
     * @return a table cell.
     */
    @Override public TableCell<T, Number> call(TableColumn<T, Number> param) {
        return new NumberCell<>(numberFormat);
    }
}

package no.flaming_adventure.util;

import javafx.scene.control.TableCell;

import java.text.NumberFormat;

/**
 * A table cell displaying a formatted number.
 *
 * @param <T> the type of the {@link javafx.scene.control.TableView table view}'s records.
 */
public class NumberCell<T> extends TableCell<T, Number> {

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
     * Construct a table cell using the given number format.
     *
     * @param numberFormat a number format.
     */
    public NumberCell(NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }

    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    @Override protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            setText(numberFormat.format(item));
        } else {
            setText(null);
        }
    }
}

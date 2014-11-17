package no.flaming_adventure.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.function.Consumer;

public class SQLSortPolicy<S> implements Callback<TableView<S>, Boolean> {

    /***************************************************************************
     *                                                                         *
     * Static properties and methods                                           *
     *                                                                         *
     **************************************************************************/

    /**
     * Return a string containing the SQL sort direction corresponding to the given sort type.
     *
     * @param sortType a sort type.
     * @return either "ASC" or "DESC".
     */
    static private String directionFromSortType(TableColumn.SortType sortType) {
        if (sortType == TableColumn.SortType.ASCENDING) {
            return "ASC";
        } else {
            return "DESC";
        }
    }

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    /**
     * A callable that takes an SQL ordering (the parameters to an <code>ORDER BY</code> clause).
     *
     * <p> This will be called with the ordering determined by the table view's state.
     */
    private final Consumer<String> dataLoader;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public SQLSortPolicy(Consumer<String> dataLoader) {
        this.dataLoader = dataLoader;
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Perform the sort operation.
     *
     * <p> The function builds a parameter for an SQL <code>ORDER BY</code> clause based on the ordering of
     * the given table view. That parameter string is then passed to the policy's {@link #dataLoader data loader},
     * which is responsible for the actual sort.
     *
     * <p> Note that database column names are extracted from the table view's column IDs. These should be set using
     * {@link javafx.scene.control.TableColumn#setId(String)} for the sorting to work.
     *
     * @param tableView the table view to sort.
     * @return <code>true</code>.
     */
    @Override public Boolean call(TableView<S> tableView) {
        // The table view's sort order is the list of all the columns to sort by.
        ObservableList<TableColumn<S, ?>> sortOrder = tableView.getSortOrder();
        if (sortOrder.isEmpty()) {
            return true;
        }

        // Build the SQL `ORDER BY` parameter.
        StringBuilder builder = new StringBuilder("");
        String prefix = "";
        for (TableColumn<?, ?> column : sortOrder) {
            if (column.getId() == null) { continue; }
            builder.append(prefix);
            prefix = ", ";
            builder.append(column.getId()).append(' ').append(directionFromSortType(column.getSortType()));
        }
        String ordering = builder.toString();

        if (ordering.isEmpty()) {
            ordering = null;
        } else {
            ordering = ordering;
        }
        dataLoader.accept(ordering);
        return true;
    }
}

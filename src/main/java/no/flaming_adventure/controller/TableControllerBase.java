package no.flaming_adventure.controller;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.util.ListUpdateListener;
import no.flaming_adventure.util.SQLSortPolicy;

public abstract class TableControllerBase<T> {

    /***************************************************************************
     *                                                                         *
     * Static variables and methods                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * {@link javafx.event.EventHandler Event handler} for the
     * {@link javafx.scene.control.TableView table view}'s sort events.
     *
     * <p> This is required for the {@link no.flaming_adventure.util.SQLSortPolicy default
     * sort policy} to work as otherwise the table view might try to reselect one or more
     * items that no longer exist after the sort.
     *
     * @see TableView#setOnSort
     * @see TableView#setSortPolicy
     * @param sortEvent a sort event.
     * @param <T> the type of the object contained in the table.
     */
    protected static <T> void onSortEventHandler(SortEvent<TableView<T>> sortEvent) {
        sortEvent.getSource().getSelectionModel().clearSelection();
    }

    /**
     * The number of items on a table page.
     */
    protected static final Integer ITEMS_PER_PAGE = 50;


    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    /**
     * The table's data model, should be injected in {@link #inject}.
     */
    protected DataModel dataModel;

    /**
     * The table's list of items.
     */
    protected final ObservableList<T> items;

    /**
     * A string used as parameter to an SQL <code>ORDER BY</code> clause.
     */
    protected String ordering = null;

    /**
     * The {@link #items item list} should only be updated by user code if this is false.
     *
     * <p> This exists so that we can avoid updating the item list multiple times on what
     * will seem like a single event to the user.
     */
    protected Boolean dataLock = false;

    /**
     * The JavaFX table view displaying the {@link #items item list}.
     */
    @FXML protected TableView<T> tableView;

    /**
     * A JavaFX pagination control.
     *
     * @see #loadPage(Integer)
     */
    @FXML protected Pagination pagination;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * Construct a table controller base with no editable fields.
     */
    protected TableControllerBase() {
        this(null);
    }

    /**
     * Construct a table controller base with editable fields.
     *
     * <p> The update behaviour of the table can be specified by overriding the
     * #updateItem method.
     *
     * @param extractor element to Observable[] converter. The Observable[] is used to
     *                  listen for list updates.
     */
    protected TableControllerBase(Callback<T, Observable[]> extractor) {
        if (extractor == null) {
            items = FXCollections.observableArrayList();
        } else {
            items = FXCollections.observableArrayList(extractor);
            items.addListener(new ListUpdateListener<T>(this::updateItem));
        }
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public void inject(DataModel dataModel) {
        this.dataModel = dataModel;

        tableView.setOnSort(TableControllerBase::onSortEventHandler);
        tableView.setSortPolicy(new SQLSortPolicy<>(this::setOrdering));

        pagination.currentPageIndexProperty().addListener(this::currentPageIndexPropertyListener);
    }

    public void load() {
        ordering = null;
        tableView.getSortOrder().clear();
        loadPage(0);
    }

    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    protected void updateItem(T item) { }

    @FXML protected void initialize() {
        tableView.setItems(items);
    }

    protected final void setOrdering(String ordering) {
        this.ordering = ordering;
        loadPage(0);
    }

    protected void currentPageIndexPropertyListener(ObservableValue<? extends Number> observable,
                                                    Number oldValue, Number newValue) {
        if (! newValue.equals(oldValue)) { loadPage(newValue.intValue()); }
    }

    protected final void setPageCount(Integer itemCount) {
        if (itemCount == null || itemCount == 0) {
            pagination.setPageCount(1);
        } else {
            // Ceiling[itemCount / ITEMS_PER_PAGE]
            pagination.setPageCount((itemCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        }
    }

    protected final void loadPage(Integer pageIndex) {
        if (!dataLock) { loadPageImpl(pageIndex); }
    }

    protected abstract void loadPageImpl(Integer pageIndex);
}

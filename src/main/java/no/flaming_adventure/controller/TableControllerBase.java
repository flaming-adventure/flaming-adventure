package no.flaming_adventure.controller;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableView;
import no.flaming_adventure.SQLSortPolicy;
import no.flaming_adventure.model.DataModel;

import java.util.function.Consumer;

public abstract class TableControllerBase<T> {

    /***************************************************************************
     *                                                                         *
     * Static variables and methods                                            *
     *                                                                         *
     **************************************************************************/

    protected static <T> void onSortEventHandler(SortEvent<TableView<T>> sortEvent) {
        sortEvent.getSource().getSelectionModel().clearSelection();
    }

    protected static final Integer ITEMS_PER_PAGE = 50;


    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    /* Injected fields. */
    protected DataModel           dataModel;
    protected Consumer<Throwable> unhandledExceptionHook;

    protected final ObservableList<T> items = FXCollections.observableArrayList();

    protected String ordering   = null;
    protected Boolean dataLock  = false;

    @FXML protected TableView<T>  tableView;
    @FXML protected Pagination    pagination;

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;

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

    @FXML protected void initialize() {
        tableView.setItems(items);
    }

    protected void setOrdering(String ordering) {
        this.ordering = ordering;
        loadPage(0);
    }

    protected void currentPageIndexPropertyListener(ObservableValue<? extends Number> observable,
                                                    Number oldValue, Number newValue) {
        if (! newValue.equals(oldValue)) { loadPage(newValue.intValue()); }
    }

    protected void setPageCount(Integer itemCount) {
        if (itemCount == null) {
            pagination.setPageCount(1);
        } else {
            // Ceiling[itemCount / ITEMS_PER_PAGE]
            pagination.setPageCount((itemCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        }
    }

    protected void loadPage(Integer pageIndex) {
        if (!dataLock) { loadPageImpl(pageIndex); }
    }

    protected abstract void loadPageImpl(Integer pageIndex);
}

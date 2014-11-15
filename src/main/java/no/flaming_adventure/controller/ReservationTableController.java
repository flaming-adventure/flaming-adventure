package no.flaming_adventure.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.SQLSortPolicy;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Controller for the reservation table view.
 */
public class ReservationTableController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    private static final Integer ITEMS_PER_PAGE = 50;
    private static final Hut ALL_HUTS = new Hut(-1, "ALLE", 0, 0);

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    /* Injected fields. */
    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    private String ordering = null;

    @FXML private ComboBox<Hut> hutFilter;
    @FXML private DatePicker    fromDateFilter;
    @FXML private DatePicker    toDateFilter;

    private final ObservableList<Reservation> reservations = FXCollections.observableArrayList();

    @FXML private TableView<Reservation>              tableView;
    @FXML private TableColumn<Reservation, String>    hutColumn;
    @FXML private TableColumn<Reservation, LocalDate> dateColumn;
    @FXML private TableColumn<Reservation, String>    nameColumn;
    @FXML private TableColumn<Reservation, String>    emailColumn;
    @FXML private TableColumn<Reservation, Number>    countColumn;
    @FXML private TableColumn<Reservation, String>    commentColumn;

    @FXML private Pagination pagination;

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;

        // This is necessary so that we don't try to reselect a row that no longer exists.
        tableView.setOnSort(e -> tableView.getSelectionModel().clearSelection());
        tableView.setSortPolicy(new SQLSortPolicy<>(this::setOrdering));

        hutFilter.setOnAction(event -> setData());
        fromDateFilter.setOnAction(event -> setData());
        toDateFilter.setOnAction(event -> setData());

        pagination.currentPageIndexProperty()
                  .addListener((observable, oldValue, newValue) -> setData(newValue.intValue()));
    }

    public void load() {
        ObservableList<Hut> huts;
        try {
            huts = dataModel.getHuts();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        hutFilter.setItems(huts);
        hutFilter.getItems().add(0, ALL_HUTS);

        pagination.setCurrentPageIndex(0);
        // This triggers loadPage(0).
        hutFilter.setValue(ALL_HUTS);
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    /**
     * JavaFX initialization method.
     *
     * <p> This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by
     * user code.
     */
    @FXML private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        dateColumn.setCellFactory(new Util.DateCellFactory<>());
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().emailProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());

        // Set column IDs to the names of database columns.
        dateColumn.setId("date");
        nameColumn.setId("name");
        emailColumn.setId("email");
        countColumn.setId("count");
        commentColumn.setId("comment");

        tableView.setItems(reservations);
    }

    private void setOrdering(String ordering) {
        this.ordering = ordering;
        setData();
    }

    private void setData() {
        Hut hut = hutFilter.getValue();
        if (hut == ALL_HUTS) { hut = null; }
        setData(0, ordering, hut, fromDateFilter.getValue(), toDateFilter.getValue());
    }

    private void setData(Integer pageIndex) {
        Hut hut = hutFilter.getValue();
        if (hut == ALL_HUTS) { hut = null; }
        setData(pageIndex, ordering, hut, fromDateFilter.getValue(), toDateFilter.getValue());
    }

    private void setData(Integer pageIndex, String ordering, Hut hutFilter,
                         LocalDate fromDateFilter, LocalDate toDateFilter) {
        Integer reservationCount;
        ObservableList<Reservation> reservations;
        try {
            reservationCount = dataModel.reservationCount(hutFilter, fromDateFilter, toDateFilter);
            reservations = dataModel.reservationPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE,
                                                     hutFilter, fromDateFilter, toDateFilter, ordering);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        this.reservations.setAll(reservations);

        if (reservationCount == 0) {
            // There should always be at least a single page.
            pagination.setPageCount(1);
        } else {
            // Ceiling[reservationCount / ITEMS_PER_PAGE]
            pagination.setPageCount((reservationCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        }

        pagination.setCurrentPageIndex(pageIndex);
    }
}

package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

    @FXML private ComboBox<Hut> hutFilter;
    @FXML private DatePicker    fromDateFilter;
    @FXML private DatePicker    toDateFilter;

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

        hutFilter.setOnAction(e -> loadPage(0));
        fromDateFilter.setOnAction(e -> loadPage(0));
        toDateFilter.setOnAction(e -> loadPage(0));

        pagination.currentPageIndexProperty()
                  .addListener((observable, oldValue, newValue) -> loadPage(newValue.intValue()));
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
    }

    private void loadPage(Integer pageIndex) {
        Hut hut = hutFilter.getValue();
        if (hut == ALL_HUTS) { hut = null; }
        LocalDate fromDate = fromDateFilter.getValue();
        LocalDate toDate = toDateFilter.getValue();

        Integer reservationCount;
        ObservableList<Reservation> reservations;
        try {
            reservationCount = dataModel.reservationCount(hut, fromDate, toDate);
            reservations = dataModel.reservationPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE,
                    hut, fromDate, toDate, null);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(reservations);

        if (reservationCount == 0) {
            pagination.setPageCount(1);
        } else {
            pagination.setPageCount((reservationCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        }

        pagination.setCurrentPageIndex(pageIndex);
    }
}

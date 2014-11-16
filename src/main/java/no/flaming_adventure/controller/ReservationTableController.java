package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.model.Reservation;
import no.flaming_adventure.util.DateCellFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Controller for the reservation table view.
 */
public class ReservationTableController extends TableControllerBase<Reservation> {

    /***************************************************************************
     *                                                                         *
     * Static variables and methods                                            *
     *                                                                         *
     **************************************************************************/

    private static final Hut HUT_FILTER_NO_SELECTION = new Hut(-1, "<ALLE>", 0, 0);

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    @FXML private ComboBox<Hut> hutFilter;
    @FXML private DatePicker    fromDateFilter;
    @FXML private DatePicker    toDateFilter;

    @FXML private TableColumn<Reservation, String>    hutColumn;
    @FXML private TableColumn<Reservation, LocalDate> dateColumn;
    @FXML private TableColumn<Reservation, String>    nameColumn;
    @FXML private TableColumn<Reservation, String>    emailColumn;
    @FXML private TableColumn<Reservation, Number>    countColumn;
    @FXML private TableColumn<Reservation, String>    commentColumn;

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        super.inject(dataModel, unhandledExceptionHook);

        hutFilter.setOnAction(this::setDataEventHandler);
        fromDateFilter.setOnAction(this::setDataEventHandler);
        toDateFilter.setOnAction(this::setDataEventHandler);
    }

    @Override public void load() {
        ObservableList<Hut> huts;
        try {
            huts = dataModel.getHuts();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        hutFilter.getItems().clear();
        hutFilter.getItems().add(HUT_FILTER_NO_SELECTION);
        hutFilter.getItems().addAll(huts);

        // Don't load the data just because we changed the filter.
        dataLock = true;
        hutFilter.getSelectionModel().selectFirst();
        dataLock = false;

        super.load();
    }

    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    /**
     * JavaFX initialization method.
     *
     * <p> This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by
     * user code.
     */
    @Override @FXML protected void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        dateColumn.setCellFactory(new DateCellFactory<>());
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().emailProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());

        hutColumn.setId("huts.name");
        dateColumn.setId("reservations.date");
        nameColumn.setId("reservations.name");
        emailColumn.setId("reservations.email");
        countColumn.setId("reservations.count");
        commentColumn.setId("reservations.comment");

        super.initialize();
    }

    private void setDataEventHandler(ActionEvent event) {
        loadPage(0);
    }

    @Override protected void loadPageImpl(Integer pageIndex) {
        Hut hut = hutFilter.getValue();
        if (hut == HUT_FILTER_NO_SELECTION) { hut = null; }
        setData(pageIndex, hut, fromDateFilter.getValue(), toDateFilter.getValue());
    }

    private void setData(Integer pageIndex, Hut hutFilter, LocalDate fromDateFilter, LocalDate toDateFilter) {
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

        this.items.setAll(reservations);

        setPageCount(reservationCount);

        pagination.setCurrentPageIndex(pageIndex);
    }
}

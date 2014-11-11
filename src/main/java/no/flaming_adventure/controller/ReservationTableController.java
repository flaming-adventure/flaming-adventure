package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Controller for the reservation table view.
 * <ul>
 *     <li>TODO (low priority): inline editing of reservations.
 *     <li>TODO (low priority): deletion/deactivation of reservations.
 * </ul>
 */
public class ReservationTableController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    private static final Integer ITEMS_PER_PAGE = 50;

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    /* Injected fields. */
    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

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
    }

    public void load() {
        pagination.currentPageIndexProperty()
                .addListener((observable, oldValue, newValue) -> loadPage(newValue.intValue()));
        loadPage(0);
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
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().emailProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
    }

    private void loadPage(Integer pageIndex) {
        Integer reservationCount;
        ObservableList<Reservation> reservations;
        try {
            reservationCount = dataModel.reservationCount();
            reservations = dataModel.reservationPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(reservations);

        pagination.setPageCount((reservationCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        pagination.setCurrentPageIndex(pageIndex);
    }
}

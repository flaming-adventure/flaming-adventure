package no.flaming_adventure.controller;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Reservation;

import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for the reservation table view.
 */
public class ReservationTableController {
    /**
     * Date cell for the dateColumn.
     *
     * Responsible for date formatting.
     *
     * TODO: use custom, application wide, date formatting.
     */
    static private final class DateCell extends TableCell<Reservation, Date> {
        @Override
        protected void updateItem(Date date, boolean empty) {
            super.updateItem(date, empty);

            if (date == null) {
                setText(null);
            } else {
                setText(DateFormat.getDateInstance().format(date));
            }
        }
    }

    /** Default value for the from date filter. */
    static private final LocalDate defaultFromDate = LocalDate.now();
    /** Default value for the to date filter. */
    static private final LocalDate defaultToDate   = LocalDate.now().plusYears(1);

    @FXML private DatePicker filterFromDatePicker;
    @FXML private DatePicker filterToDatePicker;

    /* The main table widgets.
     *
     * TODO (low priority): inline editing of reservations.
     * TODO (low priority): deletion/deactivation of reservations.
     */
    @FXML private TableView<Reservation>              tableView;
    @FXML private TableColumn<Reservation, String>    hutColumn;
    @FXML private TableColumn<Reservation, Date>      dateColumn;
    @FXML private TableColumn<Reservation, String>    nameColumn;
    @FXML private TableColumn<Reservation, String>    emailColumn;
    @FXML private TableColumn<Reservation, Number>    countColumn;
    @FXML private TableColumn<Reservation, String>    commentColumn;

    /**
     * The list of reservations shown in the table.
     *
     * See #filterAction() for information about how the list is filtered.
     */
    private FilteredList<Reservation> reservations;

    /**
     * JavaFX initialization method.
     *
     * Set up cell value- and cell factories for the table columns and
     * initialize static data (e.g. filter defaults).
     *
     * This method is called by JavaFX when all FXML dependencies have been
     * injected. It should not be called by user code.
     */
    @FXML
    private void initialize() {
        dateColumn.setCellFactory(column -> new DateCell());

        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().emailProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());

        filterFromDatePicker.setValue(defaultFromDate);
        filterToDatePicker.setValue(defaultToDate);
    }

    /**
     * Finalize the initialization by providing access to the data model.
     *
     * Note: this method should be called after #initialize() has been called
     * by JavaFX.
     *
     * @param dataModel The application's data model.
     */
    public void initializeData(DataModel dataModel) {
        reservations = new FilteredList<>(dataModel.getReservationList(), p -> true);
        tableView.setItems(reservations);

        filterFromDatePicker.setOnAction(event -> filterAction());
        filterToDatePicker.setOnAction(event -> filterAction());

        filterAction();
    }

    /**
     * Update the table data by applying the current filters.
     */
    private void filterAction() {
        reservations.setPredicate(reservation -> {
            Date date = reservation.getDate();
            Date from = Util.dateFromLocalDate(filterFromDatePicker.getValue());
            Date to = Util.dateFromLocalDate(filterToDatePicker.getValue());
            // Show only reservations with dates in the inclusive range
            // [from, to]. Ignore unset filters, and always show reservations
            // without a date (which do not exist, and cannot be created, at
            // the time of writing).
            return date == null
                    || (from    == null || date.compareTo(from) >= 0)
                    && (to      == null || date.compareTo(to)   <= 0);
        });
    }
}

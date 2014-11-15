package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.OverviewRow;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class OverviewController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    @FXML private TableView<OverviewRow>                tableView;
    @FXML private TableColumn<OverviewRow, String>      hutColumn;
    @FXML private TableColumn<OverviewRow, Number>      capacityColumn;
    @FXML private TableColumn<OverviewRow, Number>      firewoodColumn;
    @FXML private TableColumn<OverviewRow, Number>      occupancyColumn;
    @FXML private TableColumn<OverviewRow, LocalDate>   nextReservationColumn;
    @FXML private TableColumn<OverviewRow, Number>      brokenCountColumn;
    @FXML private TableColumn<OverviewRow, Number>      forgottenCountColumn;

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;

        fromDatePicker.setOnAction(e -> loadImpl());
        toDatePicker.setOnAction(e -> loadImpl());
    }

    public void load() {
        loadImpl();
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    /**
     * Load the table contents based on the given filters.
     *
     * <p> Note that this function calls {@link DataModel#overviewRows(LocalDate, LocalDate) DataModel#overviewRows()}
     * which executes a potentially expensive database query.
     */
    private void loadImpl() {
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        ObservableList<OverviewRow> overviewRows;
        try {
            overviewRows = dataModel.overviewRows(fromDate, toDate);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(overviewRows);
    }

    /**
     * Initialization function called by JavaFX subsequent to injecting FXML dependencies.
     *
     * <p> Initializes how the various table columns display their data.
     */
    @FXML private void initialize() {
        hutColumn.setCellValueFactory(p -> p.getValue().getHut().nameProperty());
        capacityColumn.setCellValueFactory(p -> p.getValue().getHut().capacityProperty());
        firewoodColumn.setCellValueFactory(p -> p.getValue().getHut().firewoodProperty());
        occupancyColumn.setCellValueFactory(p -> p.getValue().occupancyProperty());
        occupancyColumn.setCellFactory(new Util.PercentageCellFactory<>());
        nextReservationColumn.setCellValueFactory(p -> p.getValue().nextReservationProperty());
        nextReservationColumn.setCellFactory(new Util.DateCellFactory<>());
        brokenCountColumn.setCellValueFactory(p -> p.getValue().brokenCountProperty());
        forgottenCountColumn.setCellValueFactory(p -> p.getValue().forgottenCountProperty());
    }
}

package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.OverviewRow;

import java.math.BigDecimal;
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

    @FXML private TableView<OverviewRow>                tableView;
    @FXML private TableColumn<OverviewRow, String>      hutColumn;
    @FXML private TableColumn<OverviewRow, Number>      capacityColumn;
    @FXML private TableColumn<OverviewRow, Number>      firewoodColumn;
    @FXML private TableColumn<OverviewRow, String>      occupancyColumn;
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
    }

    public void load() {
        ObservableList<OverviewRow> overviewRows;
        try {
            overviewRows = dataModel.overviewRows(LocalDate.now().minusDays(30), LocalDate.now());
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(overviewRows);
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    @FXML private void initialize() {
        hutColumn.setCellValueFactory(p -> p.getValue().getHut().nameProperty());
        capacityColumn.setCellValueFactory(p -> p.getValue().getHut().capacityProperty());
        firewoodColumn.setCellValueFactory(p -> p.getValue().getHut().firewoodProperty());
        occupancyColumn.setCellValueFactory(p -> {
            BigDecimal percentage = p.getValue().getOccupancy();
            if (percentage == null) { return new SimpleStringProperty(""); }
            String str = String.format("%s %%", percentage.setScale(2, BigDecimal.ROUND_HALF_EVEN));
            return new SimpleStringProperty(str);
        });
        nextReservationColumn.setCellValueFactory(p -> p.getValue().nextReservationProperty());
        brokenCountColumn.setCellValueFactory(p -> p.getValue().brokenCountProperty());
        forgottenCountColumn.setCellValueFactory(p -> p.getValue().forgottenCountProperty());
    }
}

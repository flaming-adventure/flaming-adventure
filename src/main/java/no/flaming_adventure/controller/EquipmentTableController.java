package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Equipment;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class EquipmentTableController {

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    @FXML private TableView<Equipment>              tableView;
    @FXML private TableColumn<Equipment, String>    hutColumn;
    @FXML private TableColumn<Equipment, String>    itemColumn;
    @FXML private TableColumn<Equipment, Number>    countColumn;
    @FXML private TableColumn<Equipment, LocalDate> dateColumn;

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
        ObservableList<Equipment> equipmentList;
        try {
            equipmentList = dataModel.getEquipmentList();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(equipmentList);
    }


    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    @FXML
    private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().purchaseDateProperty());
    }
}

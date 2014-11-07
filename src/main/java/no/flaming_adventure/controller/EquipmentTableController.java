package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Equipment;

public class EquipmentTableController {
    @FXML private TableView<Equipment> tableView;
    @FXML private TableColumn<Equipment, String> hutColumn;
    @FXML private TableColumn<Equipment, String> itemColumn;
    @FXML private TableColumn<Equipment, Number> countColumn;
    @FXML private TableColumn<Equipment, String> dateColumn;

    @FXML
    private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
    }

    protected void initializeData(DataModel dataModel) {
        tableView.setItems(dataModel.getEquipmentList());
    }
}

package no.flaming_adventure.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import no.flaming_adventure.model.EquipmentModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Hut;

import java.sql.SQLException;
import java.util.Map;

public class EquipmentController {
    protected final HutModel hutModel;
    protected final EquipmentModel equipmentModel;

    protected final TableView<Equipment> equipmentTableView;
    protected final TableColumn<Equipment, String> equipmentHutColumn;
    protected final TableColumn<Equipment, String> equipmentItemColumn;
    protected final TableColumn<Equipment, Integer> equipmentCountColumn;
    protected final TableColumn<Equipment, String> equipmentDateColumn;

    protected ObservableList<Equipment> equipmentObservableList = FXCollections.observableArrayList();

    protected Map<Integer, Hut> hutMap;

    public EquipmentController(HutModel hutModel, EquipmentModel equipmentModel,
                               TableView<Equipment> equipmentTableView,
                               TableColumn<Equipment, String> equipmentHutColumn,
                               TableColumn<Equipment, String> equipmentItemColumn,
                               TableColumn<Equipment, Integer> equipmentCountColumn,
                               TableColumn<Equipment, String> equipmentDateColumn) {
        this.hutModel = hutModel;
        this.equipmentModel = equipmentModel;
        this.equipmentTableView = equipmentTableView;
        this.equipmentHutColumn = equipmentHutColumn;
        this.equipmentItemColumn = equipmentItemColumn;
        this.equipmentCountColumn = equipmentCountColumn;
        this.equipmentDateColumn = equipmentDateColumn;

        try {
            hutMap = hutModel.hutMap();
            equipmentObservableList.setAll(equipmentModel.items());
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(1);
        }

        equipmentHutColumn.setCellValueFactory(
                param -> hutMap.get(param.getValue().getHutID()).nameProperty()
        );
        equipmentItemColumn.setCellValueFactory(new PropertyValueFactory<Equipment, String>("item"));
        equipmentCountColumn.setCellValueFactory(new PropertyValueFactory<Equipment, Integer>("count"));
        equipmentDateColumn.setCellValueFactory(new PropertyValueFactory<Equipment, String>("date"));

        equipmentTableView.setItems(equipmentObservableList);
    }
}

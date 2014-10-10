package no.flaming_adventure.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Callback;
import no.flaming_adventure.App;
import no.flaming_adventure.model.*;
import no.flaming_adventure.shared.*;

import java.sql.SQLException;
import java.util.Map;

public class HutStatusController {
    protected App app;

    protected HutModel hutModel;
    protected BookingModel bookingModel;
    protected ForgottenModel forgottenModel;
    protected DestroyedModel destroyedModel;
    protected EquipmentModel equipmentModel;

    protected Map<Integer, Booking> bookingMap;

    protected ObservableList<Forgotten> forgottenItems;
    protected ObservableList<Destroyed> destroyedItems;
    protected ObservableList<Equipment> equipmentItems;

    // General UI
    // ----------
    @FXML
    protected ChoiceBox<Hut> hutChoiceBox;
    @FXML
    protected Text firewoodText;

    // Forgotten item table
    // --------------------
    @FXML
    protected TableView<Forgotten> forgottenTableView;
    @FXML
    protected TableColumn<Forgotten, String> forgottenItemColumn;
    @FXML
    protected TableColumn<Forgotten, String> forgottenCommentColumn;
    @FXML
    protected TableColumn<Forgotten, String> forgottenNameColumn;
    @FXML
    protected TableColumn<Forgotten, String> forgottenEmailColumn;
    @FXML
    protected TableColumn<Forgotten, String> forgottenDateColumn;

    // Destroyed item table
    // --------------------
    @FXML
    protected TableView<Destroyed> destroyedTableView;
    @FXML
    protected TableColumn<Destroyed, String> destroyedItemColumn;

    // Equipment table
    // ---------------
    @FXML
    protected TableView<Equipment> equipmentTableView;
    @FXML
    protected TableColumn<Equipment, String> equipmentItemColumn;
    @FXML
    protected TableColumn<Equipment, Integer> equipmentCountColumn;
    @FXML
    protected TableColumn<Equipment, String> equipmentDateColumn;

    public HutStatusController(App app, HutModel hutModel, BookingModel bookingModel, ForgottenModel forgottenModel,
                               DestroyedModel destroyedModel, EquipmentModel equipmentModel) {
        this.app = app;
        this.hutModel = hutModel;
        this.bookingModel = bookingModel;
        this.forgottenModel = forgottenModel;
        this.destroyedModel = destroyedModel;
        this.equipmentModel = equipmentModel;

        this.forgottenItems = FXCollections.observableArrayList();
        this.destroyedItems = FXCollections.observableArrayList();
        this.equipmentItems = FXCollections.observableArrayList();
    }

    @FXML
    protected void initialize() {
        initializeHutChoiceBox();
        initializeForgottenTable();
        initializeDestroyedTable();
        initializeEquipmentTable();
    }

    protected void initializeHutChoiceBox() {
        try {
            hutChoiceBox.getItems().setAll(hutModel.huts());
        } catch (SQLException e) {
            app.showError("SQL Exception: " + e);
        }

        hutChoiceBox.setConverter(Hut.stringConverter);

        hutChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Hut>() {
            @Override
            public void changed(ObservableValue<? extends Hut> observable, Hut oldHut, Hut newHut) {
                updateData(newHut);
            }
        });
        // Set the hut after setting the listener so we populate the data fields instantly.
        hutChoiceBox.getSelectionModel().selectFirst();
    }

    protected void initializeForgottenTable() {
        forgottenItemColumn.setCellValueFactory(new PropertyValueFactory<Forgotten, String>("item"));
        forgottenCommentColumn.setCellValueFactory(new PropertyValueFactory<Forgotten, String>("comment"));

        forgottenNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Forgotten, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Forgotten, String> forgottenStringCellDataFeatures) {
                Integer bookingID = forgottenStringCellDataFeatures.getValue().getBookingID();
                return bookingMap.get(bookingID).nameProperty();
            }
        });
        forgottenEmailColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Forgotten, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Forgotten, String> forgottenStringCellDataFeatures) {
                Integer bookingID = forgottenStringCellDataFeatures.getValue().getBookingID();
                return bookingMap.get(bookingID).emailProperty();
            }
        });
        forgottenDateColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Forgotten, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Forgotten, String> forgottenStringCellDataFeatures) {
                Integer bookingID = forgottenStringCellDataFeatures.getValue().getBookingID();
                return bookingMap.get(bookingID).dateProperty();
            }
        });
        forgottenTableView.setItems(forgottenItems);
    }

    protected void initializeDestroyedTable() {
        destroyedItemColumn.setCellValueFactory(new PropertyValueFactory<Destroyed, String>("item"));
        destroyedTableView.setItems(destroyedItems);
    }

    protected void initializeEquipmentTable() {
        equipmentItemColumn.setCellValueFactory(new PropertyValueFactory<Equipment, String>("item"));
        equipmentCountColumn.setCellValueFactory(new PropertyValueFactory<Equipment, Integer>("count"));
        equipmentDateColumn.setCellValueFactory(new PropertyValueFactory<Equipment, String>("date"));
        equipmentTableView.setItems(equipmentItems);
    }

    protected void updateData(Hut newHut) {
        // Set firewood count.
        Integer firewood = newHut.getFirewood();
        firewoodText.setText("Ved: " + firewood.toString() + " sekker.");

        // Retrieve data from the database.
        try {
            bookingMap = bookingModel.bookingMapForHut(newHut);
            equipmentItems.setAll(equipmentModel.itemsForHut(newHut));
            destroyedItems.setAll(destroyedModel.itemsForBookings(bookingMap.values()));
            forgottenItems.setAll(forgottenModel.itemsForBookings(bookingMap.values()));
        } catch (SQLException e) {
            app.showError("SQLException: " + e);
        }
    }
}

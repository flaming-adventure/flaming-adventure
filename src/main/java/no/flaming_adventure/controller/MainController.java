package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Destroyed;
import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

/**
 * Controller for the main view.
 *
 * <ul>
 *     <li>TODO (enhancement): extract equipment table controller and view.
 *     <li>TODO (enhancement): extract destroyed table controller and view.
 *     <li>TODO (enhancement): unify date handling application-wide.
 *     <li>TODO (enhancement): only initialize controllers when the corresponding tab is first opened.
 *     <li>TODO #38 (enhancement): add data validation application-wide.
 *     <li>TODO #43 (enhancement): add error interface.
 *     <li>TODO (bug): handle empty selections.
 * </ul>
 */
public class MainController {
    private final SimpleDateFormat dateFormat;

    private final DataModel dataModel;

    @FXML private ReservationFormController     reservationFormController;
    @FXML private ReservationTableController    reservationTableController;
    @FXML private ForgottenTableController      forgottenTableController;

    @FXML protected TableView<Equipment> equipmentTableView;
    @FXML protected TableColumn<Equipment, String> equipmentHutColumn;
    @FXML protected TableColumn<Equipment, String> equipmentItemColumn;
    @FXML protected TableColumn<Equipment, Integer> equipmentCountColumn;
    @FXML protected TableColumn<Equipment, String> equipmentDateColumn;

    @FXML protected TableView<Destroyed> destroyedTableView;
    @FXML protected TableColumn<Destroyed, String> destroyedHutColumn;
    @FXML protected TableColumn<Destroyed, String> destroyedDateColumn;
    @FXML protected TableColumn<Destroyed, String> destroyedItemColumn;

    @FXML protected ComboBox<Hut> destroyedHutComboBox;
    @FXML protected DatePicker destroyedDatePicker;
    @FXML protected ChoiceBox<Reservation> destroyedReservationChoiceBox;
    @FXML protected TextField destroyedTextField;
    @FXML protected Button destroyedCommitButton;

    public MainController(SimpleDateFormat dateFormat, DataModel dataModel) {
        this.dateFormat = dateFormat;
        this.dataModel  = dataModel;
    }

    @FXML protected void initialize() {
        reservationFormController.initializeData(dataModel);
        reservationTableController.initializeData(dataModel);
        forgottenTableController.initializeData(dataModel);
        initializeEquipmentTable();
        initializeDestroyedTable();
        initializeDestroyedForm();
    }

    protected void initializeEquipmentTable() {

        equipmentHutColumn.setCellValueFactory(
                param -> param.getValue().getHut().nameProperty()
        );
        equipmentItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        equipmentCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        equipmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        equipmentTableView.setItems(dataModel.getEquipmentList());
    }

    private void initializeDestroyedTable() {
        destroyedHutColumn.setCellValueFactory(
                param -> param.getValue().getReservation().getHut().nameProperty()
        );
        destroyedDateColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                        dateFormat.format(param.getValue().getReservation().getDate())
                )
        );
        destroyedItemColumn.setCellValueFactory(param -> param.getValue().itemProperty());

        destroyedTableView.setItems(dataModel.getDestroyedList());
    }

    private static<E> void initializeComboBox(ComboBox<E> comboBox, ObservableList<E> list,
                                                 ChangeListener<E> changeListener,
                                                 StringConverter<E> converter) {
        if (converter != null) { comboBox.setConverter(converter); }
        comboBox.setItems(list);
        comboBox.getSelectionModel().selectFirst();
        comboBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }

    private void initializeDestroyedForm() {
        initializeComboBox(destroyedHutComboBox, dataModel.getHutList(),
                (observable, oldHut, newHut) -> updateDestroyedForm(newHut), Hut.stringConverter);

        destroyedDatePicker.setValue(LocalDate.now());
        destroyedDatePicker.setOnAction(event -> updateDestroyedForm());

        destroyedReservationChoiceBox.setConverter(Reservation.nameEmailConverter);

        destroyedCommitButton.setOnAction(event -> destroyedCommitAction());

        updateDestroyedForm();
    }

    private void destroyedFormDisable() {
        destroyedReservationChoiceBox.setDisable(true);
        destroyedTextField.setDisable(true);
        destroyedCommitButton.setDisable(true);
    }

    private void destroyedFormEnable() {
        destroyedReservationChoiceBox.setDisable(false);
        destroyedTextField.setDisable(false);
        destroyedCommitButton.setDisable(false);
    }

    private void updateDestroyedForm() {
        updateDestroyedForm(destroyedHutComboBox.getValue());
    }

    private void updateDestroyedForm(Hut hut) {
        destroyedReservationChoiceBox.setItems(dataModel.getReservationListForHut(hut));

        if (destroyedReservationChoiceBox.getItems().isEmpty()) {
            destroyedFormDisable();
        } else {
            if (destroyedReservationChoiceBox.getValue() == null) {
                destroyedReservationChoiceBox.getSelectionModel().selectFirst();
            }
            destroyedFormEnable();
        }
    }

    private void destroyedCommitAction() {
        destroyedHutComboBox.setDisable(true);
        destroyedDatePicker.setDisable(true);
        destroyedFormDisable();

        Reservation reservation = destroyedReservationChoiceBox.getValue();
        String item = destroyedTextField.getText();
        Boolean fixed = false;

        Destroyed destroyed = new Destroyed(reservation, -1, reservation.getID(), item, fixed);

        try {
            dataModel.insertDestroyed(destroyed);
        } catch (SQLException ignored) {
        }

        destroyedHutComboBox.setDisable(false);
        destroyedDatePicker.setDisable(false);
        destroyedFormEnable();
    }
}

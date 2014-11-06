package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.*;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for the main view.
 */
public class MainController {
    private final SimpleDateFormat dateFormat;

    private final DataModel dataModel;

    @FXML protected ReservationFormController reservationFormController;

    @FXML private DatePicker reservationFilterFromDate;
    @FXML private DatePicker reservationFilterToDate;

    @FXML protected TableView<Reservation> reservationTableView;
    @FXML protected TableColumn<Reservation, String> reservationHutColumn;
    @FXML protected TableColumn<Reservation, String> reservationDateColumn;
    @FXML protected TableColumn<Reservation, String> reservationNameColumn;
    @FXML protected TableColumn<Reservation, String> reservationEmailColumn;
    @FXML protected TableColumn<Reservation, Integer> reservationCountColumn;
    @FXML protected TableColumn<Reservation, String> reservationCommentColumn;

    @FXML protected TableView<Forgotten> forgottenTableView;
    @FXML protected TableColumn<Forgotten, String> forgottenHutColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenItemColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenCommentColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenNameColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenEmailColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenDateColumn;

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

    @FXML protected ComboBox<Hut> forgottenHutComboBox;
    @FXML protected DatePicker forgottenDatePicker;
    @FXML protected ChoiceBox<Reservation> forgottenReservationChoiceBox;
    @FXML protected TextField forgottenItemTextField;
    @FXML protected TextField forgottenCommentTextField;
    @FXML protected Button forgottenCommitButton;

    public MainController(SimpleDateFormat dateFormat, DataModel dataModel) {
        this.dateFormat = dateFormat;
        this.dataModel  = dataModel;
    }

    @FXML protected void initialize() {
        // TODO: Only initialize items when tab is first opened.
        reservationFormController.initializeData(dataModel);
        initializeReservationTable();
        initializeReservationFilter();
        initializeEquipmentTable();
        initializeForgottenTable();
        initializeForgottenForm();
        initializeDestroyedTable();
        initializeDestroyedForm();
    }

    protected void initializeReservationTable() {
        reservationHutColumn.setCellValueFactory(
                param -> param.getValue().getHut().nameProperty()
        );
        reservationDateColumn.setCellValueFactory(
                param -> new SimpleStringProperty(dateFormat.format(param.getValue().getDate()))
        );
        reservationNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        reservationEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        reservationCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        reservationCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
    }

    private void initializeReservationFilter() {
        reservationFilterFromDate.setValue(LocalDate.now());
        reservationFilterToDate.setValue(LocalDate.now().plusYears(1));

        reservationFilterFromDate.setOnAction(event -> filterReservations());
        reservationFilterToDate.setOnAction(event -> filterReservations());

        filterReservations();
    }

    private void filterReservations() {
        reservationTableView.setItems(dataModel.getReservationList().filtered(reservation -> {
                    Date date = reservation.getDate();
                    LocalDate fromLDate = reservationFilterFromDate.getValue();
                    LocalDate toLDate = reservationFilterToDate.getValue();
                    return date.after(Util.dateFromLocalDate(fromLDate))
                            && date.before(Util.dateFromLocalDate(toLDate));
                }
        ));
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

    protected void initializeForgottenTable() {
        forgottenHutColumn.setCellValueFactory(
                param -> param.getValue().getReservation().getHut().nameProperty()
        );
        forgottenItemColumn.setCellValueFactory(new PropertyValueFactory<Forgotten, String>("item"));
        forgottenCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        forgottenNameColumn.setCellValueFactory(
                param -> param.getValue().getReservation().nameProperty()
        );
        forgottenEmailColumn.setCellValueFactory(
                param -> param.getValue().getReservation().emailProperty()
        );
        forgottenDateColumn.setCellValueFactory(
                // TODO: Date handling.
                param -> new SimpleStringProperty(
                        dateFormat.format(param.getValue().getReservation().getDate()))
        );

        forgottenTableView.setItems(dataModel.getForgottenList());
    }

    private void initializeForgottenForm() {
        initializeComboBox(forgottenHutComboBox, dataModel.getHutList(),
                (observable, oldHut, newHut) -> updateForgottenForm(newHut), Hut.stringConverter);

        forgottenDatePicker.setValue(LocalDate.now());
        forgottenDatePicker.setOnAction(event -> updateForgottenForm());

        forgottenReservationChoiceBox.setConverter(Reservation.nameEmailConverter);

        forgottenCommitButton.setOnAction(event -> forgottenCommitAction());

        updateForgottenForm();
    }

    private void updateForgottenForm() {
        updateForgottenForm(forgottenHutComboBox.getValue());
    }

    private void updateForgottenForm(Hut hut) {
        forgottenReservationChoiceBox.setItems(dataModel.getReservationListForHut(hut));

        if (forgottenReservationChoiceBox.getItems().isEmpty()) {
            disableForgottenForm();
        } else {
            if (forgottenReservationChoiceBox.getValue() == null) {
                forgottenReservationChoiceBox.getSelectionModel().selectFirst();
            }
            enableForgottenForm();
        }
    }

    private void disableForgottenForm() {
        forgottenReservationChoiceBox.setDisable(true);
        forgottenItemTextField.setDisable(true);
        forgottenCommentTextField.setDisable(true);
        forgottenCommitButton.setDisable(true);
    }

    private void enableForgottenForm() {
        forgottenReservationChoiceBox.setDisable(false);
        forgottenItemTextField.setDisable(false);
        forgottenCommentTextField.setDisable(false);
        forgottenCommitButton.setDisable(false);
    }

    private void forgottenCommitAction() {
        forgottenHutComboBox.setDisable(true);
        forgottenDatePicker.setDisable(true);
        disableForgottenForm();

        // TODO: Validate data.
        Reservation reservation = forgottenReservationChoiceBox.getValue();
        String item             = forgottenItemTextField.getText();
        String comment          = forgottenCommentTextField.getText();
        Boolean delivered       = false;

        Forgotten forgotten = new Forgotten(reservation, -1, reservation.getID(), item, delivered, comment);

        try {
            dataModel.insertForgotten(forgotten);
        } catch (SQLException e) {
            // TODO: Handle exception.
        }

        forgottenHutComboBox.setDisable(false);
        forgottenDatePicker.setDisable(false);
        enableForgottenForm();
    }

    private void initializeDestroyedTable() {
        destroyedHutColumn.setCellValueFactory(
                param -> param.getValue().getReservation().getHut().nameProperty()
        );
        destroyedDateColumn.setCellValueFactory(
                // TODO: Date handling.
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
        // TODO: Handle the case where we have no huts.
        comboBox.getSelectionModel().selectFirst();
        comboBox.getSelectionModel().selectedItemProperty().addListener(changeListener);
    }

    private void initializeDestroyedForm() {
        initializeComboBox(destroyedHutComboBox, dataModel.getHutList(),
                (observable, oldHut, newHut) -> updateDestroyedForm(newHut), Hut.stringConverter);

        destroyedDatePicker.setValue(LocalDate.now());
        destroyedDatePicker.setOnAction(event -> updateDestroyedForm());

        // TODO: Extract converter.
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
        } catch (SQLException e) {
            // TODO: Handle exception.
        }

        destroyedHutComboBox.setDisable(false);
        destroyedDatePicker.setDisable(false);
        destroyedFormEnable();
    }
}

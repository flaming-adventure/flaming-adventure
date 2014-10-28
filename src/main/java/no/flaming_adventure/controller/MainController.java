package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class MainController {
    protected final SimpleDateFormat dateFormat;

    private final DataModel dataModel;

    protected ReservationFormController reservationFormController;
    @FXML protected ChoiceBox<Hut> reservationHutChoiceBox;
    @FXML protected DatePicker reservationDatePicker;
    @FXML protected Text reservationCapacityText;
    @FXML protected TextField reservationNameTextField;
    @FXML protected TextField reservationEmailTextField;
    @FXML protected ChoiceBox<Integer> reservationCountChoiceBox;
    @FXML protected TextArea reservationCommentTextArea;
    @FXML protected Button reservationCommitButton;

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

    public MainController(SimpleDateFormat dateFormat, DataModel dataModel) {
        this.dateFormat = dateFormat;
        this.dataModel  = dataModel;
    }

    @FXML protected void initialize() {
        // TODO: Only initialize items when tab is first opened.
        initializeReservationForm();
        initializeReservationTable();
        initializeEquipmentTable();
        initializeForgottenTable();
    }

    protected void initializeReservationForm() {
        reservationFormController = new ReservationFormController(dateFormat, dataModel,
                reservationHutChoiceBox, reservationDatePicker, reservationCapacityText, reservationNameTextField,
                reservationEmailTextField, reservationCountChoiceBox, reservationCommentTextArea,
                reservationCommitButton);
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

        try {
            reservationTableView.setItems(dataModel.getReservationList());
        } catch (SQLException e) {
            // TODO: Handle exception.
        }
    }

    protected void initializeEquipmentTable() {

        equipmentHutColumn.setCellValueFactory(
                param -> param.getValue().getHut().nameProperty()
        );
        equipmentItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        equipmentCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        equipmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        try {
            equipmentTableView.setItems(dataModel.getEquipmentList());
        } catch (SQLException e) {
            // TODO: Handle exception.
        }
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

        try {
            forgottenTableView.setItems(dataModel.getForgottenList());
        } catch (SQLException e) {
            // TODO: Handle exception.
        }
    }
}

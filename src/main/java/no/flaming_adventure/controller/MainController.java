package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import no.flaming_adventure.model.EquipmentModel;
import no.flaming_adventure.model.ForgottenModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.model.ReservationModel;
import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainController {
    protected final SimpleDateFormat dateFormat;

    protected final HutModel hutModel;
    protected final ReservationModel reservationModel;
    protected final ForgottenModel forgottenModel;
    protected final EquipmentModel equipmentModel;

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

    protected ObservableMap<Integer, Hut> hutMap;
    protected ObservableMap<Integer, Reservation> reservationMap;
    protected ObservableList<Hut> huts;
    protected ObservableList<Reservation> reservations;
    protected ObservableList<Equipment> equipmentList;
    protected ObservableList<Forgotten> forgottenItems;

    public MainController(SimpleDateFormat dateFormat, HutModel hutModel, ReservationModel reservationModel,
                          ForgottenModel forgottenModel, EquipmentModel equipmentModel) {
        this.dateFormat = dateFormat;
        this.hutModel = hutModel;
        this.reservationModel = reservationModel;
        this.forgottenModel = forgottenModel;
        this.equipmentModel = equipmentModel;

        try {
            hutMap = FXCollections.observableMap(hutModel.hutMap());
            reservationMap = FXCollections.observableMap(reservationModel.reservationMap());
            equipmentList = FXCollections.observableArrayList(equipmentModel.items());
            forgottenItems = FXCollections.observableArrayList(forgottenModel.forgotten());
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(1);
        }

        huts = FXCollections.observableArrayList(hutMap.values());
        reservations = FXCollections.observableArrayList(reservationMap.values());

        reservations.addListener((ListChangeListener<Reservation>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends Reservation> added = c.getAddedSubList();
                    added.stream().filter(reservation -> reservation.getID() == -1).forEach(reservation -> {
                        try {
                            reservation.IDProperty().setValue(reservationModel.insert(reservation));
                        } catch (SQLException e) {
                            System.err.println(e);
                            System.exit(1);
                        }
                        reservationMap.put(reservation.getID(), reservation);
                    });
                }
            }
        });
    }

    @FXML protected void initialize() {
        // TODO: Only initialize items when tab is first opened.
        initializeReservationForm();
        initializeReservationTable();
        initializeEquipmentTable();
        initializeForgottenTable();
    }

    protected void initializeReservationForm() {
        reservationFormController = new ReservationFormController(dateFormat, huts, reservations,
                reservationHutChoiceBox, reservationDatePicker, reservationCapacityText, reservationNameTextField,
                reservationEmailTextField, reservationCountChoiceBox, reservationCommentTextArea,
                reservationCommitButton);
    }

    protected void initializeReservationTable() {
        reservationHutColumn.setCellValueFactory(
                param -> hutMap.get(param.getValue().getHutID()).nameProperty()
        );
        reservationDateColumn.setCellValueFactory(
                param -> new SimpleStringProperty(dateFormat.format(param.getValue().getDate()))
        );
        reservationNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        reservationEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        reservationCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        reservationCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        reservationTableView.setItems(reservations);
    }

    protected void initializeEquipmentTable() {

        equipmentHutColumn.setCellValueFactory(
                param -> hutMap.get(param.getValue().getHutID()).nameProperty()
        );
        equipmentItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        equipmentCountColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        equipmentDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        equipmentTableView.setItems(equipmentList);
    }

    protected void initializeForgottenTable() {
        forgottenHutColumn.setCellValueFactory(
                param -> hutMap.get(reservationMap.get(param.getValue().getID()).getID()).nameProperty()
        );
        forgottenItemColumn.setCellValueFactory(new PropertyValueFactory<Forgotten, String>("item"));
        forgottenCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        forgottenNameColumn.setCellValueFactory(
                param -> reservationMap.get(param.getValue().getID()).nameProperty()
        );
        forgottenEmailColumn.setCellValueFactory(
                param -> reservationMap.get(param.getValue().getID()).emailProperty()
        );
        forgottenDateColumn.setCellValueFactory(
                param -> new SimpleStringProperty(
                        dateFormat.format(reservationMap.get(param.getValue().getID()).getDate()))
        );

        forgottenTableView.setItems(forgottenItems);
    }
}

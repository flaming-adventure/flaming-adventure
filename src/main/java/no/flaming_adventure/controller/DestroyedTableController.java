package no.flaming_adventure.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Destroyed;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

public class DestroyedTableController {
    static private final LocalDate defaultDate = LocalDate.now();

    @FXML private TableView<Destroyed> tableView;
    @FXML private TableColumn<Destroyed, String> hutColumn;
    @FXML private TableColumn<Destroyed, Date> dateColumn;
    @FXML private TableColumn<Destroyed, String> itemColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<Reservation> reservationChoiceBox;
    @FXML private TextField textField;
    @FXML private Button commitButton;

    private DataModel dataModel;

    @FXML
    private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getReservation().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().getReservation().dateProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());

        hutComboBox.setConverter(Hut.stringConverter);
        datePicker.setValue(defaultDate);
        reservationChoiceBox.setConverter(Reservation.nameEmailConverter);
    }

    public void initializeData(DataModel dataModel) {
        this.dataModel = dataModel;

        tableView.setItems(dataModel.getDestroyedList());

        hutComboBox.setItems(dataModel.getHutList());
        hutComboBox.getSelectionModel().selectFirst();
        hutComboBox.setOnAction(this::formUpdateAction);

        datePicker.setOnAction(this::formUpdateAction);

        commitButton.setOnAction(this::formCommitAction);

        formUpdateAction(null);
    }

    private void formUpdateAction(ActionEvent event) {
        Hut hut = hutComboBox.getValue();

        reservationChoiceBox.setItems(dataModel.getReservationListForHut(hut));

        if (reservationChoiceBox.getItems().isEmpty()) {
            formDisable(false);
        } else {
            reservationChoiceBox.getSelectionModel().selectFirst();
            formEnable(false);
        }
    }

    private void formCommitAction(ActionEvent event) {
        formDisable(true);

        Reservation reservation = reservationChoiceBox.getValue();
        String item = textField.getText();
        Boolean fixed = false;

        Destroyed destroyed = new Destroyed(reservation, -1, reservation.getID(), item, fixed);

        try {
            dataModel.insertDestroyed(destroyed);
        } catch (SQLException ignored) {

        }

        formEnable(true);
    }

    private void formDisable(Boolean all) {
        if (all) {
            hutComboBox.setDisable(true);
            datePicker.setDisable(true);
        }
        reservationChoiceBox.setDisable(true);
        textField.setDisable(true);
        commitButton.setDisable(true);
    }

    private void formEnable(Boolean all) {
        if (all) {
            hutComboBox.setDisable(false);
            datePicker.setDisable(false);
        }
        reservationChoiceBox.setDisable(false);
        textField.setDisable(false);
        commitButton.setDisable(false);
    }
}

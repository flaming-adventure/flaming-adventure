package no.flaming_adventure.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.model.BrokenItem;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.model.Reservation;

import java.time.LocalDate;

public class BrokenItemTableController {
    static private final LocalDate defaultDate = LocalDate.now();

    @FXML private TableView<BrokenItem>                 tableView;
    @FXML private TableColumn<BrokenItem, String>       hutColumn;
    @FXML private TableColumn<BrokenItem, LocalDate>    dateColumn;
    @FXML private TableColumn<BrokenItem, String>       itemColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<Reservation> reservationChoiceBox;
    @FXML private TextField textField;
    @FXML private Button commitButton;

    private DataModel dataModel;

    @FXML
    private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());

        datePicker.setValue(defaultDate);
    }

    public void initializeData(DataModel dataModel) {
        this.dataModel = dataModel;

        tableView.setItems(dataModel.getBrokenItemList());

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

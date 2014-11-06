package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for reservation form.
 */
public class ReservationFormController {
    private static final String capacityTextFormat = "%d av totalt %d plasser ledige.";

    private DataModel dataModel;

    @FXML private ComboBox<Hut>         hutComboBox;
    @FXML private DatePicker            datePicker;
    @FXML private Text                  capacityText;
    @FXML private TextField             nameTextField;
    @FXML private TextField             emailTextField;
    @FXML private ChoiceBox<Integer>    countChoiceBox;
    @FXML private TextArea              commentTextArea;
    @FXML private Button                commitButton;

    @FXML
    private void initialize() {
        datePicker.setValue(LocalDate.now());
        datePicker.setDayCellFactory(new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);

                        if (date.isBefore(LocalDate.now())) {
                            this.setDisable(true);
                        }
                    }
                };
            }
        });
    }

    public void initializeData(DataModel dataModel) {
        this.dataModel = dataModel;

        ObservableList<Hut> huts = dataModel.getHutList();

        hutComboBox.setItems(huts);
        hutComboBox.setConverter(Hut.stringConverter);
        // FIXME: it's possible for the list of huts to be empty.
        hutComboBox.getSelectionModel().selectFirst();

        hutComboBox.setOnAction(event -> updateAction());
        datePicker.setOnAction(event -> updateAction());
        commitButton.setOnAction(event -> {
            commitAction(dataModel);
            updateAction();
        });

        updateAction();
    }

    private void updateAction() {
        Hut hut = hutComboBox.getValue();
        Date date = Util.dateFromLocalDate(datePicker.getValue());

        if (hut == null || date == null) { return; }

        Integer occupancy =  0;
        for (Reservation reservation : dataModel.getReservationListForHut(hut)) {
            if (reservation.getDate().equals(date)) { occupancy += reservation.getCount(); }
        }

        Integer totalCapacity = hut.getCapacity();
        Integer actualCapacity = totalCapacity - occupancy;

        capacityText.setText(String.format(capacityTextFormat, actualCapacity, totalCapacity));

        ObservableList<Integer> countChoiceBoxItems = countChoiceBox.getItems();
        countChoiceBoxItems.clear();
        for (int i = 1; i <= actualCapacity; i++) { countChoiceBoxItems.add(i); }

        // TODO: retain previous choice of count.

        if (actualCapacity < 1) { disableInput(); }
        else { enableInput(); }
    }

    /**
     * Disable the use of the name text field, the email text field, the choice box for number of people and the commit
     * button.
     */
    private void disableInput() {
        nameTextField.setDisable(true);
        emailTextField.setDisable(true);
        countChoiceBox.setDisable(true);
        commentTextArea.setDisable(true);
        commitButton.setDisable(true);
    }

    /**
     * Enable the use of the name text field, the email text field, the choice box for number of people and the commit
     * button.
     */
    private void enableInput() {
        nameTextField.setDisable(false);
        emailTextField.setDisable(false);
        countChoiceBox.setDisable(false);
        commentTextArea.setDisable(false);
        commitButton.setDisable(false);
    }

    /**
     * Validate the current form data and commit it to the database as a reservation if it is valid.
     */
    private void commitAction(DataModel dataModel) {
        // TODO: Disable input during commit.
        Hut hut = hutComboBox.getValue();
        Date date = Util.dateFromLocalDate(datePicker.getValue());
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        Integer count = countChoiceBox.getValue();
        String comment = commentTextArea.getText();

        try {
            dataModel.insertReservation(new Reservation(hut, -1, hut.getID(), date, name, email, count, comment));
        } catch (SQLException e) {
            // TODO: Handle exception.
        }
    }
}

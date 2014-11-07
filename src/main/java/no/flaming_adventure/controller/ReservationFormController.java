package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for reservation form view.
 * <ul>
 *     <li>TODO (enhancement): provide visual feedback on field limits.
 *     <li>TODO (enhancement): provide visual feedback on required fields.
 *     <li>TODO (enhancement): provide visual feedback on successful database insert.
 * </ul>
 */
public class ReservationFormController {
    /**
     * Date cell for the datePicker.
     * <p>
     * Responsible for disabling selection of dates earlier than today's date.
     * <ul>
     *     <li>TODO (low priority): mark dates with zero capacity.
     * </ul>
     */
    static private final class DateCell extends javafx.scene.control.DateCell {
        @Override
        public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);

            if (date == null) { return; }

            if (date.isBefore(LocalDate.now())) {
                this.setDisable(true);
            }
        }
    }

    static private final LocalDate defaultDate = LocalDate.now();

    /**
     * Format string for capacity text.
     * <ul>
     *     <li>TODO (low priority): extract to localization file.
     * </ul>
     */
    static private final String capacityTextFormat = "%d av totalt %d plasser ledige.";

    private DataModel dataModel;

    @FXML private ComboBox<Hut>         hutComboBox;
    @FXML private DatePicker            datePicker;
    @FXML private Text                  capacityText;
    @FXML private TextField             nameTextField;
    @FXML private TextField             emailTextField;
    @FXML private ChoiceBox<Integer>    countChoiceBox;
    @FXML private TextArea              commentTextArea;
    @FXML private Button                commitButton;

    /**
     * JavaFX initialization method.
     * <p>
     * Responsible for initializing the date picker. All other initialization is handled in
     * {@link #initializeData(DataModel) initializeData(DataModel)}.
     * <p>
     * This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by user
     * code.
     */
    @FXML
    private void initialize() {
        datePicker.setValue(defaultDate);
        datePicker.setDayCellFactory(param -> new DateCell());
    }

    /**
     * Finalize the initialization by providing access to the data model.
     * <p>
     * Note: this method should be called after {@link #initialize() initialize()} has been called by JavaFX.
     * <ul>
     *     <li>TODO (bug): handle initialization with an empty list of huts.
     * </ul>
     *
     * @param dataModel The application's data model.
     */
    public void initializeData(DataModel dataModel) {
        this.dataModel = dataModel;

        ObservableList<Hut> huts = dataModel.getHutList();

        hutComboBox.setItems(huts);
        hutComboBox.setConverter(Hut.stringConverter);
        hutComboBox.getSelectionModel().selectFirst();

        hutComboBox.setOnAction(event -> updateAction());
        datePicker.setOnAction(event -> updateAction());
        commitButton.setOnAction(event -> {
            commitAction(dataModel);
            updateAction();
        });

        updateAction();
    }

    /**
     * Update the form based on the currently selected hut and date.
     * <ul>
     *     <li>TODO (bug): handle the lack of a selected hut or date.
     *     <li>TODO (enhancement): do occupancy calculation in the data model.
     *     <li>TODO (enhancement, low priority): improve count choice update code.
     *     <li>TODO (enhancement): make a default selection for count choice.
     *     <li>TODO (enhancement): keep previously selected count choice if possible.
     * </ul>
     */
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

        if (actualCapacity < 1) { disableInput(); }
        else { enableInput(); }
    }

    /**
     * Disable the use of all inputs except for hut and date selection.
     * <ul>
     *     <li>TODO: (enhancement): rename to something more descriptive.
     * </ul>
     */
    private void disableInput() {
        nameTextField.setDisable(true);
        emailTextField.setDisable(true);
        countChoiceBox.setDisable(true);
        commentTextArea.setDisable(true);
        commitButton.setDisable(true);
    }

    /**
     * Enable the use of all inputs except for hut and date selection.
     * <ul>
     *     <li>TODO: (enhancement): rename to something more descriptive.
     * </ul>
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
     * <ul>
     *     <li>TODO (enhancement): disable input during commit.
     *     <li>TODO (bug): validate data in the data model and throw an additional exception for that case.
     *     <li>TODO (bug): handle error conditions.
     *     <li>TODO (enhancement): display error conditions to the user instead of letting them pass silently.
     * </ul>
     */
    private void commitAction(DataModel dataModel) {
        Hut hut = hutComboBox.getValue();
        Date date = Util.dateFromLocalDate(datePicker.getValue());
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        Integer count = countChoiceBox.getValue();
        String comment = commentTextArea.getText();

        try {
            dataModel.insertReservation(new Reservation(hut, -1, hut.getID(), date, name, email, count, comment));
        } catch (SQLException ignored) {
        }
    }
}

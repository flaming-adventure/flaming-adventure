package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.model.Reservation;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Controller for reservation form view.
 * <ul>
 *     <li>TODO #45 (enhancement): provide visual feedback on field limits.
 *     <li>TODO #45 (enhancement): provide visual feedback on required fields.
 *     <li>TODO #45 (enhancement): provide visual feedback on successful database insert.
 * </ul>
 */
public class ReservationFormController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    /**
     * Date cell for the datePicker.
     * <p>
     * Responsible for disabling selection of dates earlier than today's date.
     * <ul>
     *     <li>TODO #45 (enhancement, low priority): mark dates with zero capacity.
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
     *     <li>TODO #42 (low priority): extract to localization file.
     * </ul>
     */
    static private final String capacityTextFormat = "%d av totalt %d plasser ledige.";

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    /* Injected dependencies (see #inject()). */
    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    /* JavaFX injected dependencies. */
    @FXML private ComboBox<Hut>         hutComboBox;
    @FXML private DatePicker            datePicker;
    @FXML private Text                  capacityText;
    @FXML private TextField             nameTextField;
    @FXML private TextField             emailTextField;
    @FXML private ChoiceBox<Integer>    countChoiceBox;
    @FXML private TextArea              commentTextArea;
    @FXML private Button                commitButton;

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;
    }

    /**
     * Finalize the initialization by providing access to the data model.
     *
     * <p> Note: this method should be called after {@link #initialize() initialize()} has been called by JavaFX.
     *
     * <ul>
     *     <li>TODO (bug): handle initialization with an empty list of huts.
     * </ul>
     */
    public void load() {
        ObservableList<Hut> huts;
        try {
            huts = dataModel.getHutList();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        hutComboBox.getEditor().setDisable(true);
        hutComboBox.setItems(huts);
        hutComboBox.getSelectionModel().selectFirst();

        hutComboBox.setOnAction(event -> updateAction());
        datePicker.setOnAction(event -> updateAction());
        commitButton.setOnAction(event -> {
            commitAction();
            updateAction();
        });

        updateAction();
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    /**
     * JavaFX initialization method.
     *
     * <p> Responsible for initializing the date picker. All other initialization is handled in
     * {@link #load() load()}.
     *
     * <p> This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by
     * user code.
     */
    @FXML
    private void initialize() {
        datePicker.setValue(defaultDate);
        datePicker.setDayCellFactory(param -> new DateCell());
    }

    /**
     * Update the form based on the currently selected hut and date.
     *
     * <ul>
     *     <li>TODO (bug): handle the lack of a selected hut or date.
     *     <li>TODO #38 (enhancement): do occupancy calculation in the data model.
     *     <li>TODO (enhancement, low priority): improve count choice update code.
     *     <li>TODO #45 (enhancement): make a default selection for count choice.
     *     <li>TODO #45 (enhancement): keep previously selected count choice if possible.
     * </ul>
     */
    private void updateAction() {
        Hut hut = hutComboBox.getValue();
        LocalDate date = datePicker.getValue();

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

        if (actualCapacity < 1) { disableInput(false); }
        else { enableInput(false); }
    }

    /**
     * Disable the use of all inputs except for hut and date selection.
     */
    private void disableInput(Boolean disableAll) {
        if (disableAll) {
            hutComboBox.setDisable(true);
            datePicker.setDisable(true);
        }
        nameTextField.setDisable(true);
        emailTextField.setDisable(true);
        countChoiceBox.setDisable(true);
        commentTextArea.setDisable(true);
        commitButton.setDisable(true);
    }

    /**
     * Enable the use of all inputs except for hut and date selection.
     */
    private void enableInput(Boolean disableAll) {
        if (disableAll) {
            hutComboBox.setDisable(false);
            datePicker.setDisable(false);
        }
        nameTextField.setDisable(false);
        emailTextField.setDisable(false);
        countChoiceBox.setDisable(false);
        commentTextArea.setDisable(false);
        commitButton.setDisable(false);
    }

    /**
     * Validate the current form data and commit it to the database as a reservation if it is valid.
     *
     * <ul>
     *     <li>TODO #45 (enhancement): disable input during commit.
     *     <li>TODO #38 (bug): validate data in the data model and throw an additional exception for that case.
     *     <li>TODO #43 (bug): handle error conditions.
     *     <li>TODO #43 (enhancement): display error conditions to the user instead of letting them pass silently.
     * </ul>
     */
    private void commitAction() {
        disableInput(true);

        Hut hut         = hutComboBox.getValue();
        LocalDate date  = datePicker.getValue();
        String name     = nameTextField.getText();
        String email    = emailTextField.getText();
        Integer count   = countChoiceBox.getValue();
        String comment  = commentTextArea.getText();

        Reservation reservation = new Reservation(null, hut, date, name, email, count, comment);

        try {
            dataModel.insertReservation(reservation);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        enableInput(true);
    }
}

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
 *
 * <p> The reservation form has the singular purpose of adding records to the reservations table in the database.
 */
public class ReservationFormController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    public static final int NAME_MAX_LENGTH = 64;
    public static final int EMAIL_MAX_LENGTH = 64;

    /**
     * Date cell for the date picker.
     *
     * <p> Disables dates before today.
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

    static private final LocalDate TODAY = LocalDate.now();

    /**
     * Format string for capacity text.
     */
    static private final String CAPACITY_TEXT_FORMAT = "%d av totalt %d plasser ledige.";

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    /* Injected dependencies (see #inject()). */
    private DataModel           dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    /* JavaFX injected dependencies. */
    @FXML private ComboBox<Hut>      hutComboBox;
    @FXML private DatePicker         datePicker;
    @FXML private Text               capacityText;
    @FXML private TextField          nameTextField;
    @FXML private TextField          emailTextField;
    @FXML private ChoiceBox<Integer> countChoiceBox;
    @FXML private TextArea           commentTextArea;
    @FXML private Button             commitButton;

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
     */
    public void load() {
        ObservableList<Hut> huts;
        try {
            huts = dataModel.getHuts();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        hutComboBox.setItems(huts);
        hutComboBox.getSelectionModel().selectFirst();
        datePicker.setValue(TODAY);

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
        datePicker.setDayCellFactory(param -> new DateCell());
        hutComboBox.getEditor().setDisable(true);

        hutComboBox.setOnAction(ignored -> updateAction());
        datePicker.setOnAction(ignored -> updateAction());
        commitButton.setOnAction(ignored -> commitAction());

        nameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > NAME_MAX_LENGTH) {
                nameTextField.setText(oldValue);
            }
        });
        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > EMAIL_MAX_LENGTH) {
                emailTextField.setText(oldValue);
            }
        });
    }

    /**
     * Update the form based on the currently selected hut and date.
     */
    private void updateAction() {
        Hut hut = hutComboBox.getValue();
        LocalDate date = datePicker.getValue();

        if (hut == null || date == null) { return; }

        Integer occupancy;
        try {
            occupancy = dataModel.occupancy(hut, date);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        Integer totalCapacity = hut.getCapacity();
        Integer actualCapacity = totalCapacity - occupancy;

        capacityText.setText(String.format(CAPACITY_TEXT_FORMAT, actualCapacity, totalCapacity));

        updateCount(actualCapacity);
    }

    private void updateCount(int max) {
        SingleSelectionModel<Integer> selectionModel = countChoiceBox.getSelectionModel();
        ObservableList<Integer> items = countChoiceBox.getItems();

        Integer oldValue = selectionModel.getSelectedItem();
        items.clear();
        if (max < 1) {
            disableInput(false);
        } else {
            for (int i = 1; i <= max; i++) { items.add(i); }
            if (items.contains(oldValue)) {
                selectionModel.select(oldValue);
            } else {
                selectionModel.selectFirst();
            }
            enableInput(false);
        }
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
     */
    private void commitAction() {
        disableInput(true);

        Hut hut         = hutComboBox.getValue();
        LocalDate date  = datePicker.getValue();
        String name     = nameTextField.getText();
        String email    = emailTextField.getText();
        Integer count   = countChoiceBox.getValue();
        String comment  = commentTextArea.getText();

        try {
            Reservation reservation = new Reservation(-1, hut, date, name, email, count, comment);
            dataModel.insertReservation(reservation);
        } catch (Exception e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        updateAction();

        enableInput(true);
    }
}

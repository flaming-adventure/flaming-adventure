package no.flaming_adventure.controller;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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

    private final DataModel dataModel;

    private final ChoiceBox<Hut> hutChoiceBox;
    private final DatePicker datePicker;
    private final Text capacityText;
    private final TextField nameTextField;
    private final TextField emailTextField;
    private final ChoiceBox<Integer> countChoiceBox;
    private final TextArea commentTextArea;
    private final Button commitButton;

    private final ObservableList<Hut> huts;

    public ReservationFormController(DataModel dataModel,
                                     ChoiceBox<Hut> hutChoiceBox, DatePicker datePicker, Text capacityText,
                                     TextField nameTextField, TextField emailTextField, ChoiceBox<Integer> countChoiceBox,
                                     TextArea commentTextArea, Button commitButton) {
        this.dataModel = dataModel;
        this.huts = dataModel.getHutList();
        this.hutChoiceBox = hutChoiceBox;
        this.datePicker = datePicker;
        this.capacityText = capacityText;
        this.nameTextField = nameTextField;
        this.emailTextField = emailTextField;
        this.countChoiceBox = countChoiceBox;
        this.commentTextArea = commentTextArea;
        this.commitButton = commitButton;

        initializeDatePicker();
        initializeHutChoiceBox();
        updateCapacity(hutChoiceBox.getValue());

        commitButton.setOnAction(event -> commitAction());
    }

    /**
     * Initialize the hut choice box.
     * <p/>
     * The @i huts list is set as the item property for the choice box. If at least one hut exists it is selected.
     * If no huts exist all input is disabled until at least one hut is added. The first hut added is also selected.
     * <p/>
     * Additionally an event handler that updates the displayed capacity and the count selection box upon choosing a new
     * hut is added to the choice box.
     */
    private void initializeHutChoiceBox() {
        hutChoiceBox.setItems(huts);
        hutChoiceBox.setConverter(Hut.stringConverter);

        // If we have no huts we should disable the form entirely until at least one hut is added.
        if (huts.isEmpty()) {
            hutChoiceBox.setDisable(true);
            datePicker.setDisable(true);
            disableInput();

            // One-time listener selecting a hut and enabling input when at least one hut is added.
            huts.addListener(new ListChangeListener<Hut>() {
                @Override
                public void onChanged(Change<? extends Hut> c) {
                    if (c.wasAdded()) {
                        hutChoiceBox.getSelectionModel().selectFirst();
                        hutChoiceBox.setDisable(false);
                        datePicker.setDisable(false);
                        enableInput();
                        huts.removeListener(this);
                    }
                }
            });
        // If we have at least one hut we'll simply select the first one.
        } else {
            hutChoiceBox.getSelectionModel().selectFirst();
        }

        // Whenever we select a hut we should update the displayed capacity and the count selection box.
        hutChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldHut, newHut) -> updateCapacity(newHut));
    }

    /**
     * Initialize the date picker with a DayCellFactory that disables selection of all dates before today and set the
     * selected date to today.
     * <p/>
     * In addition an event handler that calls updateCapacity with the current hut every time a new selection is made is
     * added.
     */
    private void initializeDatePicker() {
        // DayCellFactory that disables all dates before today.
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);

                        if (date.isBefore(LocalDate.now())) {
                            setDisable(true);
                        }
                    }
                };
            }
        };
        datePicker.setDayCellFactory(dayCellFactory);


        // XXX: The date picker's value should be set before its event handler so that we don't inadvertently call
        // updateCapacity with an empty hut list.
        datePicker.setValue(LocalDate.now());

        datePicker.setOnAction(event -> updateCapacity(hutChoiceBox.getValue()));
    }

    /**
     * Updates the max and actual capacity numbers in the interface to those of the given hut on the current date.
     * <p/>
     * The person count choice box is also updated, with the maximum selectable value being the actual capacity of the
     * hut.
     * <p/>
     * If the actual capacity is zero various interface elements are disabled until a hut or date where the capacity is
     * non-zero is selected.
     *
     * @param hut Hut from which capacity is to be retrieved.
     */
    private void updateCapacity(Hut hut) {
        // XXX: Currently redundant, but the mistake is easy to make.
        if (hut == null) { return; }

        Date date = Util.dateFromLocalDate(datePicker.getValue());


        Integer occupancy = 0;
        for (Reservation reservation : dataModel.getReservationListForHut(hut)) {
            if (reservation.getDate().equals(date)) {
                occupancy += reservation.getCount();
            }
        }

        Integer totalCapacity = hut.getCapacity();
        Integer actualCapacity = totalCapacity - occupancy;

        capacityText.setText(String.format(capacityTextFormat, actualCapacity, totalCapacity));

        ObservableList<Integer> countChoiceBoxItems = countChoiceBox.getItems();
        Integer oldCapacityChoice = countChoiceBox.getValue();

        countChoiceBoxItems.clear();
        for (int i = 1; i <= actualCapacity; i++) {
            countChoiceBoxItems.add(i);
        }

        // XXX: oldCapacityChoice is null on the first update.
        if (oldCapacityChoice != null && oldCapacityChoice <= actualCapacity) {
            countChoiceBox.setValue(oldCapacityChoice);
        } else {
            countChoiceBox.getSelectionModel().selectFirst();
        }

        if (actualCapacity < 1) {
            disableInput();
        } else {
            enableInput();
        }
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
    private void commitAction() {
        // TODO: Disable input during commit.
        Hut hut = hutChoiceBox.getValue();
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

        updateCapacity(hutChoiceBox.getValue());
    }
}

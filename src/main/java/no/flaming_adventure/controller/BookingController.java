package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Hut;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Controller for booking window.
 */
public class BookingController {
    protected final SimpleDateFormat dateFormat;
    protected final ChoiceBox<Hut> hutChoiceBox;
    protected final DatePicker datePicker;
    protected final Text capacityText;
    protected final TextField nameTextField;
    protected final TextField emailTextField;
    protected final ChoiceBox<Integer> countChoiceBox;
    protected final TextArea commentTextArea;
    protected final Button commitButton;

    protected final ObservableList<Hut> huts;
    protected final ObservableList<Booking> bookings;

    public BookingController(SimpleDateFormat dateFormat, ObservableList<Hut> huts, ObservableList<Booking> bookings,
                             ChoiceBox<Hut> hutChoiceBox, DatePicker datePicker, Text capacityText,
                             TextField nameTextField, TextField emailTextField, ChoiceBox<Integer> countChoiceBox,
                             TextArea commentTextArea, Button commitButton) {
        this.dateFormat = dateFormat;
        this.huts = huts;
        this.bookings = bookings;
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
     * Query the database for the names of all huts and add them to hutChoiceBox. The first hut in the list is chosen as
     * the current hut.
     * <p/>
     * In addition an event handler that calls updateCapacity with the new hut every time a new selection is made is
     * added.
     */
    protected void initializeHutChoiceBox() {
        hutChoiceBox.setItems(huts);

        hutChoiceBox.setConverter(Hut.stringConverter);

        // XXX: The controller is not necessarily fully initialized at this
        //  point, so we should avoid having updateCapacity called.
        hutChoiceBox.getSelectionModel().selectFirst();

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
    protected void initializeDatePicker() {
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


        // XXX: The controller is not necessarily fully initialized at this
        //  point, so we should avoid having updateCapacity called.
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
    protected void updateCapacity(Hut hut) {
        // Convert the LocalDate from the datePicker to a java.util.Date
        // object.
        LocalDate ldt = datePicker.getValue();
        Instant instant = ldt.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);

        Integer totalCapacity = hut.getCapacity();

        Integer occupancy = 0;
        Integer hutID = hut.getID();
        for (Booking booking : bookings) {
            if (booking.getHutID() == hutID && booking.getDate().equals(date)) {
                occupancy += booking.getCount();
            }
        }

        Integer actualCapacity = totalCapacity - occupancy;

        capacityText.setText(String.format("%d av totalt %d plasser ledige.", actualCapacity, totalCapacity));

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
    protected void disableInput() {
        nameTextField.setDisable(true);
        emailTextField.setDisable(true);
        countChoiceBox.setDisable(true);
        commitButton.setDisable(true);
    }

    /**
     * Enable the use of the name text field, the email text field, the choice box for number of people and the commit
     * button.
     */
    protected void enableInput() {
        nameTextField.setDisable(false);
        emailTextField.setDisable(false);
        countChoiceBox.setDisable(false);
        commitButton.setDisable(false);
    }

    /**
     * Validate the current form data and commit it to the database as a booking if it is valid.
     */
    protected void commitAction() {
        Hut hut = hutChoiceBox.getValue();
        LocalDate localDate = datePicker.getValue();
        String name = nameTextField.getText();
        String email = emailTextField.getText();
        Integer count = countChoiceBox.getValue();
        String comment = commentTextArea.getText();

        Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();

        bookings.add(new Booking(-1, hut.getID(), Date.from(instant), name, email, count, comment));

        datePicker.setValue(localDate.plusDays(1));
    }
}

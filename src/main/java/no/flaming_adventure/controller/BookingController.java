package no.flaming_adventure.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import no.flaming_adventure.App;
import no.flaming_adventure.model.BookingModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Hut;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Controller for booking window.
 */
public class BookingController {
    protected final App app;
    protected final BookingModel bookingModel;
    protected final HutModel hutModel;
    @FXML
    protected ChoiceBox<Hut> hutChoiceBox;
    @FXML
    protected DatePicker datePicker;
    @FXML
    protected Text capacityText;
    @FXML
    protected TextField nameTextField;
    @FXML
    protected TextField emailTextField;
    @FXML
    protected ChoiceBox<Integer> countChoiceBox;
    @FXML
    protected Button commitButton;
    @FXML
    protected Button abortButton;

    public BookingController(App app, HutModel hutModel, BookingModel bookingModel) {
        this.app = app;
        this.hutModel = hutModel;
        this.bookingModel = bookingModel;
    }

    /**
     * Initialize the controller.
     * <p/>
     * This function is automatically called when the contents of the FXML document has been completely loaded.
     */
    @FXML
    protected void initialize() {
        initializeDatePicker();
        initializeHutChoiceBox();
        updateCapacity(hutChoiceBox.getValue());

        abortButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                abortAction();
            }
        });

        commitButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                commitAction();
            }
        });
    }

    /**
     * Query the database for the names of all huts and add them to hutChoiceBox. The first hut in the list is chosen as
     * the current hut.
     * <p/>
     * In addition an event handler that calls updateCapacity with the new hut every time a new selection is made is
     * added.
     */
    protected void initializeHutChoiceBox() {
        try {
            hutChoiceBox.getItems().setAll(hutModel.huts());
        } catch (SQLException e) {
            app.showError("SQL Exception: " + e);
        }

        hutChoiceBox.setConverter(Hut.stringConverter);

        // XXX: The controller is not necessarily fully initialized at this
        //  point, so we should avoid having updateCapacity called.
        hutChoiceBox.getSelectionModel().selectFirst();

        hutChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Hut>() {
            @Override
            public void changed(ObservableValue<? extends Hut> observable, Hut oldHut, Hut newHut) {
                updateCapacity(newHut);
            }
        });
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

        datePicker.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateCapacity(hutChoiceBox.getValue());
            }
        });
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
        LocalDateTime ldt = datePicker.getValue().atStartOfDay();
        Instant instant = ldt.atZone(ZoneId.systemDefault()).toInstant();
        Date date = Date.from(instant);

        Integer totalCapacity = hut.getCapacity();

        Integer occupancy = 0;
        try {
            occupancy = bookingModel.occupancy(hut.getID(), date);
        } catch (SQLException e) {
            app.showError("SQL Exception: " + e);
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
    }

    /**
     * Return to the app window.
     */
    protected void abortAction() {
        reset();
        app.showMenu();
    }

    /**
     * Reset the window to its original state.
     */
    protected void reset() {
        hutChoiceBox.getSelectionModel().selectFirst();
        datePicker.setValue(LocalDate.now());
        nameTextField.setText("");
        emailTextField.setText("");
        countChoiceBox.setValue(1);
    }
}

package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.sql.SQLException;
import java.text.DateFormat;
import java.time.LocalDate;
import java.util.Date;

/**
 * Controller for the forgotten item tab, responsible for both table and form.
 *
 * <ul>
 *     <li>TODO (high priority): add filter functionality.
 *     <li>TODO (low priority): add inline editing of records.
 *     <li>TODO (low priority): add deletion or deactivation of records.
 *     <li>TODO (high priority): show whether items have been returned.
 * </ul>
 */
public class ForgottenTableController {
    /**
     * Date cell for the dateColumn.
     * <p>
     * Responsible for date formatting.
     * <ul>
     *     <li>TODO: use custom, application wide, date formatting.
     * </ul>
     */
    static private final class DateCell extends TableCell<Forgotten, Date> {
        @Override
        protected void updateItem(Date date, boolean empty) {
            super.updateItem(date, empty);

            if (date == null) {
                setText(null);
            } else {
                setText(DateFormat.getDateInstance().format(date));
            }
        }
    }

    /**
     * Default date for the form's date picker.
     */
    static private final LocalDate defaultDate = LocalDate.now();

    @FXML private TableView<Forgotten> tableView;
    @FXML private TableColumn<Forgotten, String> hutColumn;
    @FXML private TableColumn<Forgotten, String> itemColumn;
    @FXML private TableColumn<Forgotten, String> commentColumn;
    @FXML private TableColumn<Forgotten, String> nameColumn;
    @FXML private TableColumn<Forgotten, String> emailColumn;
    @FXML private TableColumn<Forgotten, Date>   dateColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ChoiceBox<Reservation> reservationChoiceBox;
    @FXML private TextField itemTextField;
    @FXML private TextField commentTextField;
    @FXML private Button commitButton;

    private DataModel dataModel;

    /**
     * JavaFX initialization method.
     * <p>
     * Initialize static data and set defaults not depending on business data.
     * <p>
     * This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by user
     * code.
     */
    @FXML
    private void initialize() {
        dateColumn.setCellFactory(column -> new DateCell());

        hutColumn.setCellValueFactory(param -> param.getValue().getReservation().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
        nameColumn.setCellValueFactory(param -> param.getValue().getReservation().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().getReservation().emailProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().getReservation().dateProperty());

        hutComboBox.setConverter(Hut.stringConverter);
        datePicker.setValue(defaultDate);
        reservationChoiceBox.setConverter(Reservation.nameEmailConverter);
    }

    /**
     * Finalize the initialization by providing access to the data model.
     * <p>
     * Note: this method should be called after {@link #initialize() initialize()} has been called by JavaFX.
     *
     * @param dataModel the application's data model.
     */
    public void initializeData(DataModel dataModel) {
        this.dataModel = dataModel;

        tableView.setItems(dataModel.getForgottenList());

        hutComboBox.setItems(dataModel.getHutList());
        hutComboBox.getSelectionModel().selectFirst();
        hutComboBox.setOnAction(event -> updateAction());

        datePicker.setOnAction(event -> updateAction());
        commitButton.setOnAction(event -> {
            commitAction();
            updateAction();
        });

        updateAction();
    }

    /**
     * Update action for the form.
     */
    private void updateAction() {
        Hut hut = hutComboBox.getValue();

        reservationChoiceBox.setItems(dataModel.getReservationListForHut(hut));
        if (reservationChoiceBox.getItems().isEmpty()) {
            disableForm();
        } else {
            reservationChoiceBox.getSelectionModel().selectFirst();
            enableForm();
        }
    }

    /**
     * Disable all parts of the form with the exception of the hut combo box and the date picker.
     */
    private void disableForm() {
        reservationChoiceBox.setDisable(true);
        itemTextField.setDisable(true);
        commentTextField.setDisable(true);
        commitButton.setDisable(true);
    }

    /**
     * Enable all parts of the form with the exception of the hut combo box and the date picker.
     */
    private void enableForm() {
        reservationChoiceBox.setDisable(false);
        itemTextField.setDisable(false);
        commentTextField.setDisable(false);
        commitButton.setDisable(false);
    }

    /**
     * Attempt to add a new forgotten item from the form to the database.
     */
    private void commitAction() {
        hutComboBox.setDisable(true);
        datePicker.setDisable(true);
        disableForm();

        Reservation reservation = reservationChoiceBox.getValue();
        String item             = itemTextField.getText();
        String comment          = commentTextField.getText();
        Boolean delivered       = false;

        Forgotten forgotten = new Forgotten(reservation, -1, reservation.getID(), item, delivered, comment);

        try {
            dataModel.insertForgotten(forgotten);
        } catch (SQLException ignored) {
        }

        hutComboBox.setDisable(false);
        datePicker.setDisable(false);
        enableForm();
    }
}

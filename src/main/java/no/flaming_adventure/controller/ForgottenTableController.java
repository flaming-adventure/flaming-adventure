package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.ForgottenItem;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.model.Reservation;

import java.time.LocalDate;

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
     * Default date for the form's date picker.
     */
    static private final LocalDate defaultDate = LocalDate.now();

    @FXML private TableView<ForgottenItem>              tableView;
    @FXML private TableColumn<ForgottenItem, String>    hutColumn;
    @FXML private TableColumn<ForgottenItem, String>    itemColumn;
    @FXML private TableColumn<ForgottenItem, String>    commentColumn;
    @FXML private TableColumn<ForgottenItem, String>    nameColumn;
    @FXML private TableColumn<ForgottenItem, String>    emailColumn;
    @FXML private TableColumn<ForgottenItem, LocalDate> dateColumn;

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
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().contactProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());

        datePicker.setValue(defaultDate);
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

        tableView.setItems(dataModel.getForgottenItemList());

        hutComboBox.setItems(dataModel.getHutListDeprecated());
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
    }
}

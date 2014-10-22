package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import no.flaming_adventure.model.BookingModel;
import no.flaming_adventure.model.ForgottenModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;

public class MainController {
    protected final HutModel hutModel;
    protected final BookingModel bookingModel;
    protected final ForgottenModel forgottenModel;

    protected BookingController bookingController;
    @FXML protected ChoiceBox<Hut> bookingHutChoiceBox;
    @FXML protected DatePicker bookingDatePicker;
    @FXML protected Text bookingCapacityText;
    @FXML protected TextField bookingNameTextField;
    @FXML protected TextField bookingEmailTextField;
    @FXML protected ChoiceBox<Integer> bookingCountChoiceBox;
    @FXML protected Button bookingCommitButton;

    protected ForgottenController forgottenController;
    @FXML protected TableView<Forgotten> forgottenTableView;
    @FXML protected TableColumn<Forgotten, String> forgottenHutColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenItemColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenCommentColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenNameColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenEmailColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenDateColumn;

    public MainController(HutModel hutModel, BookingModel bookingModel, ForgottenModel forgottenModel) {
        this.hutModel = hutModel;
        this.bookingModel = bookingModel;
        this.forgottenModel = forgottenModel;
    }

    @FXML protected void initialize() {
        bookingController = new BookingController(bookingModel, hutModel, bookingHutChoiceBox, bookingDatePicker,
                bookingCapacityText, bookingNameTextField, bookingEmailTextField, bookingCountChoiceBox,
                bookingCommitButton);
        forgottenController = new ForgottenController(hutModel, forgottenModel, bookingModel, forgottenTableView,
                forgottenHutColumn, forgottenItemColumn, forgottenCommentColumn, forgottenNameColumn,
                forgottenEmailColumn, forgottenDateColumn);
    }
}

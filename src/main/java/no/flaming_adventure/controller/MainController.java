package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.flaming_adventure.model.BookingModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Hut;

public class MainController {
    protected final HutModel hutModel;
    protected final BookingModel bookingModel;

    protected BookingController bookingController;
    @FXML protected ChoiceBox<Hut> bookingHutChoiceBox;
    @FXML protected DatePicker bookingDatePicker;
    @FXML protected Text bookingCapacityText;
    @FXML protected TextField bookingNameTextField;
    @FXML protected TextField bookingEmailTextField;
    @FXML protected ChoiceBox<Integer> bookingCountChoiceBox;
    @FXML protected Button bookingCommitButton;

    public MainController(HutModel hutModel, BookingModel bookingModel) {
        this.hutModel = hutModel;
        this.bookingModel = bookingModel;
    }

    @FXML protected void initialize() {
        if (bookingDatePicker == null) { System.out.println("Was null!"); }
        bookingController = new BookingController(bookingModel, hutModel, bookingHutChoiceBox, bookingDatePicker,
                bookingCapacityText, bookingNameTextField, bookingEmailTextField, bookingCountChoiceBox,
                bookingCommitButton);
    }
}

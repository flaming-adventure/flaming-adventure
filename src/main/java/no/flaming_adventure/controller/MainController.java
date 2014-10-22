package no.flaming_adventure.controller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import no.flaming_adventure.model.BookingModel;
import no.flaming_adventure.model.EquipmentModel;
import no.flaming_adventure.model.ForgottenModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainController {
    protected final SimpleDateFormat dateFormat;

    protected final HutModel hutModel;
    protected final BookingModel bookingModel;
    protected final ForgottenModel forgottenModel;
    protected final EquipmentModel equipmentModel;

    protected BookingController bookingController;
    @FXML protected ChoiceBox<Hut> bookingHutChoiceBox;
    @FXML protected DatePicker bookingDatePicker;
    @FXML protected Text bookingCapacityText;
    @FXML protected TextField bookingNameTextField;
    @FXML protected TextField bookingEmailTextField;
    @FXML protected ChoiceBox<Integer> bookingCountChoiceBox;
    @FXML protected TextArea bookingCommentTextArea;
    @FXML protected Button bookingCommitButton;

    protected BookingTableController bookingTableController;
    @FXML protected TableView<Booking> bookingTableView;
    @FXML protected TableColumn<Booking, String> bookingHutColumn;
    @FXML protected TableColumn<Booking, String> bookingDateColumn;
    @FXML protected TableColumn<Booking, String> bookingNameColumn;
    @FXML protected TableColumn<Booking, String> bookingEmailColumn;
    @FXML protected TableColumn<Booking, Integer> bookingCountColumn;
    @FXML protected TableColumn<Booking, String> bookingCommentColumn;

    protected ForgottenController forgottenController;
    @FXML protected TableView<Forgotten> forgottenTableView;
    @FXML protected TableColumn<Forgotten, String> forgottenHutColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenItemColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenCommentColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenNameColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenEmailColumn;
    @FXML protected TableColumn<Forgotten, String> forgottenDateColumn;

    protected EquipmentController equipmentController;
    @FXML protected TableView<Equipment> equipmentTableView;
    @FXML protected TableColumn<Equipment, String> equipmentHutColumn;
    @FXML protected TableColumn<Equipment, String> equipmentItemColumn;
    @FXML protected TableColumn<Equipment, Integer> equipmentCountColumn;
    @FXML protected TableColumn<Equipment, String> equipmentDateColumn;

    protected ObservableMap<Integer, Hut> hutMap;
    protected ObservableMap<Integer, Booking> bookingMap;
    protected ObservableList<Hut> huts;
    protected ObservableList<Booking> bookings;
    protected ObservableList<Forgotten> forgottenItems;

    public MainController(SimpleDateFormat dateFormat, HutModel hutModel, BookingModel bookingModel,
                          ForgottenModel forgottenModel, EquipmentModel equipmentModel) {
        this.dateFormat = dateFormat;
        this.hutModel = hutModel;
        this.bookingModel = bookingModel;
        this.forgottenModel = forgottenModel;
        this.equipmentModel = equipmentModel;

        try {
            hutMap = FXCollections.observableMap(hutModel.hutMap());
            bookingMap = FXCollections.observableMap(bookingModel.bookingMap());
            forgottenItems = FXCollections.observableArrayList(forgottenModel.forgotten());
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(1);
        }

        huts = FXCollections.observableArrayList(hutMap.values());
        bookings = FXCollections.observableArrayList(bookingMap.values());

        bookings.addListener((ListChangeListener<Booking>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends Booking> added = c.getAddedSubList();
                    added.stream().filter(booking -> booking.getID() == -1).forEach(booking -> {
                        try {
                            booking.IDProperty().setValue(bookingModel.insert(booking));
                        } catch (SQLException e) {
                            System.err.println(e);
                            System.exit(1);
                        }
                        bookingMap.put(booking.getID(), booking);
                    });
                }
            }
        });
    }

    @FXML protected void initialize() {
        bookingController = new BookingController(dateFormat, huts, bookings, bookingHutChoiceBox, bookingDatePicker,
                bookingCapacityText, bookingNameTextField, bookingEmailTextField, bookingCountChoiceBox,
                bookingCommentTextArea, bookingCommitButton);

        bookingTableController = new BookingTableController(dateFormat, hutMap, bookings, bookingTableView,
                bookingHutColumn, bookingDateColumn, bookingNameColumn, bookingEmailColumn, bookingCountColumn,
                bookingCommentColumn);

        forgottenController = new ForgottenController(dateFormat, hutMap, bookingMap, forgottenItems,
                forgottenTableView, forgottenHutColumn, forgottenItemColumn, forgottenCommentColumn,
                forgottenNameColumn, forgottenEmailColumn, forgottenDateColumn);

        equipmentController = new EquipmentController(hutModel, equipmentModel, equipmentTableView, equipmentHutColumn,
                equipmentItemColumn, equipmentCountColumn, equipmentDateColumn);
    }
}

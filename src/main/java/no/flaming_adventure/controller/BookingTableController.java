package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Hut;

import java.text.SimpleDateFormat;

public class BookingTableController {
    protected final SimpleDateFormat dateFormat;

    protected final TableView<Booking> tableView;
    protected final TableColumn<Booking, String> hutColumn;
    protected final TableColumn<Booking, String> dateColumn;
    protected final TableColumn<Booking, String> nameColumn;
    protected final TableColumn<Booking, String> emailColumn;
    protected final TableColumn<Booking, Integer> countColumn;
    protected final TableColumn<Booking, String> commentColumn;

    protected final ObservableList<Booking> bookings;
    protected final ObservableMap<Integer, Hut> hutMap;

    public BookingTableController(SimpleDateFormat dateFormat, ObservableMap<Integer, Hut> hutMap,
                                  ObservableList<Booking> bookings, TableView<Booking> tableView,
                                  TableColumn<Booking, String> hutColumn, TableColumn<Booking, String> dateColumn,
                                  TableColumn<Booking, String> nameColumn, TableColumn<Booking, String> emailColumn,
                                  TableColumn<Booking, Integer> countColumn,
                                  TableColumn<Booking, String> commentColumn) {
        this.dateFormat = dateFormat;
        this.hutMap = hutMap;
        this.bookings = bookings;
        this.tableView = tableView;
        this.hutColumn = hutColumn;
        this.dateColumn = dateColumn;
        this.nameColumn = nameColumn;
        this.emailColumn = emailColumn;
        this.countColumn = countColumn;
        this.commentColumn = commentColumn;

        hutColumn.setCellValueFactory(
                param -> hutMap.get(param.getValue().getHutID()).nameProperty()
        );
        dateColumn.setCellValueFactory(
                param -> new SimpleStringProperty(dateFormat.format(param.getValue().getDate()))
        );
        nameColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("email"));
        countColumn.setCellValueFactory(new PropertyValueFactory<Booking, Integer>("count"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("comment"));

        tableView.setItems(bookings);
    }
}

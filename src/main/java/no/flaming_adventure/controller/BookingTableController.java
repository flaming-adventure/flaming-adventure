package no.flaming_adventure.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import no.flaming_adventure.model.BookingModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Hut;

import java.sql.SQLException;
import java.util.Map;

public class BookingTableController {
    protected final HutModel hutModel;
    protected final BookingModel bookingModel;

    protected final TableView<Booking> tableView;
    protected final TableColumn<Booking, String> hutColumn;
    protected final TableColumn<Booking, String> dateColumn;
    protected final TableColumn<Booking, String> nameColumn;
    protected final TableColumn<Booking, String> emailColumn;
    protected final TableColumn<Booking, Integer> countColumn;
    protected final TableColumn<Booking, String> commentColumn;

    protected ObservableList<Booking> bookingObservableList = FXCollections.observableArrayList();

    protected Map<Integer, Hut> hutMap;

    public BookingTableController(HutModel hutModel, BookingModel bookingModel, TableView<Booking> tableView,
                                  TableColumn<Booking, String> hutColumn, TableColumn<Booking, String> dateColumn,
                                  TableColumn<Booking, String> nameColumn, TableColumn<Booking, String> emailColumn,
                                  TableColumn<Booking, Integer> countColumn,
                                  TableColumn<Booking, String> commentColumn) {
        this.hutModel = hutModel;
        this.bookingModel = bookingModel;
        this.tableView = tableView;
        this.hutColumn = hutColumn;
        this.dateColumn = dateColumn;
        this.nameColumn = nameColumn;
        this.emailColumn = emailColumn;
        this.countColumn = countColumn;
        this.commentColumn = commentColumn;

        try {
            hutMap = hutModel.hutMap();
            bookingObservableList.setAll(bookingModel.bookingMap().values());
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(1);
        }

        hutColumn.setCellValueFactory(
                param -> hutMap.get(param.getValue().getHutID()).nameProperty()
        );
        dateColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("date"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("email"));
        countColumn.setCellValueFactory(new PropertyValueFactory<Booking, Integer>("count"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<Booking, String>("comment"));

        tableView.setItems(bookingObservableList);
    }
}

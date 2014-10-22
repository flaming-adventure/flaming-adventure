package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import no.flaming_adventure.shared.Hut;
import no.flaming_adventure.shared.Reservation;

import java.text.SimpleDateFormat;

public class ReservationTableController {
    protected final SimpleDateFormat dateFormat;

    protected final TableView<Reservation> tableView;
    protected final TableColumn<Reservation, String> hutColumn;
    protected final TableColumn<Reservation, String> dateColumn;
    protected final TableColumn<Reservation, String> nameColumn;
    protected final TableColumn<Reservation, String> emailColumn;
    protected final TableColumn<Reservation, Integer> countColumn;
    protected final TableColumn<Reservation, String> commentColumn;

    protected final ObservableList<Reservation> reservations;
    protected final ObservableMap<Integer, Hut> hutMap;

    public ReservationTableController(SimpleDateFormat dateFormat, ObservableMap<Integer, Hut> hutMap,
                                      ObservableList<Reservation> reservations, TableView<Reservation> tableView,
                                      TableColumn<Reservation, String> hutColumn, TableColumn<Reservation, String> dateColumn,
                                      TableColumn<Reservation, String> nameColumn, TableColumn<Reservation, String> emailColumn,
                                      TableColumn<Reservation, Integer> countColumn,
                                      TableColumn<Reservation, String> commentColumn) {
        this.dateFormat = dateFormat;
        this.hutMap = hutMap;
        this.reservations = reservations;
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
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        countColumn.setCellValueFactory(new PropertyValueFactory<>("count"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        tableView.setItems(reservations);
    }
}

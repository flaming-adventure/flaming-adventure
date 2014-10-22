package no.flaming_adventure.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;

import java.text.SimpleDateFormat;

public class ForgottenController {
    protected final SimpleDateFormat dateFormat;
    protected final TableView<Forgotten> forgottenTableView;
    protected final TableColumn<Forgotten, String> forgottenHutColumn;
    protected final TableColumn<Forgotten, String> forgottenItemColumn;
    protected final TableColumn<Forgotten, String> forgottenCommentColumn;
    protected final TableColumn<Forgotten, String> forgottenNameColumn;
    protected final TableColumn<Forgotten, String> forgottenEmailColumn;
    protected final TableColumn<Forgotten, String> forgottenDateColumn;

    protected final ObservableList<Forgotten> forgottenItems;

    protected final ObservableMap<Integer, Hut> hutMap;
    protected final ObservableMap<Integer, Booking> bookingMap;

    public ForgottenController(SimpleDateFormat dateFormat, ObservableMap<Integer, Hut> hutMap,
                               ObservableMap<Integer, Booking> bookingMap,
                               ObservableList<Forgotten> forgottenItems, TableView<Forgotten> forgottenTableView,
                               TableColumn<Forgotten, String> forgottenHutColumn,
                               TableColumn<Forgotten, String> forgottenItemColumn,
                               TableColumn<Forgotten, String> forgottenCommentColumn,
                               TableColumn<Forgotten, String> forgottenNameColumn,
                               TableColumn<Forgotten, String> forgottenEmailColumn,
                               TableColumn<Forgotten, String> forgottenDateColumn) {
        this.dateFormat = dateFormat;
        this.hutMap = hutMap;
        this.bookingMap = bookingMap;
        this.forgottenItems = forgottenItems;
        this.forgottenTableView = forgottenTableView;
        this.forgottenHutColumn = forgottenHutColumn;
        this.forgottenItemColumn = forgottenItemColumn;
        this.forgottenCommentColumn = forgottenCommentColumn;
        this.forgottenNameColumn = forgottenNameColumn;
        this.forgottenEmailColumn = forgottenEmailColumn;
        this.forgottenDateColumn = forgottenDateColumn;

        forgottenHutColumn.setCellValueFactory(
                param -> hutMap.get(bookingMap.get(param.getValue().getID()).getID()).nameProperty()
        );
        forgottenItemColumn.setCellValueFactory(new PropertyValueFactory<>("item"));
        forgottenCommentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        forgottenNameColumn.setCellValueFactory(
                param -> bookingMap.get(param.getValue().getID()).nameProperty()
        );
        forgottenEmailColumn.setCellValueFactory(
                param -> bookingMap.get(param.getValue().getID()).emailProperty()
        );
        forgottenDateColumn.setCellValueFactory(
                param -> new SimpleStringProperty(dateFormat.format(bookingMap.get(param.getValue().getID()).getDate()))
        );

        forgottenTableView.setItems(forgottenItems);
    }
}

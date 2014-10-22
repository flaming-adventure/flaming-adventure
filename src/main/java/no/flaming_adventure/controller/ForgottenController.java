package no.flaming_adventure.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import no.flaming_adventure.model.BookingModel;
import no.flaming_adventure.model.ForgottenModel;
import no.flaming_adventure.model.HutModel;
import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Forgotten;
import no.flaming_adventure.shared.Hut;

import java.sql.SQLException;
import java.util.Map;

public class ForgottenController {
    protected final HutModel hutModel;
    protected final BookingModel bookingModel;
    protected final ForgottenModel forgottenModel;

    protected final TableView<Forgotten> forgottenTableView;
    protected final TableColumn<Forgotten, String> forgottenHutColumn;
    protected final TableColumn<Forgotten, String> forgottenItemColumn;
    protected final TableColumn<Forgotten, String> forgottenCommentColumn;
    protected final TableColumn<Forgotten, String> forgottenNameColumn;
    protected final TableColumn<Forgotten, String> forgottenEmailColumn;
    protected final TableColumn<Forgotten, String> forgottenDateColumn;

    protected ObservableList<Forgotten> forgottenObservableList = FXCollections.observableArrayList();

    protected Map<Integer, Hut> hutMap;
    protected Map<Integer, Booking> bookingMap;

    public ForgottenController(HutModel hutModel, ForgottenModel forgottenModel, BookingModel bookingModel,
                               TableView<Forgotten> forgottenTableView,
                               TableColumn<Forgotten, String> forgottenHutColumn,
                               TableColumn<Forgotten, String> forgottenItemColumn,
                               TableColumn<Forgotten, String> forgottenCommentColumn,
                               TableColumn<Forgotten, String> forgottenNameColumn,
                               TableColumn<Forgotten, String> forgottenEmailColumn,
                               TableColumn<Forgotten, String> forgottenDateColumn) {
        this.hutModel = hutModel;
        this.forgottenModel = forgottenModel;
        this.bookingModel = bookingModel;
        this.forgottenTableView = forgottenTableView;
        this.forgottenHutColumn = forgottenHutColumn;
        this.forgottenItemColumn = forgottenItemColumn;
        this.forgottenCommentColumn = forgottenCommentColumn;
        this.forgottenNameColumn = forgottenNameColumn;
        this.forgottenEmailColumn = forgottenEmailColumn;
        this.forgottenDateColumn = forgottenDateColumn;

        try {
            hutMap = hutModel.hutMap();
            bookingMap = bookingModel.bookingMap();
            forgottenObservableList.setAll(forgottenModel.forgotten());
        } catch (SQLException e) {
            System.err.println(e);
            System.exit(1);
        }

        forgottenHutColumn.setCellValueFactory(
                param -> hutMap.get(bookingMap.get(param.getValue().getID()).getID()).nameProperty()
        );
        forgottenItemColumn.setCellValueFactory(new PropertyValueFactory<Forgotten, String>("item"));
        forgottenCommentColumn.setCellValueFactory(new PropertyValueFactory<Forgotten, String>("comment"));
        forgottenNameColumn.setCellValueFactory(
                param -> bookingMap.get(param.getValue().getID()).nameProperty()
        );
        forgottenEmailColumn.setCellValueFactory(
                param -> bookingMap.get(param.getValue().getID()).emailProperty()
        );
        forgottenDateColumn.setCellValueFactory(
                param -> bookingMap.get(param.getValue().getID()).dateProperty()
        );

        forgottenTableView.setItems(forgottenObservableList);
    }
}

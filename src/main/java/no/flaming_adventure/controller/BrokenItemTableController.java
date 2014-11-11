package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.model.BrokenItem;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Hut;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class BrokenItemTableController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    private static final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    @FXML private TableView<BrokenItem>                 tableView;
    @FXML private TableColumn<BrokenItem, String>       hutColumn;
    @FXML private TableColumn<BrokenItem, LocalDate>    dateColumn;
    @FXML private TableColumn<BrokenItem, String>       itemColumn;
    @FXML private TableColumn<BrokenItem, String>       commentColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker    datePicker;
    @FXML private TextField     textField;
    @FXML private Button        commitButton;
    @FXML private TextField     commentTextField;

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;
    }

    public void load() {
        ObservableList<Hut>         huts;
        ObservableList<BrokenItem>  brokenItems;
        try {
            huts = dataModel.getHuts();
            brokenItems = dataModel.getBrokenItems();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(brokenItems);

        hutComboBox.setItems(huts);
        hutComboBox.getSelectionModel().selectFirst();

        datePicker.setValue(YESTERDAY);
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    @FXML
    private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
    }
}

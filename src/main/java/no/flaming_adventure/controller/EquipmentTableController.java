package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import no.flaming_adventure.App;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Equipment;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.util.DateCellFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class EquipmentTableController extends TableControllerBase<Equipment> {

    /***************************************************************************
     *                                                                         *
     * Static variables and methods                                            *
     *                                                                         *
     **************************************************************************/

    private static final Hut HUT_FILTER_NO_SELECTION = new Hut(-1, "<ALLE>", 0, 0);

    private static final LocalDate TODAY = LocalDate.now();

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    @FXML private ComboBox<Hut> hutFilter;
    @FXML private DatePicker    fromDateFilter;
    @FXML private DatePicker    toDateFilter;

    @FXML private TableColumn<Equipment, String>    hutColumn;
    @FXML private TableColumn<Equipment, String>    itemColumn;
    @FXML private TableColumn<Equipment, Number>    countColumn;
    @FXML private TableColumn<Equipment, LocalDate> dateColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker    datePicker;
    @FXML private TextField     itemTextField;
    @FXML private TextField     countTextField;
    @FXML private Button        commitButton;

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        super.inject(dataModel, unhandledExceptionHook);

        hutFilter.setOnAction(event -> loadPage(0));
        fromDateFilter.setOnAction(event -> loadPage(0));
        toDateFilter.setOnAction(event -> loadPage(0));
        commitButton.setOnAction(event -> commitButtonHook());
    }

    @Override public void load() {
        ObservableList<Hut> huts;
        try {
            huts = dataModel.getHuts();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        hutFilter.getItems().clear();
        hutFilter.getItems().add(HUT_FILTER_NO_SELECTION);
        hutFilter.getItems().addAll(huts);

        // Don't load the data just because we changed the filter.
        dataLock = true;
        hutFilter.setValue(HUT_FILTER_NO_SELECTION);
        dataLock = false;

        super.load();

        hutComboBox.setItems(huts);
        hutComboBox.getSelectionModel().selectFirst();

        datePicker.setValue(TODAY);
    }


    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    @Override @FXML protected void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        countColumn.setCellValueFactory(param -> param.getValue().countProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().purchaseDateProperty());
        dateColumn.setCellFactory(new DateCellFactory<>(App.DATE_TIME_FORMATTER));

        hutColumn.setId("huts.name");
        itemColumn.setId("equipment.name");
        countColumn.setId("equipment.count");
        dateColumn.setId("equipment.purchase_date");

        super.initialize();

        EventHandler<KeyEvent> enterHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitButton.fire();
            }
        };

        hutComboBox.setOnKeyReleased(enterHandler);
        datePicker.setOnKeyReleased(enterHandler);
        itemTextField.setOnKeyReleased(enterHandler);
        countTextField.setOnKeyReleased(enterHandler);
    }

    @Override protected void loadPageImpl(Integer pageIndex) {
        Hut hut = hutFilter.getValue();
        if (hut == HUT_FILTER_NO_SELECTION) { hut = null; }
        LocalDate fromDate  = fromDateFilter.getValue();
        LocalDate toDate    = toDateFilter.getValue();

        Integer itemCount;
        ObservableList<Equipment> items;
        try {
            itemCount = dataModel.equipmentCount(hut, fromDate, toDate);
            items = dataModel.equipmentPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE, hut, fromDate, toDate,
                                            ordering);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        this.items.setAll(items);

        setPageCount(itemCount);

        pagination.setCurrentPageIndex(pageIndex);
    }

    private void commitButtonHook() {
        Hut hut         = hutComboBox.getValue();
        LocalDate date  = datePicker.getValue();
        String item     = itemTextField.getText();
        Integer count   = Integer.parseUnsignedInt(countTextField.getText());

        try {
            Equipment equipment = new Equipment(-1, hut, item, date, count);
            dataModel.insertEquipment(equipment);
        } catch (NullPointerException|SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        itemTextField.clear();
        countTextField.clear();

        loadPage(pagination.getCurrentPageIndex());
    }
}

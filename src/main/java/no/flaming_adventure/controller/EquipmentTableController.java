package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Equipment;
import no.flaming_adventure.model.Hut;

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
        dateColumn.setCellFactory(new Util.DateCellFactory<>());

        hutColumn.setId("huts.name");
        itemColumn.setId("equipment.name");
        countColumn.setId("equipment.count");
        dateColumn.setId("equipment.purchase_date");

        super.initialize();
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
        System.out.println(itemCount);
        setPageCount(itemCount);

        pagination.setCurrentPageIndex(pageIndex);
    }
}

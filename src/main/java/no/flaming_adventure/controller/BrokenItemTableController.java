package no.flaming_adventure.controller;

import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import no.flaming_adventure.App;
import no.flaming_adventure.model.BrokenItem;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.Hut;
import no.flaming_adventure.util.DateCellFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

public class BrokenItemTableController extends TableControllerBase<BrokenItem> {

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
    @FXML private CheckBox      fixedFilter;

    @FXML private TableColumn<BrokenItem, String>    hutColumn;
    @FXML private TableColumn<BrokenItem, LocalDate> dateColumn;
    @FXML private TableColumn<BrokenItem, String>    itemColumn;
    @FXML private TableColumn<BrokenItem, String>    commentColumn;
    @FXML private TableColumn<BrokenItem, Boolean>   fixedColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker    datePicker;
    @FXML private TextField     itemTextField;
    @FXML private TextField     commentTextField;
    @FXML private Button        commitButton;

    /***************************************************************************
     *                                                                         *
     * Constructors                                                            *
     *                                                                         *
     **************************************************************************/

    public BrokenItemTableController() {
        super(param -> new Observable[]{param.fixedProperty()});
    }

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    @Override public void inject(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        super.inject(dataModel, unhandledExceptionHook);

        hutFilter.setOnAction(e -> loadPage(0));
        fromDateFilter.setOnAction(e -> loadPage(0));
        toDateFilter.setOnAction(e -> loadPage(0));
        fixedFilter.setOnAction(e -> loadPage(0));
        commitButton.setOnAction(ignored -> commitButtonHook());
    }

    @Override public void load() {
        ObservableList<Hut>         huts;
        try {
            huts = dataModel.getHuts();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        hutFilter.getItems().clear();
        hutFilter.getItems().add(HUT_FILTER_NO_SELECTION);
        hutFilter.getItems().addAll(huts);

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

    protected void updateItem(BrokenItem item) {
        try {
            dataModel.updateBrokenItemFixed(item);
        } catch (Throwable e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }
    }

    @Override @FXML protected void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        dateColumn.setCellFactory(new DateCellFactory<>(App.DATE_TIME_FORMATTER));
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
        fixedColumn.setCellValueFactory(param -> param.getValue().fixedProperty());
        fixedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(fixedColumn));

        hutColumn.setId("huts.name");
        dateColumn.setId("broken_items.date");
        itemColumn.setId("broken_items.item");
        commentColumn.setId("broken_items.comment");
        fixedColumn.setId("broken_items.fixed");

        super.initialize();

        EventHandler<KeyEvent> enterHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitButton.fire();
            }
        };

        hutComboBox.setOnKeyReleased(enterHandler);
        datePicker.setOnKeyReleased(enterHandler);
        itemTextField.setOnKeyReleased(enterHandler);
        commentTextField.setOnKeyReleased(enterHandler);
    }

    @Override protected void loadPageImpl(Integer pageIndex) {
        Hut hut = hutFilter.getValue();
        if (hut == HUT_FILTER_NO_SELECTION) { hut = null; }
        LocalDate fromDate  = fromDateFilter.getValue();
        LocalDate toDate    = toDateFilter.getValue();
        Boolean showFixed   = fixedFilter.isSelected();

        String filterBy = null;
        if (! showFixed) { filterBy = "broken_items.fixed = FALSE"; }

        Integer brokenItemCount;
        ObservableList<BrokenItem> brokenItems;
        try {
            brokenItemCount = dataModel.brokenItemCount(hut, fromDate, toDate, filterBy);
            brokenItems = dataModel.brokenItemPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE,
                                                   hut, fromDate, toDate, ordering, filterBy);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        items.setAll(brokenItems);

        setPageCount(brokenItemCount);

        pagination.setCurrentPageIndex(pageIndex);
    }

    private void commitButtonHook() {
        Hut hut         = hutComboBox.getValue();
        LocalDate date  = datePicker.getValue();
        String item     = itemTextField.getText();
        String comment  = commentTextField.getText();

        try {
            BrokenItem brokenItem = new BrokenItem(-1, hut, item, date, false, comment);
            dataModel.insertBrokenItem(brokenItem);
        } catch (NullPointerException|SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        itemTextField.clear();
        commentTextField.clear();

        loadPage(pagination.getCurrentPageIndex());
    }
}

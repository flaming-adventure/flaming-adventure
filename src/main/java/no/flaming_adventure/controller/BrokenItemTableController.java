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

    private static final Integer ITEMS_PER_PAGE = 50;

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

    @FXML private Pagination pagination;

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
        try {
            huts = dataModel.getHuts();
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        pagination.currentPageIndexProperty()
                .addListener((observable, oldValue, newValue) -> loadPage(newValue.intValue()));
        loadPage(0);

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

    private void loadPage(Integer pageIndex) {
        Integer brokenItemCount;
        ObservableList<BrokenItem> brokenItems;
        try {
            brokenItemCount = dataModel.brokenItemCount();
            brokenItems = dataModel.brokenItemPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(brokenItems);

        if (brokenItemCount == 0) {
            pagination.setPageCount(1);
        } else {
            pagination.setPageCount((brokenItemCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        }

        pagination.setCurrentPageIndex(pageIndex);
    }
}

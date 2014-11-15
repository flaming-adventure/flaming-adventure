package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import no.flaming_adventure.Util;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.ForgottenItem;
import no.flaming_adventure.model.Hut;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Controller for the forgotten item tab, responsible for both table and form.
 */
public class ForgottenTableController extends TableControllerBase<ForgottenItem> {

    /***************************************************************************
     *                                                                         *
     * Static variables and methods                                            *
     *                                                                         *
     **************************************************************************/

    static private final Hut HUT_FILTER_NO_SELECTION = new Hut(-1, "<ALLE>", 0, 0);

    static private final LocalDate TODAY = LocalDate.now();

    /***************************************************************************
     *                                                                         *
     * Instance Variables                                                      *
     *                                                                         *
     **************************************************************************/

    @FXML private ComboBox<Hut> hutFilter;
    @FXML private DatePicker    fromDateFilter;
    @FXML private DatePicker    toDateFilter;

    @FXML private TableColumn<ForgottenItem, String>    hutColumn;
    @FXML private TableColumn<ForgottenItem, String>    itemColumn;
    @FXML private TableColumn<ForgottenItem, String>    commentColumn;
    @FXML private TableColumn<ForgottenItem, String>    nameColumn;
    @FXML private TableColumn<ForgottenItem, String>    emailColumn;
    @FXML private TableColumn<ForgottenItem, LocalDate> dateColumn;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker    datePicker;
    @FXML private TextField     itemTextField;
    @FXML private TextField     nameTextField;
    @FXML private TextField     contactTextField;
    @FXML private TextField     commentTextField;
    @FXML private Button        commitButton;

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
        commitButton.setOnAction(ignored -> commitButtonHook());
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

        dataLock = true;
        hutFilter.setValue(HUT_FILTER_NO_SELECTION);
        dataLock = false;

        super.load();

        hutComboBox.setItems(huts);
        hutComboBox.getSelectionModel().selectFirst();

        datePicker.setValue(TODAY);
    }

    /************************************************************************
     *
     * Private API
     *
     ************************************************************************/

    /**
     * JavaFX initialization method.
     *
     * <p> Initialize static data and set defaults not depending on business data.
     *
     * <p> This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by
     * user code.
     */
    @Override @FXML protected void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().contactProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());
        dateColumn.setCellFactory(new Util.DateCellFactory<>());

        hutColumn.setId("huts.name");
        itemColumn.setId("forgotten_items.item");
        commentColumn.setId("forgotten_items.comment");
        nameColumn.setId("forgotten_items.name");
        emailColumn.setId("forgotten_items.contact");
        dateColumn.setId("forgotten_items.date");

        super.initialize();

        hutComboBox.setOnKeyReleased(this::formKeyReleaseHandler);
        datePicker.setOnKeyReleased(this::formKeyReleaseHandler);
        itemTextField.setOnKeyReleased(this::formKeyReleaseHandler);
        nameTextField.setOnKeyReleased(this::formKeyReleaseHandler);
        contactTextField.setOnKeyReleased(this::formKeyReleaseHandler);
        commentTextField.setOnKeyReleased(this::formKeyReleaseHandler);
    }

    private void formKeyReleaseHandler(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            commitButton.fire();
        }
    }

    @Override protected void loadPageImpl(Integer pageIndex) {
        Hut hut = hutFilter.getValue();
        if (hut == HUT_FILTER_NO_SELECTION) { hut = null; }
        LocalDate fromDate  = fromDateFilter.getValue();
        LocalDate toDate    = toDateFilter.getValue();

        Integer forgottenItemCount;
        ObservableList<ForgottenItem> forgottenItems;
        try {
            forgottenItemCount = dataModel.forgottenItemCount(hut, fromDate, toDate);
            forgottenItems = dataModel.forgottenItemPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE,
                                                         hut, fromDate, toDate, ordering);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.getItems().setAll(forgottenItems);

        setPageCount(forgottenItemCount);

        pagination.setCurrentPageIndex(pageIndex);
    }

    private void commitButtonHook() {
        Hut hut         = hutComboBox.getValue();
        LocalDate date  = datePicker.getValue();
        String item     = itemTextField.getText();
        String name     = nameTextField.getText();
        String contact  = contactTextField.getText();
        String comment  = commentTextField.getText();

        try {
            ForgottenItem forgottenItem = new ForgottenItem(-1, hut, item, name, contact, date, false, comment);
            dataModel.insertForgottenItem(forgottenItem);
        } catch (SQLException|NullPointerException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        itemTextField.clear();
        commentTextField.clear();

        loadPage(pagination.getCurrentPageIndex());
    }
}

package no.flaming_adventure.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.model.ForgottenItem;
import no.flaming_adventure.model.Hut;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.function.Consumer;

/**
 * Controller for the forgotten item tab, responsible for both table and form.
 */
public class ForgottenTableController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    static private final Integer ITEMS_PER_PAGE = 50;

    static private final LocalDate YESTERDAY = LocalDate.now().minusDays(1);

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private DataModel dataModel;
    private Consumer<Throwable> unhandledExceptionHook;

    @FXML private TableView<ForgottenItem>              tableView;
    @FXML private TableColumn<ForgottenItem, String>    hutColumn;
    @FXML private TableColumn<ForgottenItem, String>    itemColumn;
    @FXML private TableColumn<ForgottenItem, String>    commentColumn;
    @FXML private TableColumn<ForgottenItem, String>    nameColumn;
    @FXML private TableColumn<ForgottenItem, String>    emailColumn;
    @FXML private TableColumn<ForgottenItem, LocalDate> dateColumn;

    @FXML private Pagination pagination;

    @FXML private ComboBox<Hut> hutComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField itemTextField;
    @FXML private TextField nameTextField;
    @FXML private TextField contactTextField;
    @FXML private TextField commentTextField;
    @FXML private Button commitButton;

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
        ObservableList<Hut> huts;
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
     * Private API
     *
     ************************************************************************/

    /**
     * JavaFX initialization method.
     * <p>
     * Initialize static data and set defaults not depending on business data.
     * <p>
     * This method is called by JavaFX when all FXML dependencies have been injected. It should not be called by user
     * code.
     */
    @FXML
    private void initialize() {
        hutColumn.setCellValueFactory(param -> param.getValue().getHut().nameProperty());
        itemColumn.setCellValueFactory(param -> param.getValue().itemProperty());
        commentColumn.setCellValueFactory(param -> param.getValue().commentProperty());
        nameColumn.setCellValueFactory(param -> param.getValue().nameProperty());
        emailColumn.setCellValueFactory(param -> param.getValue().contactProperty());
        dateColumn.setCellValueFactory(param -> param.getValue().dateProperty());

        commitButton.setOnAction(ignored -> commitButtonHook());
    }

    private void loadPage(Integer pageIndex) {
        Integer forgottenItemCount;
        ObservableList<ForgottenItem> forgottenItems;
        try {
            forgottenItemCount = dataModel.forgottenItemCount();
            forgottenItems = dataModel.forgottenItemPage(pageIndex * ITEMS_PER_PAGE, ITEMS_PER_PAGE);
        } catch (SQLException e) {
            unhandledExceptionHook.accept(e);
            throw new IllegalStateException(e);
        }

        tableView.setItems(forgottenItems);

        if (forgottenItemCount == 0) {
            pagination.setPageCount(1);
        } else {
            pagination.setPageCount((forgottenItemCount + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        }

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

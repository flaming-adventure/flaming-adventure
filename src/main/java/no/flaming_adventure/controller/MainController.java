package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import no.flaming_adventure.model.DataModel;

import java.util.function.Consumer;

/**
 * Controller for the main view.
 *
 * <ul>
 *     <li>TODO #38 (enhancement): add data validation application-wide.
 * </ul>
 */
public class MainController {
    private final DataModel dataModel;
    private final Consumer<Throwable> unhandledExceptionHook;

    @FXML private ReservationFormController     reservationFormController;
    @FXML private ReservationTableController    reservationTableController;
    @FXML private ForgottenTableController      forgottenTableController;
    @FXML private EquipmentTableController      equipmentTableController;
    @FXML private BrokenItemTableController     brokenItemTableController;

    @FXML private TabPane   tabPane;
    @FXML private Tab       reservationFormTab;
    @FXML private Tab       reservationTableTab;
    @FXML private Tab       forgottenTab;
    @FXML private Tab       equipmentTab;
    @FXML private Tab       brokenTab;

    public MainController(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel  = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;
    }

    @FXML private void initialize() {
        reservationFormController.inject(dataModel, unhandledExceptionHook);
        reservationTableController.inject(dataModel, unhandledExceptionHook);
        forgottenTableController.inject(dataModel, unhandledExceptionHook);
        brokenItemTableController.inject(dataModel, unhandledExceptionHook);
        equipmentTableController.inject(dataModel, unhandledExceptionHook);

        tabPane.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, tab) -> loadTab(tab));

        loadTab(tabPane.getSelectionModel().getSelectedItem());
    }

    private void loadTab(Tab tab) {
        if (tab == reservationFormTab) {
            reservationFormController.load();
        } else if (tab == reservationTableTab) {
            reservationTableController.load();
        } else if (tab == forgottenTab) {
            forgottenTableController.load();
        } else if (tab == equipmentTab) {
            equipmentTableController.load();
        } else if (tab == brokenTab) {
            brokenItemTableController.load();
        }
    }
}

package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import no.flaming_adventure.model.DataModel;

/**
 * Controller for the main view.
 *
 * <p> The main controller simply manages the application's tab pane by making sure that the various subcontrollers
 * have access to the needed data and know when they're being displayed. The meat of the GUI logic can be found in
 * the tab content controllers.
 *
 * @see no.flaming_adventure.controller.ReservationFormController
 * @see no.flaming_adventure.controller.ReservationTableController
 * @see no.flaming_adventure.controller.ForgottenTableController
 * @see no.flaming_adventure.controller.EquipmentTableController
 * @see no.flaming_adventure.controller.BrokenItemTableController
 */
public class MainController {

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final DataModel dataModel;

    @FXML private OverviewController            overviewTableController;
    @FXML private ReservationFormController     reservationFormController;
    @FXML private ReservationTableController    reservationTableController;
    @FXML private ForgottenTableController      forgottenTableController;
    @FXML private EquipmentTableController      equipmentTableController;
    @FXML private BrokenItemTableController     brokenItemTableController;

    @FXML private TabPane   tabPane;
    @FXML private Tab       overviewTab;
    @FXML private Tab       reservationFormTab;
    @FXML private Tab       reservationTableTab;
    @FXML private Tab       forgottenTab;
    @FXML private Tab       equipmentTab;
    @FXML private Tab       brokenTab;

    /************************************************************************
     *
     * Constructors
     *
     ************************************************************************/

    /**
     * Construct a MainController with the given data model and exception hook.
     *
     * @param dataModel                 active data model for the application instance.
     */
    public MainController(DataModel dataModel) {
        this.dataModel  = dataModel;
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    /**
     * Initialize the controller.
     *
     * <p> This function is called by JavaFX after all UI elements and controllers have been injected. It is used
     * here to inject dependencies into the various subcontrollers and initialize tab-loading.
     */
    @FXML private void initialize() {
        overviewTableController.inject(dataModel);
        reservationFormController.inject(dataModel);
        reservationTableController.inject(dataModel);
        forgottenTableController.inject(dataModel);
        brokenItemTableController.inject(dataModel);
        equipmentTableController.inject(dataModel);

        tabPane.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, tab) -> loadTab(tab));

        loadTab(tabPane.getSelectionModel().getSelectedItem());
    }

    /**
     * Call the load() function of the controller corresponding to the given tab, if any.
     *
     * @param tab the tab whose corresponding controller's load() function should be called.
     * @see no.flaming_adventure.controller.ReservationFormController#load()
     * @see no.flaming_adventure.controller.ReservationTableController#load()
     * @see no.flaming_adventure.controller.ForgottenTableController#load()
     * @see no.flaming_adventure.controller.EquipmentTableController#load()
     * @see no.flaming_adventure.controller.BrokenItemTableController#load()
     */
    private void loadTab(Tab tab) {
        if (tab == overviewTab) {
            overviewTableController.load();
        } else if (tab == reservationFormTab) {
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

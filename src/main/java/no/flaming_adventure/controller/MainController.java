package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import no.flaming_adventure.model.DataModel;

import java.util.function.Consumer;

/**
 * Controller for the main view.
 *
 * <ul>
 *     <li>TODO (enhancement): unify date handling application-wide.
 *     <li>TODO (enhancement): only initialize controllers when the corresponding tab is first opened.
 *     <li>TODO #38 (enhancement): add data validation application-wide.
 *     <li>TODO #43 (enhancement): add error interface.
 *     <li>TODO (bug): handle empty selections.
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

    public MainController(DataModel dataModel, Consumer<Throwable> unhandledExceptionHook) {
        this.dataModel  = dataModel;
        this.unhandledExceptionHook = unhandledExceptionHook;
    }

    @FXML protected void initialize() {
        reservationFormController.inject(dataModel, unhandledExceptionHook);

        reservationFormController.load();

        reservationTableController.initializeData(dataModel);
        forgottenTableController.initializeData(dataModel);
        equipmentTableController.initializeData(dataModel);
        brokenItemTableController.initializeData(dataModel);
    }
}

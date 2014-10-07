package no.flaming_adventure.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import no.flaming_adventure.App;

public class MenuController {
    protected final App app;
    @FXML
    protected Button reserveButton;
    @FXML
    protected Button hutStatusButton;

    public MenuController(App app) {
        this.app = app;
    }

    @FXML
    protected void initialize() {
        reserveButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.showBooking();
            }
        });
        hutStatusButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                app.showHutStatus();
            }
        });
    }
}

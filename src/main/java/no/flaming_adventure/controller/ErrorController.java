package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.text.Text;

public class ErrorController {
    @FXML
    protected Text errorText;

    protected String errorMessage;

    public ErrorController(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @FXML
    protected void initialize() {
        errorText.setText(errorMessage);
    }
}

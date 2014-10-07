package no.flaming_adventure.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.flaming_adventure.App;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class LoginController {
    public static final String DB_DRIVER = "com.mysql.jdbc.Driver";
    protected final App app;
    @FXML
    protected TextField databaseURL;
    @FXML
    protected TextField databaseUser;
    @FXML
    protected PasswordField databasePass;
    @FXML
    protected Text errorText;
    @FXML
    protected Button logInButton;

    public LoginController(App app) {
        this.app = app;
    }

    @FXML
    protected void initialize() {
        logInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                logIn();
            }
        });
    }

    public void logIn() {
        try {
            // XXX: Done here to make for easier exception handling/error
            // signaling.
            Class.forName(DB_DRIVER);

            Connection connection = DriverManager.getConnection(databaseURL.getText(), databaseUser.getText(), databasePass.getText());
            app.connectionHook(connection);
        } catch (ClassNotFoundException e) {
            // TODO: Language.
            // TODO: Extract constant.
            errorText.setText("Unable to find database driver");
            errorText.setVisible(true);
            return;
        } catch (SQLException e) {
            int error = e.getErrorCode();
            if (error == 0) {
                // TODO: Language.
                // TODO: Extract constant.
                errorText.setText("Cannot connect to server");
            } else if (error == 1045) {
                // TODO: Language.
                // TODO: Extract constant.
                errorText.setText("Access denied for user");
            } else {
                // TODO: More informative (error code), formatted text.
                // TODO: Language.
                // TODO: Extract constant.
                errorText.setText("Unknown SQL error: " + e);
            }
            errorText.setVisible(true);
            return;
        }

        app.showMenu();
    }
}

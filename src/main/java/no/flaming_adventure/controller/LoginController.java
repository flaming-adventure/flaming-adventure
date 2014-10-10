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
    protected TextField URLField;
    @FXML
    protected TextField usernameField;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected Text errorText;
    @FXML
    protected Button logInButton;

    public LoginController(App app) {
        this.app = app;
    }

    @FXML
    protected void initialize() {
        URLField.setText(app.preferences.get(App.DATABASE_URL, ""));
        usernameField.setText(app.preferences.get(App.USERNAME, ""));
        passwordField.setText(app.preferences.get(App.PASSWORD, ""));
        logInButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                logIn();
            }
        });
    }

    public void logIn() {
        String URL = URLField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            // XXX: Done here to make for easier exception handling/error
            // signaling.
            Class.forName(DB_DRIVER);

            Connection connection = DriverManager.getConnection(URL, username, password);
            app.connectionHook(connection);
            app.preferences.put(App.DATABASE_URL, URL);
            app.preferences.put(App.USERNAME, username);
            app.preferences.put(App.PASSWORD, password);
        } catch (ClassNotFoundException e) {
            errorText.setText("Unable to find database driver");
            errorText.setVisible(true);
            return;
        } catch (SQLException e) {
            int error = e.getErrorCode();
            if (error == 0) {
                errorText.setText("Cannot connect to server");
            } else if (error == 1045) {
                errorText.setText("Access denied for user");
            } else {
                errorText.setText("Unknown SQL error: " + e);
            }
            errorText.setVisible(true);
            return;
        }

        app.showMenu();
    }
}

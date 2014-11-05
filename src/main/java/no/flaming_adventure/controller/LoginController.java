package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import no.flaming_adventure.App;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Controller for the login view.
 *
 * The login view is responsible for creating a connection to the database and calling the application's
 * connection hook with that connection.
 */
public class LoginController {
    /**
     * Database driver to use.
     *
     * Note that we could possibly allow more dynamic loading of database drivers, but it's not within the current
     * scope of the application.
     */
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    private final Logger logger;
    private final App app;

    /* Fields assigned by JavaFX at some point in time before initialize() is called. */
    @FXML private TextField URLField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Text errorText;
    @FXML private Button logInButton;

    public LoginController(Logger logger, App app) {
        this.logger = logger;
        this.app = app;
    }

    /**
     * Initialization function called when JavaFX is ready to initialize the controller.
     *
     * Sets the URL, username and password fields to data from the configuration if such data is available.
     *
     * TODO: make credential persistence optional.
     */
    @FXML private void initialize() {
        logger.info("Initializing login interface...");

        URLField.setText(app.preferences.get(App.DATABASE_URL, ""));
        if (! URLField.getText().isEmpty()) { logger.info("Database URL was set from configuration."); }
        usernameField.setText(app.preferences.get(App.USERNAME, ""));
        if (! usernameField.getText().isEmpty()) { logger.info("Username was set from configuration."); }
        passwordField.setText(app.preferences.get(App.PASSWORD, ""));
        if (! passwordField.getText().isEmpty()) { logger.info("Password was set from configuration."); }

        logInButton.setOnAction(event -> logIn());
    }

    /**
     * Attempt to log in to the database with the entered credentials.
     *
     * TODO: add database driver prefix if none is given.
     */
    private void logIn() {
        String URL = URLField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        logger.info("Attempting to log in to " + URL + " as " + username + "...");

        try {
            // XXX: Done here to make for easier exception handling/error
            // signaling.
            logger.info("Loading database driver...");
            Class.forName(DB_DRIVER);

            logger.info("Connecting to database...");
            Connection connection = DriverManager.getConnection(URL, username, password);

            logger.info("Storing user credentials...");
            app.preferences.put(App.DATABASE_URL, URL);
            app.preferences.put(App.USERNAME, username);
            app.preferences.put(App.PASSWORD, password);

            logger.info("Calling application connection hook...");
            app.connectionHook(connection);
        } catch (ClassNotFoundException e) {
            logger.warning("Unable to find database driver.");
            errorText.setText("Unable to find database driver");
            errorText.setVisible(true);
        } catch (SQLException e) {
            logger.warning("SQLException: " + e + ".");
            int error = e.getErrorCode();
            if (error == 0) {
                errorText.setText("Cannot connect to server");
            } else if (error == 1045) {
                errorText.setText("Access denied for user");
            } else {
                errorText.setText("Unknown SQL error: " + e);
            }
            errorText.setVisible(true);
        }
    }
}

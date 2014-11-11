package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Controller for the login view.
 *
 * <p> Responsible for creating a connection to the database and calling the
 * application's connection hook with that connection.
 */
public class LoginController {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    public static final String DATABASE_URL = "databaseURL";
    public static final String USERNAME     = "username";
    public static final String PASSWORD     = "password";

    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final Preferences           preferences;
    private final Consumer<Connection>  connectionHook;

    @FXML private TextField     URLField;
    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button        logInButton;

    /************************************************************************
     *
     * Constructors
     *
     ************************************************************************/

    public LoginController(Preferences preferences, Consumer<Connection> connectionHook) {
        this.preferences    = preferences;
        this.connectionHook = connectionHook;
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    /**
     * Initialization function called when JavaFX is ready to initialize the
     * controller.
     *
     * <p> Sets the URL, username and password fields to data from the
     * configuration if such data is available.
     *
     * <ul>
     *     <li>TODO #44 (enhancement): make credential persistence optional.
     * </ul>
     */
    @FXML private void initialize() {
        LOGGER.info("Initializing login interface...");

        URLField.setText(preferences.get(DATABASE_URL, ""));
        if (! URLField.getText().isEmpty()) { LOGGER.info("Database URL was set from configuration."); }
        usernameField.setText(preferences.get(USERNAME, ""));
        if (! usernameField.getText().isEmpty()) { LOGGER.info("Username was set from configuration."); }
        passwordField.setText(preferences.get(PASSWORD, ""));
        if (! passwordField.getText().isEmpty()) { LOGGER.info("Password was set from configuration."); }

        logInButton.setOnAction(event -> logIn());
    }

    /**
     * Attempt to log in to the database with the entered credentials.
     *
     * <ul>
     *     <li>TODO #34 (enhancement): add database driver prefix if none is given.
     *     <li>TODO #42 (enhancement): extract messages to localization file.
     * </ul>
     */
    private void logIn() {
        String URL = URLField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        LOGGER.log(Level.INFO, "Attempting to log in to {0} as {1}.",
                new Object[]{URL, username});

        try {
            LOGGER.log(Level.INFO, "Connecting to database.");
            Connection connection = DriverManager.getConnection(URL, username, password);

            LOGGER.log(Level.INFO, "Storing user credentials.");
            preferences.put(DATABASE_URL, URL);
            preferences.put(USERNAME, username);
            preferences.put(PASSWORD, password);

            connectionHook.accept(connection);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, e.getMessage(), e);

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connection error!");
            alert.setHeaderText("There was an error while connecting to the SQL server");
            alert.setContentText(e.getMessage());

            alert.showAndWait();
        }
    }
}

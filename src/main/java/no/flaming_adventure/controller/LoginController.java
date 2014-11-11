package no.flaming_adventure.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Consumer;
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

    /**
     * Database driver to use.
     *
     * <p> Note that we could possibly allow more dynamic loading of database
     * drivers, but it's not within the current scope of the application.
     */
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final Preferences preferences;
    private final Consumer<Connection> hook;

    @FXML private TextField URLField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Text errorText;
    @FXML private Button logInButton;

    /************************************************************************
     *
     * Constructors
     *
     ************************************************************************/

    public LoginController(Preferences preferences, Consumer<Connection> hook) {
        this.preferences = preferences;
        this.hook = hook;
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
     *     <li>TODO #43 (enhancement): make error interface consistent with main error interface.
     * </ul>
     */
    private void logIn() {
        String URL = URLField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        LOGGER.info("Attempting to log in to " + URL + " as " + username + "...");

        try {
            // XXX: Done here to make for easier exception handling/error
            // signaling.
            LOGGER.info("Loading database driver...");
            Class.forName(DB_DRIVER);

            LOGGER.info("Connecting to database...");
            Connection connection = DriverManager.getConnection(URL, username, password);

            LOGGER.info("Storing user credentials...");
            preferences.put(DATABASE_URL, URL);
            preferences.put(USERNAME, username);
            preferences.put(PASSWORD, password);

            LOGGER.info("Calling application connection hook...");
            hook.accept(connection);
        } catch (ClassNotFoundException e) {
            LOGGER.warning("Unable to find database driver.");
            errorText.setText("Unable to find database driver");
            errorText.setVisible(true);
        } catch (SQLException e) {
            LOGGER.warning("SQLException: " + e + ".");
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

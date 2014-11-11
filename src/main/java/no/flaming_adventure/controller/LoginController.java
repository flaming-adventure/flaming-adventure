package no.flaming_adventure.controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

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
    public static final String REMEMBER_ME  = "remember_me";

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
    @FXML private CheckBox      rememberMeCheckBox;
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
     */
    @FXML private void initialize() {
        LOGGER.info("Initializing login interface...");

        if (preferences.getBoolean(REMEMBER_ME, false)) {
            rememberMeCheckBox.setSelected(true);
            LOGGER.log(Level.INFO, "Getting user login data from configuration.");
            URLField.setText(preferences.get(DATABASE_URL, ""));
            usernameField.setText(preferences.get(USERNAME, ""));
            passwordField.setText(preferences.get(PASSWORD, ""));
            Platform.runLater(logInButton::requestFocus);
        }

        EventHandler<KeyEvent> enterHandler = event -> {
            if (event.getCode() == KeyCode.ENTER) {
                logInButtonActionHook(new Object());
            }
        };

        URLField.setOnKeyReleased(enterHandler);
        usernameField.setOnKeyReleased(enterHandler);
        passwordField.setOnKeyReleased(enterHandler);
        rememberMeCheckBox.setOnKeyReleased(enterHandler);
        logInButton.setOnAction(this::logInButtonActionHook);
    }

    /**
     * Attempt to log in to the database with the entered credentials.
     *
     * <ul>
     *     <li>TODO #42 (enhancement): extract messages to localization file.
     * </ul>
     */
    private void logInButtonActionHook(Object ignored) {
        String URL = URLField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (! URL.startsWith("jdbc:mysql://")) {
            URL = "jdbc:mysql://" + URL;
        }

        LOGGER.log(Level.INFO, "Attempting to log in to {0} as {1}.",
                new Object[]{URL, username});

        try {
            LOGGER.log(Level.INFO, "Connecting to database.");
            Connection connection = DriverManager.getConnection(URL, username, password);

            if (rememberMeCheckBox.isSelected()) {
                LOGGER.log(Level.INFO, "Storing user credentials.");
                preferences.put(DATABASE_URL, URL.replace("jdbc:mysql://", ""));
                preferences.put(USERNAME, username);
                preferences.put(PASSWORD, password);
                preferences.putBoolean(REMEMBER_ME, true);
            } else {
                // Make sure we don't keep old credentials around.
                preferences.remove(DATABASE_URL);
                preferences.remove(USERNAME);
                preferences.remove(PASSWORD);
                preferences.putBoolean(REMEMBER_ME, false);
            }

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

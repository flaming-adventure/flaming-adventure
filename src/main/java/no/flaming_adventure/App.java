package no.flaming_adventure;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.flaming_adventure.controller.LoginController;
import no.flaming_adventure.controller.MainController;
import no.flaming_adventure.model.DataModel;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class App extends Application {
    public static final String DATABASE_URL = "databaseURL";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public Preferences preferences = Preferences.userNodeForPackage(App.class);

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    private static final Logger logger = Logger.getAnonymousLogger();

    protected Stage stage;

    protected Scene loginScene;
    protected Scene mainScene;

    /**
     * Program entry point.
     *
     * @param args Commandline arguments.
     */
    public static void main(String[] args) {
        logger.setLevel(Level.ALL);
        logger.info("Launching...");
        launch(args);
    }

    /**
     * Attempt to load a view from the given filename and assign the given controller to it.
     *
     * @param filename   Name of FXML file in resources.
     * @param controller Controller object for the view.
     * @return A JavaFX scene object.
     */
    public static Scene loadScene(String filename, Object controller) {
        logger.info("Loading scene: \"" + filename + "\"...");

        URL resource = App.class.getClassLoader().getResource(filename);
        FXMLLoader loader = new FXMLLoader(resource);

        loader.setController(controller);

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            // XXX: We don't necessarily have any GUI capabilities at this point, so we fall back to logging the error
            //  and exiting.
            //
            // Note that this exception is extremely unlikely to occur in production.
            logger.severe("Failed to load scene: \"" + filename + "\"");
            System.exit(1);
        }

        logger.fine("\"" + filename + "\" loaded successfully.");
        return new Scene(root);
    }

    /**
     * Called by JavaFX when the stage is set for the application to run.
     *
     * @param stage Stage provided by JavaFX.
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        loginScene = loadScene("login.fxml", new LoginController(logger, this));

        stage.setTitle("Flaming Adventure");

        stage.setScene(loginScene);
        stage.show();
    }

    /**
     * Function to be called when a database connection is established.
     *
     * Responsible for starting up the meat of the application. No functions in this class should be called after
     * connectionHook().
     *
     * @param connection A connection to the database (server).
     * @throws SQLException
     */
    public void connectionHook(Connection connection) throws SQLException {
        mainScene = loadScene("main.fxml", new MainController(new DataModel(logger, connection)));
        stage.setScene(mainScene);
    }
}

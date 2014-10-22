package no.flaming_adventure;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.flaming_adventure.controller.LoginController;
import no.flaming_adventure.controller.MainController;
import no.flaming_adventure.model.*;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.prefs.Preferences;

public class App extends Application {
    public static final String DATABASE_URL = "databaseURL";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public Preferences preferences = Preferences.userNodeForPackage(App.class);

    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

    protected Stage stage;
    protected HutModel hutModel;
    protected BookingModel bookingModel;
    protected ForgottenModel forgottenModel;
    protected DestroyedModel destroyedModel;
    protected EquipmentModel equipmentModel;

    protected Scene loginScene;
    protected Scene mainScene;

    /**
     * Program entry point.
     *
     * @param args Commandline arguments.
     */
    public static void main(String[] args) {
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
        URL resource = App.class.getClassLoader().getResource(filename);
        FXMLLoader loader = new FXMLLoader(resource);

        loader.setController(controller);

        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            // XXX: We don't really know what GUI capabilities we have at
            //  this point, so fall back to printing an error message and
            //  exiting.
            //
            // Note that this exception really shouldn't occur in production.
            System.err.println("Failed to load resource: " + filename + e);
            System.exit(1);
        }

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

        loginScene = loadScene("login.fxml", new LoginController(this));

        stage.setTitle("Flaming Adventure");

        stage.setScene(loginScene);
        stage.show();
    }

    /**
     * @param connection A connection to the database (server).
     * @throws SQLException
     */
    public void connectionHook(Connection connection) throws SQLException {
        hutModel = new HutModel(connection);
        bookingModel = new BookingModel(connection, dateFormat);
        forgottenModel = new ForgottenModel(connection);
        destroyedModel = new DestroyedModel(connection);
        equipmentModel = new EquipmentModel(connection, dateFormat);

        mainScene = loadScene("main.fxml", new MainController(dateFormat, hutModel, bookingModel, forgottenModel,
                equipmentModel));
        stage.setScene(mainScene);
    }
}

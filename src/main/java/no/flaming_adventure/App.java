package no.flaming_adventure;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.flaming_adventure.controller.*;
import no.flaming_adventure.model.*;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

// TODO: Booking database commit.
// TODO: Forgotten class.
// TODO: ForgottenMode class.
// TODO: Destroyed class.
// TODO: DestroyedModel class.
// TODO: HutItem class.
// TODO: HutItemModel class.
// TODO: Status view (one hut).
// TODO: Status view (multiple huts).
// TODO: List of bookings (one hut).
// TODO: List of bookings (multiple huts).

/**
 * Main application class, contains the application entry point as well as being instantiated by JavaFX on launch.
 * Handles loading of models, views and controller as well as switching between views.
 */
public class App extends Application {
    protected Stage stage;

    protected HutModel hutModel;
    protected BookingModel bookingModel;
    protected ForgottenModel forgottenModel;
    protected DestroyedModel destroyedModel;
    protected EquipmentModel equipmentModel;

    protected Scene loginScene;
    protected Scene menuScene;
    protected Scene bookingScene;
    protected Scene hutStatusScene;

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
        menuScene = loadScene("menu.fxml", new MenuController(this));

        stage.setTitle("Flaming Adventure");

        showLogin();
        stage.show();
    }

    /**
     * Function called when a connection to the database (server) is established. Responsible for setting up the *Model
     * classes as well as initializing any views/controller using these.
     *
     * @param connection A connection to the database (server).
     * @throws SQLException
     */
    public void connectionHook(Connection connection) throws SQLException {
        hutModel = new HutModel(connection);
        bookingModel = new BookingModel(connection);
        forgottenModel = new ForgottenModel(connection);
        destroyedModel = new DestroyedModel(connection);
        equipmentModel = new EquipmentModel(connection);

        bookingScene = loadScene("booking.fxml",
                new BookingController(this, hutModel, bookingModel));
        hutStatusScene = loadScene("hut_status.fxml",
                new HutStatusController(this, hutModel, bookingModel, forgottenModel, destroyedModel, equipmentModel));
    }

    /**
     * Show the booking view.
     */
    public void showBooking() {
        stage.setScene(bookingScene);
    }

    /**
     * Show the login view.
     */
    public void showLogin() {
        stage.setScene(loginScene);
    }

    /**
     * Show the menu view.
     */
    public void showMenu() {
        stage.setScene(menuScene);
    }

    public void showHutStatus() {
        stage.setScene(hutStatusScene);
    }

    /**
     * Show the error view with the given message.
     */
    public void showError(String errorMessage) {
        stage.setScene(loadScene("error.fxml", new ErrorController(errorMessage)));
    }
}

package no.flaming_adventure;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import no.flaming_adventure.controller.LoginController;
import no.flaming_adventure.controller.MainController;
import no.flaming_adventure.model.DataModel;
import no.flaming_adventure.util.UnhandledExceptionDialog;

import java.net.URL;
import java.sql.Connection;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The main application entry point.
 *
 * <p> This class is responsible for launching the JavaFX application and contains
 * code to set up the environment that the
 * {@link no.flaming_adventure.controller.MainController main controller} needs to
 * run.
 */
public class App extends Application {

    /***************************************************************************
     *                                                                         *
     * Static variables and methods                                            *
     *                                                                         *
     **************************************************************************/

    /**
     * The application's locale.
     *
     * <p> This is set here and used globally because Java's locale detection is a bit
     * wonky, and because the application has been developed for a Norwegian audience.
     *
     * <p> If this needs to be changed a multitude of hardcoded strings need to be
     * localized throughout the application.
     */
    public static final Locale LOCALE = new Locale("NO", "no");

    // TODO: move to OverviewController.
    public static final NumberFormat NUMBER_FORMAT_PERCENT = NumberFormat.getPercentInstance(LOCALE);

    /**
     * A date formatter using the {@link App#LOCALE locale} set by the application.
     */
    public static final  DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                                                                                  .withLocale(LOCALE);

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    /**
     * The database driver to use.
     *
     * <p> Note that we could possibly allow more dynamic loading of database
     * drivers, but it's not within the current scope of the application.
     */
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    /**
     * Program entry point.
     *
     * @param args commandline arguments.
     */
    public static void main(String[] args) {
        LOGGER.log(Level.FINEST, "Launching application.");
        launch(args);
    }

    /***************************************************************************
     *                                                                         *
     * Instance variables                                                      *
     *                                                                         *
     **************************************************************************/

    private Stage stage;

    /***************************************************************************
     *                                                                         *
     * Public API                                                              *
     *                                                                         *
     **************************************************************************/

    /**
     * Called by JavaFX when the stage is set for the application to run.
     *
     * <p> Note that this function shouldn't be called by user code.
     *
     * @param stage the stage provided by JavaFX.
     */
    @Override public void start(Stage stage) {
        Locale.setDefault(LOCALE);
        this.stage = stage;

        LOGGER.log(Level.INFO, "Loading database driver...");
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            UnhandledExceptionDialog.create(e);
            throw new IllegalStateException(e);
        }

        LoginController loginController = new LoginController(this::connectionHook);

        Scene loginScene = loadScene("login.fxml", loginController);

        stage.setTitle("Flaming Adventure");
        stage.setScene(loginScene);
        stage.centerOnScreen();
        stage.show();
    }

    /***************************************************************************
     *                                                                         *
     * Implementation                                                          *
     *                                                                         *
     **************************************************************************/

    /**
     * Function to be called when a database connection is established.
     *
     * @param connection A connection to the database (server).
     */
    private void connectionHook(Connection connection)  {
        DataModel dataModel;
        try {
            dataModel = new DataModel(connection);
        } catch (Exception e) {
            UnhandledExceptionDialog.create(e);
            throw new IllegalStateException(e);
        }

        MainController mainController = new MainController(dataModel);
        Scene mainScene = loadScene("main.fxml", mainController);
        stage.hide();
        stage.setScene(mainScene);
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Attempt to load a view from the given filename and assign the given
     * controller to it.
     *
     * @param filename   Name of FXML file in resources.
     * @param controller Controller object for the view.
     * @return A JavaFX scene object.
     */
    private Scene loadScene(String filename, Object controller) {
        LOGGER.log(Level.INFO, "Loading scene: \"{0}\".", filename);

        URL resource = App.class.getClassLoader().getResource(filename);
        FXMLLoader loader = new FXMLLoader(resource);

        loader.setController(controller);

        Parent root;
        try {
            root = loader.load();
        } catch (Exception e) {
            UnhandledExceptionDialog.create(e);
            throw new IllegalStateException(e);
        }

        return new Scene(root);
    }
}

package no.flaming_adventure;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import no.flaming_adventure.controller.LoginController;
import no.flaming_adventure.controller.MainController;
import no.flaming_adventure.model.DataModel;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends Application {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    /**
     * Database driver to use.
     *
     * <p> Note that we could possibly allow more dynamic loading of database
     * drivers, but it's not within the current scope of the application.
     */
    private static final String DB_DRIVER = "com.mysql.jdbc.Driver";

    /************************************************************************
     *
     * Static methods
     *
     ************************************************************************/

    /**
     * Program entry point.
     *
     * @param args commandline arguments.
     */
    public static void main(String[] args) {
        LOGGER.log(Level.FINEST, "Launching application.");
        launch(args);
    }

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private Stage stage;

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    /**
     * Called by JavaFX when the stage is set for the application to run.
     *
     * <p> Note that this function shouldn't be called by user code.
     *
     * @param stage Stage provided by JavaFX.
     */
    @Override
    public void start(Stage stage) {
        Locale.setDefault(Util.LOCALE);
        this.stage = stage;

        LOGGER.log(Level.INFO, "Loading database driver...");
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            unhandledExceptionHook(e);
            throw new IllegalStateException(e);
        }

        LoginController loginController = new LoginController(this::connectionHook);

        Scene loginScene = loadScene("login.fxml", loginController);

        stage.setTitle("Flaming Adventure");
        stage.setScene(loginScene);
        stage.centerOnScreen();
        stage.show();
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    /**
     * Display an error dialog showing the given throwable and exit.
     *
     * @param throwable the throwable to display.
     */
    private void unhandledExceptionHook(Throwable throwable) {
        // Create the alert dialog.
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error!");
        alert.setHeaderText("An unhandled throwable occurred.");
        alert.setContentText(throwable.getMessage());

        // Get the stack trace.
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        String stackTrace = stringWriter.toString();

        // Create the stack trace pane.
        Label label = new Label("Stack trace:");
        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(false);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane exceptionContent = new GridPane();
        exceptionContent.setMaxWidth(Double.MAX_VALUE);
        exceptionContent.add(label, 0, 0);
        exceptionContent.add(textArea, 0, 1);

        // Add the stack trace pane to the alert dialog.
        alert.getDialogPane().setExpandableContent(exceptionContent);

        alert.showAndWait();

        LOGGER.log(Level.SEVERE, throwable.toString(), throwable);
        stage.close();
        Platform.exit();
    }

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
            unhandledExceptionHook(e);
            throw new IllegalStateException(e);
        }

        MainController mainController = new MainController(dataModel, this::unhandledExceptionHook);
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
            unhandledExceptionHook(e);
            throw new IllegalStateException(e);
        }

        return new Scene(root);
    }
}

package no.flaming_adventure.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UnhandledExceptionDialog {
    private static final Logger LOGGER = Logger.getLogger(UnhandledExceptionDialog.class.getName());

    public static void create(Throwable throwable) {
        // Create the alert dialog.
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Feil!");
        alert.setHeaderText("En ukjent feil oppstod.");
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

        LOGGER.log(Level.SEVERE, throwable.toString(), throwable);

        alert.showAndWait();
        Platform.exit();
    }
}

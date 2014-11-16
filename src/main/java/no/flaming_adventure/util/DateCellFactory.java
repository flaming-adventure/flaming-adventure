package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import java.time.LocalDate;

public class DateCellFactory<X>
        implements Callback<TableColumn<X, LocalDate>, TableCell<X, LocalDate>> {
    @Override public TableCell<X, LocalDate> call(TableColumn<X, LocalDate> param) {
        return new DateCell<>();
    }
}

package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class PercentageCellFactory<X>
        implements Callback<TableColumn<X, Number>, TableCell<X, Number>> {
    @Override public TableCell<X, Number> call(TableColumn<X, Number> param) {
        return new PercentageCell<>();
    }
}

package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import no.flaming_adventure.Util;

public class PercentageCell<X> extends TableCell<X, Number> {
    @Override protected void updateItem(Number item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            setText(Util.NUMBER_FORMAT_PERCENT.format(item));
        } else {
            setText(null);
        }
    }
}

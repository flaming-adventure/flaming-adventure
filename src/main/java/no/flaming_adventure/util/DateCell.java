package no.flaming_adventure.util;

import javafx.scene.control.TableCell;
import no.flaming_adventure.Util;

import java.time.LocalDate;

public class DateCell<X> extends TableCell<X, LocalDate> {
    @Override protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null) {
            setText(item.format(Util.DATE_TIME_FORMATTER));
        } else {
            setText(null);
        }
    }
}

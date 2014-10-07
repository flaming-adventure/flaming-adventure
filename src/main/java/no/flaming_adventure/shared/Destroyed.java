package no.flaming_adventure.shared;

import javafx.beans.property.*;

public class Destroyed {
    protected final IntegerProperty ID;
    protected final IntegerProperty bookingID;
    protected final StringProperty item;
    protected final BooleanProperty fixed;

    public Destroyed(Integer ID, Integer bookingID, String item, Boolean fixed) {
        this.ID = new SimpleIntegerProperty(ID);
        this.bookingID = new SimpleIntegerProperty(bookingID);
        this.item = new SimpleStringProperty(item);
        this.fixed = new SimpleBooleanProperty(fixed);
    }

    public int getID() {
        return ID.get();
    }

    public IntegerProperty IDProperty() {
        return ID;
    }

    public int getBookingID() {
        return bookingID.get();
    }

    public IntegerProperty bookingIDProperty() {
        return bookingID;
    }

    public String getItem() {
        return item.get();
    }

    public StringProperty itemProperty() {
        return item;
    }

    public boolean getFixed() {
        return fixed.get();
    }

    public BooleanProperty fixedProperty() {
        return fixed;
    }
}

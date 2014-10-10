package no.flaming_adventure.shared;

import javafx.beans.property.*;

public class Forgotten {
    protected IntegerProperty ID;
    protected IntegerProperty bookingID;
    protected StringProperty item;
    protected BooleanProperty delivered;
    protected StringProperty comment;

    public Forgotten(Integer ID, Integer bookingID, String item, Boolean delivered, String comment) {
        this.ID = new SimpleIntegerProperty(ID);
        this.bookingID = new SimpleIntegerProperty(bookingID);
        this.item = new SimpleStringProperty(item);
        this.delivered = new SimpleBooleanProperty(delivered);
        this.comment = new SimpleStringProperty(comment);
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

    public boolean getDelivered() {
        return delivered.get();
    }

    public BooleanProperty deliveredProperty() {
        return delivered;
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }
}

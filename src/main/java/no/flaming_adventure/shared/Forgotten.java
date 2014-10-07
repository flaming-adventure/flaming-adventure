package no.flaming_adventure.shared;

import javafx.beans.property.*;

public class Forgotten {
    protected IntegerProperty ID;
    protected IntegerProperty bookingID;
    protected StringProperty item;
    protected BooleanProperty delivered;
    protected StringProperty comment;

    // These belong to the booking, but the program becomes much simpler if we keep them here as well.
    protected StringProperty date; // There is no date property, so we just ignore the issue for now.
    protected StringProperty name;
    protected StringProperty email;

    public Forgotten(Integer ID, Integer bookingID, String item, Boolean delivered, String comment,
                     String date, String name, String email) {
        this.ID = new SimpleIntegerProperty(ID);
        this.bookingID = new SimpleIntegerProperty(bookingID);
        this.item = new SimpleStringProperty(item);
        this.delivered = new SimpleBooleanProperty(delivered);
        this.comment = new SimpleStringProperty(comment);

        this.date = new SimpleStringProperty(date);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
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

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }
}

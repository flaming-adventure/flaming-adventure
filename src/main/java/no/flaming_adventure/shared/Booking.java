package no.flaming_adventure.shared;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Booking {
    protected final IntegerProperty ID;
    protected final IntegerProperty hutID;
    protected final StringProperty date;
    protected final StringProperty name;
    protected final StringProperty email;
    protected final IntegerProperty count;
    protected final StringProperty comment;

    public Booking(Integer ID, Integer hutID, String date, String name, String email, Integer count, String comment) {
        this.ID = new SimpleIntegerProperty(ID);
        this.hutID = new SimpleIntegerProperty(hutID);
        this.date = new SimpleStringProperty(date);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.count = new SimpleIntegerProperty(count);
        this.comment = new SimpleStringProperty(comment);
    }

    public int getID() {
        return ID.get();
    }

    public IntegerProperty IDProperty() {
        return ID;
    }

    public int getHutID() {
        return hutID.get();
    }

    public IntegerProperty hutIDProperty() {
        return hutID;
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

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }
}

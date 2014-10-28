package no.flaming_adventure.shared;

import javafx.beans.property.*;
import javafx.util.StringConverter;

import java.util.Date;

public class Reservation {
    public static final StringConverter<Reservation> nameEmailConverter = new StringConverter<Reservation>() {
        @Override
        public String toString(Reservation object) {
            return object.getName() + " (" + object.getEmail() + ")";
        }

        @Override
        public Reservation fromString(String string) {
            return null;
        }
    };

    public Hut getHut() {
        return hut;
    }

    private final Hut hut;

    protected final IntegerProperty ID;
    protected final IntegerProperty hutID;
    protected final Property<Date>  date;
    protected final StringProperty  name;
    protected final StringProperty  email;
    protected final IntegerProperty count;
    protected final StringProperty  comment;

    public Reservation(Hut hut, Integer ID, Integer hutID, Date date, String name, String email, Integer count,
                       String comment) {
        this.hut = hut;
        this.ID = new SimpleIntegerProperty(ID);
        this.hutID = new SimpleIntegerProperty(hutID);
        this.date = new SimpleObjectProperty<>(date);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.count = new SimpleIntegerProperty(count);
        this.comment = new SimpleStringProperty(comment);
    }

    public void setID(int ID) {
        this.ID.set(ID);
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

    public Date getDate() { return date.getValue(); }

    public Property<Date> dateProperty() {
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

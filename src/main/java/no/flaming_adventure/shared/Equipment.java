package no.flaming_adventure.shared;

import javafx.beans.property.*;

import java.util.Date;

public class Equipment {
    public Hut getHut() {
        return hut;
    }

    private final Hut hut;

    protected final IntegerProperty ID;
    protected final IntegerProperty hutID;
    protected final StringProperty item;
    protected final Property<Date> dateProperty;        // TODO: Rename to date.
                                                        // TODO: Getters.
    protected final StringProperty date;                // TODO: Rename to dateString or remove.
                                                        // TODO: Bind to dateProperty.
    protected final IntegerProperty count;

    public Equipment(Hut hut, Integer ID, Integer hutID, String item, Date date, Integer count) {
        this.hut            = hut;
        this.ID             = new SimpleIntegerProperty(ID);
        this.hutID          = new SimpleIntegerProperty(hutID);
        this.item           = new SimpleStringProperty(item);
        this.dateProperty   = new SimpleObjectProperty<>(date);
        this.date           = new SimpleStringProperty(date.toString());  // TODO: Proper string formatting.
        this.count          = new SimpleIntegerProperty(count);
    }

    public String getItem() {
        return item.get();
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

    public StringProperty itemProperty() {
        return item;
    }

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }
}

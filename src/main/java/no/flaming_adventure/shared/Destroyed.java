package no.flaming_adventure.shared;

import javafx.beans.property.*;

public class Destroyed {
    protected IntegerProperty ID;
    protected StringProperty item;
    protected BooleanProperty fixed;

    public Destroyed(Integer ID, String item, Boolean fixed) {
        this.ID = new SimpleIntegerProperty(ID);
        this.item = new SimpleStringProperty(item);
        this.fixed = new SimpleBooleanProperty(fixed);
    }

    public int getID() {
        return ID.get();
    }

    public IntegerProperty IDProperty() {
        return ID;
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

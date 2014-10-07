package no.flaming_adventure.shared;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Hut {
    protected final IntegerProperty ID;
    protected final StringProperty name;
    protected final IntegerProperty capacity;
    protected final IntegerProperty firewood;

    /**
     * Constructor.
     *
     * @param ID       ID of the hut within the database, should be null if the hut does not yet exist in the database.
     * @param name     Name of the hut.
     * @param capacity Total number of beds in the hut.
     * @param firewood The number of sacks of firewood in the hut.
     */
    public Hut(Integer ID, String name, Integer capacity, Integer firewood) {
        this.ID = new SimpleIntegerProperty(ID);
        this.name = new SimpleStringProperty(name);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.firewood = new SimpleIntegerProperty(firewood);
    }

    public int getID() {
        return ID.get();
    }

    public IntegerProperty IDProperty() {
        return ID;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getCapacity() {
        return capacity.get();
    }

    public IntegerProperty capacityProperty() {
        return capacity;
    }

    public int getFirewood() {
        return firewood.get();
    }

    public IntegerProperty firewoodProperty() {
        return firewood;
    }
}

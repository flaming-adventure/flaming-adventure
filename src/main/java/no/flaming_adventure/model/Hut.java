package no.flaming_adventure.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A hut.
 */
public class Hut {
    /**
     * The database ID (primary key) of the object.
     *
     * <p> This can contain null if the object has not been added to the database. */
    private final IntegerProperty   id;

    /**
     * The name of the hut.
     */
    private final StringProperty    name;

    /**
     * The maximum number of people that can stay at the hut at the same time.
     */
    private final IntegerProperty   capacity;

    /**
     * The number of bags of firewood currently at the hut.
     */
    private final IntegerProperty   firewood;

    /**
     * Constructor.
     *
     * @param id       ID of the hut within the database, should be null if the hut does not yet exist in the database.
     * @param name     Name of the hut.
     * @param capacity Total number of beds in the hut.
     * @param firewood The number of sacks of firewood in the hut.
     */
    public Hut(Integer id, String name, Integer capacity, Integer firewood) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.capacity = new SimpleIntegerProperty(capacity);
        this.firewood = new SimpleIntegerProperty(firewood);
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
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

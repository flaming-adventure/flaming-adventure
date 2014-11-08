package no.flaming_adventure.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * One or more pieces of equipment belonging to a hut.
 */
public class Equipment {
    /**
     * The database ID (primary key) of the object.
     *
     * <p> This can contain null if the object has not been added to the database. */
    private final IntegerProperty           id;

    /**
     * The hut to which the equipment belongs.
     */
    private final ObjectProperty<Hut>       hut;

    /**
     * A short descriptive name (e.g. guitar).
     */
    private final StringProperty            name;

    /**
     * The date at which the equipment was purchased. Can contain null.
     */
    private final ObjectProperty<LocalDate> purchaseDate;

    /**
     * How many pieces of equipment belonging to the given hut was purchased at the given date.
     */
    private final IntegerProperty           count;

    /**
     * Construct an equipment object with the given fields.
     *
     * @param id            the primary key of the database record. May be null if no database record exists.
     * @param hut           the at which the equipment resides
     * @param name          a short descriptive name (e.g. canoe).
     * @param purchaseDate  the date at which the equipment was purchased.
     * @param count         a count for the given item at the given hut for the given purchase date.
     */
    public Equipment(Integer id, Hut hut, String name, LocalDate purchaseDate, Integer count) {
        this.id = new SimpleIntegerProperty(id);
        this.hut = new SimpleObjectProperty<>(hut);
        this.name = new SimpleStringProperty(name);
        this.purchaseDate = new SimpleObjectProperty<>(purchaseDate);
        this.count = new SimpleIntegerProperty(count);
    }

    /**
     * Set the database ID (primary key) of the object.
     *
     * @param id the database ID of the matching record.
     */
    public void setId(int id) {
        this.id.set(id);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public Hut getHut() {
        return hut.get();
    }

    public ObjectProperty<Hut> hutProperty() {
        return hut;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate.get();
    }

    public ObjectProperty<LocalDate> purchaseDateProperty() {
        return purchaseDate;
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }
}

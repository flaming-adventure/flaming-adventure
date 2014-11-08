package no.flaming_adventure.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * A broken item in one of the huts.
 *
 * <p> Each broken item keeps track of its individual hut, what it is, whether it's repaired. In addition it can
 * maintain a comment and the date at which the item was broken/reported broken.
 */
public class BrokenItem {
    /**
     * The database ID (primary key) of the object.
     *
     * <p> This can contain null if the object has not been added to the database. */
    private final IntegerProperty           id;

    /**
     * The hut containing the broken item.
     */
    private final ObjectProperty<Hut>       hut;

    /**
     * What the item is.
     */
    private final StringProperty            item;

    /**
     * The date at which the item was broken/reported broken. Can contain null.
     */
    private final ObjectProperty<LocalDate> date;

    /**
     * Whether the item has been fixed.
     */
    private final BooleanProperty           fixed;

    /**
     * A comment. Can contain null.
     */
    private final StringProperty            comment;

    /**
     * Construct a broken item with the given fields.
     *
     * @param id        the primary key of the database record. This may be null if the object isn't in the database.
     * @param hut       the hut containing the broken item.
     * @param item      a short name for the item (e.g. dishwasher).
     * @param date      the date at which the item was broken/reported broken. Can be null.
     * @param fixed     Whether the item has been fixed.
     * @param comment   A comment. Can be null.
     */
    public BrokenItem(Integer id, Hut hut, String item, LocalDate date, Boolean fixed, String comment) {
        this.id = new SimpleIntegerProperty(id);
        this.hut = new SimpleObjectProperty<>(hut);
        this.item = new SimpleStringProperty(item);
        this.date = new SimpleObjectProperty<>(date);
        this.fixed = new SimpleBooleanProperty(fixed);
        this.comment = new SimpleStringProperty(comment);
    }

    /**
     * Set the database ID (primary key) of the object.
     *
     * @param id the database ID of the matching record.
     */
    public void setId(Integer id) {
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

    public String getItem() {
        return item.get();
    }

    public StringProperty itemProperty() {
        return item;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public boolean getFixed() {
        return fixed.get();
    }

    public BooleanProperty fixedProperty() {
        return fixed;
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }
}

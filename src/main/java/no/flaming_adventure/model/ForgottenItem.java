package no.flaming_adventure.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * An item forgotten by someone at a hut.
 */
public class ForgottenItem {
    /**
     * The database ID (primary key) of the object.
     *
     * <p> This can contain null if the object has not been added to the database. */
    private final IntegerProperty           id;

    /**
     * The hut at which the item was forgotten.
     */
    private final ObjectProperty<Hut>       hut;

    /**
     * A short descriptive name for the item (e.g. jacket).
     */
    private final StringProperty            item;

    /**
     * The name of the person to which the item belongs.
     */
    private final StringProperty            name;

    /**
     * Contact information for the person to which the item belongs.
     */
    private final StringProperty            contact;

    /**
     * The date at which the item was forgotten. Can contain null.
     */
    private final ObjectProperty<LocalDate> date;

    /**
     * Whether the item has been delivered.
     */
    private final BooleanProperty           delivered;

    /**
     * A comment. Can contain null.
     */
    private final StringProperty            comment;

    /**
     * Construct a forgotten item object with the given fields.
     *
     * @param id        the primary key of the database record. May be null if no database record exists.
     * @param hut       the hut at which the item was forgotten.
     * @param item      a short descriptive name for the forgotten item (e.g. shoe).
     * @param name      the name of the owner of the item.
     * @param contact   contact information for the owner of the item.
     * @param date      the date at which the item was forgotten, can be null.
     * @param delivered whether the item has been delivered.
     * @param comment   a comment, can be null.
     */
    public ForgottenItem(Integer id, Hut hut, String item, String name, String contact, LocalDate date,
                         Boolean delivered, String comment) {
        this.id = new SimpleIntegerProperty(id);
        this.hut = new SimpleObjectProperty<>(hut);
        this.item = new SimpleStringProperty(item);
        this.name = new SimpleStringProperty(name);
        this.contact = new SimpleStringProperty(contact);
        this.date = new SimpleObjectProperty<>(date);
        this.delivered = new SimpleBooleanProperty(delivered);
        this.comment = new SimpleStringProperty(comment);
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

    public String getItem() {
        return item.get();
    }

    public StringProperty itemProperty() {
        return item;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getContact() {
        return contact.get();
    }

    public StringProperty contactProperty() {
        return contact;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
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

package no.flaming_adventure.shared;

import javafx.beans.property.*;

/**
 * A destroyed (alternatively out of order), item from one of the huts.
 */
public class Destroyed {
    /** The reservation object this item refers to. */
    private final Reservation reservation;
    /** The database ID (primary key) of the object, or -1 if the object doesn't exist in the database. */
    private final IntegerProperty ID;
    /** The database ID of the reservation. */
    private final IntegerProperty reservationID;
    /** A short name for the destroyed item (e.g. dishwasher). */
    private final StringProperty item;
    /** Whether the item has been fixed. */
    private final BooleanProperty fixed;

    /**
     * Construct a destroyed object with the given fields.
     *
     * Note that all fields with the exception of ID should be constant after construction.
     * ID is left mutable so that we can create objects before inserting them into the database.
     *
     * @param reservation       The reservation *object* this item refers to.
     * @param ID                The database ID (primary key) of the object. Should be -1 if the object does not yet
     *                          exist in the database.
     * @param reservationID     The database ID of the reservation object. (TODO: derive this from `reservation`.)
     * @param item              A short name for the destroyed item (e.g. dishwasher). (TODO: max length.)
     * @param fixed             Whether the item has been fixed.
     */
    public Destroyed(Reservation reservation, Integer ID, Integer reservationID, String item, Boolean fixed) {
        this.reservation = reservation;
        this.ID = new SimpleIntegerProperty(ID);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.item = new SimpleStringProperty(item);
        this.fixed = new SimpleBooleanProperty(fixed);
    }

    public Reservation getReservation() {
        return reservation;
    }

    /**
     * Set the database ID of the object.
     *
     * This function should only be called when the object is inserted into the database.
     */
    public void setID(int ID) {
        this.ID.set(ID);
    }

    public int getID() {
        return ID.get();
    }

    public IntegerProperty IDProperty() {
        return ID;
    }

    public int getReservationID() {
        return reservationID.get();
    }

    public IntegerProperty reservationIDProperty() {
        return reservationID;
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

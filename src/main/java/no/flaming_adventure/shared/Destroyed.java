package no.flaming_adventure.shared;

import javafx.beans.property.*;

public class Destroyed {
    private final Reservation reservation;

    public Reservation getReservation() {
        return reservation;
    }

    public void setID(int ID) {
        this.ID.set(ID);
    }

    protected final IntegerProperty ID;
    protected final IntegerProperty reservationID;
    protected final StringProperty item;
    protected final BooleanProperty fixed;

    public Destroyed(Reservation reservation, Integer ID, Integer reservationID, String item, Boolean fixed) {
        this.reservation = reservation;
        this.ID = new SimpleIntegerProperty(ID);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.item = new SimpleStringProperty(item);
        this.fixed = new SimpleBooleanProperty(fixed);
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

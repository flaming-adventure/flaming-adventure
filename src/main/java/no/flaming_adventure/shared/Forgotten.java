package no.flaming_adventure.shared;

import javafx.beans.property.*;

public class Forgotten {
    public Reservation getReservation() {
        return reservation;
    }

    private final Reservation reservation;

    protected IntegerProperty ID;
    protected IntegerProperty reservationID;
    protected StringProperty item;
    protected BooleanProperty delivered;
    protected StringProperty comment;

    public Forgotten(Reservation reservation, Integer ID, Integer reservationID, String item, Boolean delivered, String comment) {
        this.reservation = reservation;
        this.ID = new SimpleIntegerProperty(ID);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.item = new SimpleStringProperty(item);
        this.delivered = new SimpleBooleanProperty(delivered);
        this.comment = new SimpleStringProperty(comment);
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

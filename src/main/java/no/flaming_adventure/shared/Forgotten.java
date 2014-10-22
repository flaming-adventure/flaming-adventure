package no.flaming_adventure.shared;

import javafx.beans.property.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Forgotten {
    protected IntegerProperty ID;
    protected IntegerProperty reservationID;
    protected StringProperty item;
    protected BooleanProperty delivered;
    protected StringProperty comment;

    public Forgotten(Integer ID, Integer reservationID, String item, Boolean delivered, String comment) {
        this.ID = new SimpleIntegerProperty(ID);
        this.reservationID = new SimpleIntegerProperty(reservationID);
        this.item = new SimpleStringProperty(item);
        this.delivered = new SimpleBooleanProperty(delivered);
        this.comment = new SimpleStringProperty(comment);
    }

    public static Forgotten fromResultSet(ResultSet resultSet) throws SQLException {
        return new Forgotten(
                resultSet.getInt("id"),
                resultSet.getInt("reservation_id"),
                resultSet.getString("item"),
                resultSet.getBoolean("delivered"),
                resultSet.getString("comment"));
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

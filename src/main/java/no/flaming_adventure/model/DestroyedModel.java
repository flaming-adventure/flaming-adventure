package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Destroyed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DestroyedModel {
    protected PreparedStatement forBookingStmt;

    public DestroyedModel(Connection connection) throws SQLException {
        forBookingStmt = connection.prepareStatement("SELECT * FROM out_of_order WHERE reservation_id=?;");
    }

    public ArrayList<Destroyed> itemsForBooking(Booking booking) throws SQLException {
        ArrayList<Destroyed> ret = new ArrayList<Destroyed>();

        forBookingStmt.setInt(1, booking.getID());

        ResultSet resultSet = forBookingStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Destroyed(
                            resultSet.getInt("id"),
                            resultSet.getInt("reservation_id"),
                            resultSet.getString("item"),
                            resultSet.getBoolean("fixed"))
            );
        }

        return ret;
    }

    public ArrayList<Destroyed> itemsForBookings(Iterable<Booking> bookings) throws SQLException {
        ArrayList<Destroyed> ret = new ArrayList<>();

        for (Booking booking : bookings) {
            ret.addAll(itemsForBooking(booking));
        }

        return ret;
    }
}

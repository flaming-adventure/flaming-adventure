package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Forgotten;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class ForgottenModel {
    protected PreparedStatement forBookingStmt;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyy");

    public ForgottenModel(Connection connection) throws SQLException {
        forBookingStmt = connection.prepareStatement("SELECT * FROM Glemt WHERE Booking=?;");
    }

    public ArrayList<Forgotten> itemsForBooking(Booking booking) throws SQLException {
        ArrayList<Forgotten> ret = new ArrayList<Forgotten>();

        forBookingStmt.setInt(1, booking.getID());

        ResultSet resultSet = forBookingStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Forgotten(
                    resultSet.getInt("ID"),
                    resultSet.getInt("Booking"),
                    resultSet.getString("Ting"),
                    resultSet.getBoolean("Levert"),
                    resultSet.getString("Kommentar")));
        }

        return ret;
    }

    public ArrayList<Forgotten> itemsForBookings(Iterable<Booking> bookings) throws SQLException {
        ArrayList<Forgotten> ret = new ArrayList<>();

        for (Booking booking : bookings) {
            ret.addAll(itemsForBooking(booking));
        }

        return ret;
    }
}

package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Forgotten;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

public class ForgottenModel {
    protected PreparedStatement forBookingStmt;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyy");

    public ForgottenModel(Connection connection) throws SQLException {
        forBookingStmt = connection.prepareStatement("SELECT * FROM Glemt WHERE Booking=?;");
    }

    public Collection<Forgotten> itemsForBooking(Booking booking) throws SQLException {
        ArrayList<Forgotten> ret = new ArrayList<Forgotten>();

        forBookingStmt.setInt(1, booking.getID());

        ResultSet resultSet = forBookingStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Forgotten(
                    resultSet.getInt("ID"),
                    resultSet.getInt("Booking"),
                    resultSet.getString("Ting"),
                    resultSet.getBoolean("Levert"),
                    resultSet.getString("Kommentar"),
                    booking.getDate(),
                    booking.getName(),
                    booking.getEmail()));
        }

        return ret;
    }
}

package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Destroyed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class DestroyedModel {
    protected PreparedStatement forBookingStmt;

    public DestroyedModel(Connection connection) throws SQLException {
        forBookingStmt = connection.prepareStatement("SELECT * FROM Odelagt WHERE Booking=?;");
    }

    public Collection<Destroyed> itemsForBooking(Booking booking) throws SQLException {
        ArrayList<Destroyed> ret = new ArrayList<Destroyed>();

        forBookingStmt.setInt(1, booking.getID());

        ResultSet resultSet = forBookingStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Destroyed(
                            resultSet.getInt("ID"),
                            resultSet.getInt("Booking"),
                            resultSet.getString("Ting"),
                            resultSet.getBoolean("Fikset"))
            );
        }

        return ret;
    }
}

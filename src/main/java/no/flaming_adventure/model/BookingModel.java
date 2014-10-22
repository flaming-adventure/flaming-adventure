package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class BookingModel {
    public final SimpleDateFormat dateFormat;

    protected PreparedStatement stmt1;
    protected PreparedStatement forHutStmt;
    protected PreparedStatement forHutDateStmt;
    protected PreparedStatement insertStmt;

    public BookingModel(Connection connection, SimpleDateFormat dateFormat) throws SQLException {
        this.dateFormat = dateFormat;

        stmt1 = connection.prepareStatement("SELECT * FROM reservations;");
        forHutStmt = connection.prepareStatement("SELECT * FROM reservations WHERE hut_id=?;");
        forHutDateStmt = connection.prepareStatement("SELECT * FROM reservations WHERE hut_id=? AND date=?;");
        insertStmt = connection.prepareStatement("INSERT INTO reservations (hut_id, date, name, email, count, comment)" +
                "VALUES (?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
    }

    public Integer insert(Booking booking)
            throws SQLException {
        insertStmt.setInt(1, booking.getHutID());
        insertStmt.setDate(2, new java.sql.Date(booking.getDate().getTime()));
        insertStmt.setString(3, booking.getName());
        insertStmt.setString(4, booking.getEmail());
        insertStmt.setInt(5, booking.getCount());
        insertStmt.setString(6, booking.getComment());

        insertStmt.executeUpdate();

        ResultSet resultSet = insertStmt.getGeneratedKeys();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public HashMap<Integer, Booking> bookingMap() throws SQLException {
        HashMap<Integer, Booking> ret = new HashMap<>();

        ResultSet resultSet = stmt1.executeQuery();
        while (resultSet.next()) {
            Booking booking = Booking.fromResultSet(resultSet);
            ret.put(booking.getID(), booking);
        }

        return ret;
    }
}

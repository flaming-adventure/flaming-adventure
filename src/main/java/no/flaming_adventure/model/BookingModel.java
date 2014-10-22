package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Hut;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BookingModel {
    public final SimpleDateFormat dateFormat;

    protected PreparedStatement stmt1;
    protected PreparedStatement forHutStmt;
    protected PreparedStatement forHutDateStmt;
    protected PreparedStatement insertStmt;

    public BookingModel(Connection connection, SimpleDateFormat dateFormat) throws SQLException {
        this.dateFormat = dateFormat;

        stmt1 = connection.prepareStatement("SELECT * FROM Booking;");
        forHutStmt = connection.prepareStatement("SELECT * FROM Booking WHERE Koie=?;");
        forHutDateStmt = connection.prepareStatement("SELECT * FROM Booking WHERE Koie=? AND Dato=?;");
        insertStmt = connection.prepareStatement("INSERT INTO Booking (Koie, Dato, Navn, Epost, Antall, Kommentar)" +
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

    public ArrayList<Booking> bookingsForHut(Hut hut) throws SQLException {
        ArrayList<Booking> ret = new ArrayList<Booking>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(Booking.fromResultSet(resultSet));
        }

        return ret;
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

    public HashMap<Integer, Booking> bookingMapForHut(Hut hut) throws SQLException {
        HashMap<Integer, Booking> ret = new HashMap<>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            Booking booking = Booking.fromResultSet(resultSet);
            ret.put(booking.getID(), booking);
        }

        return ret;
    }

    public Integer occupancy(Integer hutID, Date date) throws SQLException {
        java.sql.Date sqlDate = new java.sql.Date(date.getTime());
        forHutDateStmt.setInt(1, hutID);
        forHutDateStmt.setDate(2, sqlDate);

        ResultSet resultSet = forHutDateStmt.executeQuery();

        Integer ret = 0;
        while (resultSet.next()) {
            ret += resultSet.getInt("Antall");
        }

        return ret;
    }
}

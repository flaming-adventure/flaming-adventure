package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Hut;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class BookingModel {
    protected final SimpleDateFormat dateFormat;

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

    public void insert(Integer hut, java.sql.Date date, String name, String email, Integer count, String comment)
            throws SQLException {
        insertStmt.setInt(1, hut);
        insertStmt.setDate(2, date);
        insertStmt.setString(3, name);
        insertStmt.setString(4, email);
        insertStmt.setInt(5, count);
        insertStmt.setString(6, comment);

        insertStmt.executeUpdate();
    }

    public ArrayList<Booking> bookingsForHut(Hut hut) throws SQLException {
        ArrayList<Booking> ret = new ArrayList<Booking>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(Booking.fromResultSet(resultSet, dateFormat));
        }

        return ret;
    }

    public HashMap<Integer, Booking> bookingMap() throws SQLException {
        HashMap<Integer, Booking> ret = new HashMap<>();

        ResultSet resultSet = stmt1.executeQuery();
        while (resultSet.next()) {
            Booking booking = Booking.fromResultSet(resultSet, dateFormat);
            ret.put(booking.getID(), booking);
        }

        return ret;
    }

    public HashMap<Integer, Booking> bookingMapForHut(Hut hut) throws SQLException {
        HashMap<Integer, Booking> ret = new HashMap<>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            Booking booking = Booking.fromResultSet(resultSet, dateFormat);
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

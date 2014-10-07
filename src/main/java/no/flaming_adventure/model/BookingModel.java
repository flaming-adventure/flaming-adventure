package no.flaming_adventure.model;

import no.flaming_adventure.shared.Booking;
import no.flaming_adventure.shared.Hut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BookingModel {
    protected final SimpleDateFormat dateFormat;

    protected PreparedStatement forHutStmt;
    protected PreparedStatement forHutDateStmt;

    public BookingModel(Connection connection, SimpleDateFormat dateFormat) throws SQLException {
        this.dateFormat = dateFormat;

        forHutStmt = connection.prepareStatement("SELECT * FROM Booking WHERE Koie=?;");
        forHutDateStmt = connection.prepareStatement("SELECT * FROM Booking WHERE Koie=? AND Dato=?;");
    }

    public ArrayList<Booking> bookingsForHut(Hut hut) throws SQLException {
        ArrayList<Booking> ret = new ArrayList<Booking>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Booking(
                    resultSet.getInt("ID"),
                    resultSet.getInt("Koie"),
                    dateFormat.format(resultSet.getDate("Dato")),
                    resultSet.getString("Navn"),
                    resultSet.getString("Epost"),
                    resultSet.getInt("Antall"),
                    resultSet.getString("Kommentar")));
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

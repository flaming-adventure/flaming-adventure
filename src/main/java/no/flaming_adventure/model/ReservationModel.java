package no.flaming_adventure.model;

import no.flaming_adventure.shared.Reservation;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;

public class ReservationModel {
    public final SimpleDateFormat dateFormat;

    protected PreparedStatement stmt1;
    protected PreparedStatement forHutStmt;
    protected PreparedStatement forHutDateStmt;
    protected PreparedStatement insertStmt;

    public ReservationModel(Connection connection, SimpleDateFormat dateFormat) throws SQLException {
        this.dateFormat = dateFormat;

        stmt1 = connection.prepareStatement("SELECT * FROM reservations;");
        forHutStmt = connection.prepareStatement("SELECT * FROM reservations WHERE hut_id=?;");
        forHutDateStmt = connection.prepareStatement("SELECT * FROM reservations WHERE hut_id=? AND date=?;");
        insertStmt = connection.prepareStatement("INSERT INTO reservations (hut_id, date, name, email, count, comment)" +
                "VALUES (?, ?, ?, ?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
    }

    public Integer insert(Reservation reservation)
            throws SQLException {
        insertStmt.setInt(1, reservation.getHutID());
        insertStmt.setDate(2, new java.sql.Date(reservation.getDate().getTime()));
        insertStmt.setString(3, reservation.getName());
        insertStmt.setString(4, reservation.getEmail());
        insertStmt.setInt(5, reservation.getCount());
        insertStmt.setString(6, reservation.getComment());

        insertStmt.executeUpdate();

        ResultSet resultSet = insertStmt.getGeneratedKeys();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public HashMap<Integer, Reservation> reservationMap() throws SQLException {
        HashMap<Integer, Reservation> ret = new HashMap<>();

        ResultSet resultSet = stmt1.executeQuery();
        while (resultSet.next()) {
            Reservation reservation = Reservation.fromResultSet(resultSet);
            ret.put(reservation.getID(), reservation);
        }

        return ret;
    }
}

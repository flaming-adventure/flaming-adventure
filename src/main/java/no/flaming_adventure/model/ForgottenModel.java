package no.flaming_adventure.model;

import no.flaming_adventure.shared.Forgotten;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ForgottenModel {
    protected PreparedStatement stmt1;
    protected PreparedStatement forBookingStmt;

    public ForgottenModel(Connection connection) throws SQLException {
        forBookingStmt = connection.prepareStatement("SELECT * FROM forgotten_items WHERE reservations_id=?;");
        stmt1 = connection.prepareStatement("SELECT * FROM forgotten_items;");
    }

    public ArrayList<Forgotten> forgotten() throws SQLException {
        ArrayList<Forgotten> ret = new ArrayList<>();

        ResultSet resultSet = stmt1.executeQuery();
        while (resultSet.next()) {
            ret.add(Forgotten.fromResultSet(resultSet));
        }

        return ret;
    }
}

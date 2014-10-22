package no.flaming_adventure.model;

import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Hut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class EquipmentModel {
    protected final SimpleDateFormat dateFormat;

    protected PreparedStatement stmt1;
    protected PreparedStatement forHutStmt;

    public EquipmentModel(Connection connection, SimpleDateFormat dateFormat) throws SQLException {
        this.dateFormat = dateFormat;

        stmt1 = connection.prepareStatement("SELECT * FROM equipment;");
        forHutStmt = connection.prepareStatement("SELECT * FROM equipment WHERE hut_id=?;");
    }

    public ArrayList<Equipment> items() throws SQLException {
        ArrayList<Equipment> ret = new ArrayList<>();

        ResultSet resultSet = stmt1.executeQuery();
        while (resultSet.next()) {
            ret.add(Equipment.fromResultSet(resultSet, dateFormat));
        }

        return ret;
    }

    public ArrayList<Equipment> itemsForHut(Hut hut) throws SQLException {
        ArrayList<Equipment> ret = new ArrayList<>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(Equipment.fromResultSet(resultSet, dateFormat));
        }

        return ret;
    }
}

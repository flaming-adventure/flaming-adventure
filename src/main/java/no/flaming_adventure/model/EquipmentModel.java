package no.flaming_adventure.model;

import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Hut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class EquipmentModel {
    protected PreparedStatement forHutStmt;

    public EquipmentModel(Connection connection) throws SQLException {
        forHutStmt = connection.prepareStatement("SELECT * FROM Ekstrautstyr WHERE Koie=?;");
    }

    public Collection<Equipment> itemsForHut(Hut hut) throws SQLException {
        ArrayList<Equipment> ret = new ArrayList<Equipment>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Equipment(
                            resultSet.getString("Navn"),
                            resultSet.getDate("Innkjopt").toString(),
                            resultSet.getInt("Antall"))
            );
        }

        return ret;
    }
}

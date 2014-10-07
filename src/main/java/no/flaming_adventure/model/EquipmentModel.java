package no.flaming_adventure.model;

import no.flaming_adventure.shared.Equipment;
import no.flaming_adventure.shared.Hut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

public class EquipmentModel {
    protected final SimpleDateFormat dateFormat;

    protected PreparedStatement forHutStmt;

    public EquipmentModel(Connection connection, SimpleDateFormat dateFormat) throws SQLException {
        this.dateFormat = dateFormat;
        forHutStmt = connection.prepareStatement("SELECT * FROM Ekstrautstyr WHERE Koie=?;");
    }

    public Collection<Equipment> itemsForHut(Hut hut) throws SQLException {
        ArrayList<Equipment> ret = new ArrayList<Equipment>();

        forHutStmt.setInt(1, hut.getID());

        ResultSet resultSet = forHutStmt.executeQuery();
        while (resultSet.next()) {
            ret.add(new Equipment(
                            resultSet.getInt("ID"),
                            resultSet.getInt("Koie"),
                            resultSet.getString("Navn"),
                            dateFormat.format(resultSet.getDate("Innkjopt")),
                            resultSet.getInt("Antall"))
            );
        }

        return ret;
    }
}

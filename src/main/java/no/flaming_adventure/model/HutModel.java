package no.flaming_adventure.model;

import no.flaming_adventure.shared.Hut;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

public class HutModel {
    protected PreparedStatement stmt1;

    public HutModel(Connection connection) throws SQLException {
        stmt1 = connection.prepareStatement("SELECT * FROM Koie;");
    }

    public Collection<Hut> huts() throws SQLException {
        ArrayList<Hut> ret = new ArrayList<Hut>();

        ResultSet resultSet = stmt1.executeQuery();
        while (resultSet.next()) {
            ret.add(new Hut(resultSet.getInt("ID"), resultSet.getString("Navn"), resultSet.getInt("Kapasitet"), resultSet.getInt("Ved")));
        }

        return ret;
    }
}

package no.flaming_adventure.db_admin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

import no.flaming_adventure.shared.database.ConnectionHandler;

public class DBAdmin {
    public static void main(String[] args) {
        try {
            CommandLine line = Cli.parse(args);
            if (line.hasOption("reset")) {
                DBAdmin.reset();
            }
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Reset the database at the given path.
     * @throws SQLException, IOException
     */
    public static void reset() throws SQLException, IOException {
        InputStream stream = DBAdmin.class.getClassLoader().getResourceAsStream("db_reset.sql");
        /* FIXME: Hack to allow execution of all statements in the file. */
        String[] sql = IOUtils.toString(stream).split(";");

        Connection connection = ConnectionHandler.getInstance().getConnection();
        Statement statement = connection.createStatement();

        for (String s: sql) {
            /* FIXME: Hack to avoid executing whitespace. */
            if (s == sql[sql.length -1]) { break; }
            statement.addBatch(s);
        }
        statement.executeBatch();
        statement.close();
    }
}

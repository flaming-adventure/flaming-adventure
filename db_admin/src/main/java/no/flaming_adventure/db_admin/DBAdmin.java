package no.flaming_adventure.db_admin;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.io.IOUtils;

public class DBAdmin {
    public static void main(String[] args) {
        try {
            Class.forName("org.sqlite.JDBC");
            CommandLine line = Cli.parse(args);
            if (line.hasOption("reset")) {
                DBAdmin.reset("./test.db");
            }
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
    }

    /**
     * Reset the database at the given path.
     *
     * @param db_path path to the database file to reset.
     * @throws SQLException
     */
    public static void reset(String db_path) throws SQLException, IOException {
        InputStream stream = DBAdmin.class.getClassLoader().getResourceAsStream("db_reset.sql");
        /* FIXME: Hack to allow execution of all statements in the file. */
        String[] sql = IOUtils.toString(stream).split(";");

        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + db_path);
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

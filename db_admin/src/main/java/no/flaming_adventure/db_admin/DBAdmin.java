package no.flaming_adventure.db_admin;

import java.sql.*;

import org.apache.commons.cli.CommandLine;

public class DBAdmin {
    public static void main(String[] args) {
        /* With a lack of something better to do, lets test if sqlite works. */
        Connection conn = null;
        try {
            CommandLine line = Cli.parse(args);

            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite::memory:");
            Statement stmt = conn.createStatement();
            String sql = "CREATE TABLE t1 (ID INT); CREATE TABLE t2 (ID INT);";
            stmt.execute(sql);
            stmt.close();
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
        System.out.println("Tables created successfully.");
    }
}

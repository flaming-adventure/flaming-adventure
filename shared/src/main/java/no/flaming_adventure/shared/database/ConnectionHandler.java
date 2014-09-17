package no.flaming_adventure.shared.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton providing access to the database connection.
 */
public class ConnectionHandler {
    public static final String DB_DRIVER = "org.sqlite.JDBC";
    public static final String DB_URI = "jdbc:sqlite::memory:";

    private static ConnectionHandler instance = null;
    private Connection connection = null;

    /**
     * Get the class instance, creating it if necessary.
     */
    public static ConnectionHandler getInstance() {
        if (instance == null) {
            instance = new ConnectionHandler();
        }
        return instance;
    }

    /**
     * Attempt to get the database connection, creating it if necessary.
     *
     * This function prints the exception to `stderr` and exits if an
     * exception is raised. This behaviour can and will change in the
     * future.
     */
    public Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName(DB_DRIVER);
                connection = DriverManager.getConnection(DB_URI);
            } catch (ClassNotFoundException e) {
                /* TODO: replace with logging, cleanup, and exit. */
                System.err.println(e);
                System.exit(1);
            } catch (SQLException e) {
                /* TODO: replace with logging, cleanup, and exit. */
                System.err.println(e);
                System.exit(1);
            }
        }
        return connection;
    }

    protected ConnectionHandler() { }
}


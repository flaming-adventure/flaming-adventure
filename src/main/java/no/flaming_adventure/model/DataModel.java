package no.flaming_adventure.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The data model for the application.
 *
 * <p> An object of this class wraps around an SQL connection and is responsible for all communications with the
 * database during a single user session.
 *
 * <h3>Database layout</h3>
 *
 * <p> For the most up to date database layout see <code>src/dist/schema.ddl</code> in the source directory or
 * <code>schema.ddl</code> in the distributed files. This section deals mostly with architecture decisions.
 *
 * <p> <em>Section removed during refactor.</em>
 *
 * <h3>Database usage strategy</h3>
 *
 * <p> Currently the data model simply requests all data from the database upon construction and creates an
 * observable list for each table. While this is not currently an issue because of the size of the data set it will
 * most certainly become one when more data is added to the database. Therefore the following tasks should be
 * completed as soon as possible.
 *
 * <ol>
 *     <li>TODO #38 (high priority): create and implement a specification for the data model API.
 *     <li>TODO #39 (high priority): create a consistent database usage strategy.
 *     <li>TODO #41 (enhancement): deploy database usage strategy.
 * </ol>
 *
 * <h4>SQL queries</h4>
 *
 * <p> All SQL queries in the entire project should be located in this class. If for any reason this should change it
 * is imperative that both this comment is modified and that the commit message presents a compelling reason for the
 * change.
 */
public class DataModel {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    private static final String SQL_INSERT_RESERVATION =
            "INSERT INTO reservations (hut_id, date, name, email, count, comment) VALUES (?, ?, ?, ?, ?, ?);";

    private static final Logger LOGGER = Logger.getLogger(DataModel.class.getName());;

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final Statement statement;

    private final PreparedStatement occupancyStmt;
    private final PreparedStatement reservationInsertStmt;

    private final Map<Integer, Hut> hutMap = new HashMap<>();

    /************************************************************************
     *
     * Constructors
     *
     ************************************************************************/

    /**
     * Create a data model from the given SQL connection.
     *
     * @param connection        Connection to the SQL database.
     * @throws java.sql.SQLException     Any conceivable SQL exception.
     */
    public DataModel(Connection connection) throws SQLException {
        LOGGER.log(Level.FINE, "Initializing data model.");

        statement = connection.createStatement();
        occupancyStmt = connection.prepareStatement("SELECT SUM(reservations.count) FROM reservations " +
                        "WHERE reservations.hut_id = ? AND reservations.date = ?;");

        reservationInsertStmt   = connection.prepareStatement(SQL_INSERT_RESERVATION, Statement.RETURN_GENERATED_KEYS);

        LOGGER.fine("Data model successfully initialized.");
    }

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public ObservableList<Hut> getHuts() throws SQLException {
        ObservableList<Hut> huts = FXCollections.observableArrayList();
        String query = "SELECT * FROM huts;";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            Hut hut = hutFromResultSet(resultSet);
            huts.add(hut);
            hutMap.put(hut.getId(), hut);
        }
        return huts;
    }

    public ObservableList<Reservation> getReservations() throws SQLException {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        String query = "SELECT * FROM reservations;";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            reservations.add(reservationFromResultSet(resultSet));
        }
        return reservations;
    }

    public ObservableList<ForgottenItem> getForgottenItems() throws SQLException {
        ObservableList<ForgottenItem> forgottenItems = FXCollections.observableArrayList();
        String query = "SELECT * FROM forgotten_items;";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            forgottenItems.add(forgottenItemFromResultSet(resultSet));
        }
        return forgottenItems;
    }

    public ObservableList<BrokenItem> getBrokenItems() throws SQLException {
        ObservableList<BrokenItem> brokenItems = FXCollections.observableArrayList();
        String query = "SELECT * FROM broken_items;";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            brokenItems.add(brokenItemFromResultSet(resultSet));
        }
        return brokenItems;
    }

    public ObservableList<Equipment> getEquipmentList() throws SQLException {
        ObservableList<Equipment> equipmentList = FXCollections.observableArrayList();
        String query = "SELECT * FROM equipment;";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            equipmentList.add(equipmentFromResultSet(resultSet));
        }
        return equipmentList;
    }

    public Integer occupancy(Hut hut, LocalDate date) throws SQLException {
        occupancyStmt.setInt(1, hut.getId());
        occupancyStmt.setDate(2, Date.valueOf(date));

        ResultSet resultSet = occupancyStmt.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    /**
     * Insert a reservation object into the database, setting the ID of the object to the database ID used.
     *
     * @param reservation the reservation object to insert into the database.
     * @throws SQLException any conceivable SQL exception.
     */
    public void insertReservation(Reservation reservation) throws SQLException {
        LOGGER.log(Level.INFO, "Adding reservation to database.");

        reservationInsertStmt.setInt(1, reservation.getHut().getId());
        reservationInsertStmt.setDate(2, Date.valueOf(reservation.getDate()));
        reservationInsertStmt.setString(3, reservation.getName());
        reservationInsertStmt.setString(4, reservation.getEmail());
        reservationInsertStmt.setInt(5, reservation.getCount());
        reservationInsertStmt.setString(6, reservation.getComment());

        reservationInsertStmt.executeUpdate();

        ResultSet resultSet = reservationInsertStmt.getGeneratedKeys();
        resultSet.next();
        reservation.setId(resultSet.getInt(1));
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    private Hut hutFromResultSet(ResultSet resultSet) throws SQLException {
        return new Hut(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getInt("capacity"),
                resultSet.getInt("firewood")
        );
    }

    private Reservation reservationFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutMap.get(resultSet.getInt("hut_id"));
        return new Reservation(
                resultSet.getInt("id"),
                hut,
                resultSet.getDate("date").toLocalDate(),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getInt("count"),
                resultSet.getString("comment")
        );
    }

    private ForgottenItem forgottenItemFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutMap.get(resultSet.getInt("hut_id"));
        return new ForgottenItem(
                resultSet.getInt("id"),
                hut,
                resultSet.getString("item"),
                resultSet.getString("name"),
                resultSet.getString("contact"),
                resultSet.getDate("date").toLocalDate(),
                resultSet.getBoolean("delivered"),
                resultSet.getString("comment")
        );
    }

    private BrokenItem brokenItemFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutMap.get(resultSet.getInt("hut_id"));
        return new BrokenItem(
                resultSet.getInt("id"),
                hut,
                resultSet.getString("item"),
                resultSet.getDate("date").toLocalDate(),
                resultSet.getBoolean("fixed"),
                resultSet.getString("comment")
        );
    }

    private Equipment equipmentFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutMap.get(resultSet.getInt("hut_id"));
        return new Equipment(
                resultSet.getInt("id"),
                hut,
                resultSet.getString("name"),
                resultSet.getDate("purchase_date").toLocalDate(),
                resultSet.getInt("count")
        );
    }
}

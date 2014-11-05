package no.flaming_adventure.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.flaming_adventure.SQLFunction;
import no.flaming_adventure.shared.*;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Logger;

/* Todo
 * ----
 *
 * - Complete logging.
 * - Cache weak references to filtered lists.
 * - Add missing functionality.
 */

/**
 * The data model for the application.
 *
 * An object of this class wraps around an SQL connection and is responsible for all communications with the database
 * during a single user session.
 */
public class DataModel {
    /* SQL queries
     * ===========
     *
     * All SQL queries in the Java sources are located in the following section. If for any reason this should change
     * this comment should be modified to reflect the current state, and the commit message should present a compelling
     * reason for the change.
     *
     * In addition this comment will be used as a temporary location for documentation and discussion relevant to
     * the database itself.
     *
     * Todo
     * ----
     *
     * - Move the dependencies on reservations to dependencies on huts and add fields where it makes sense.
     *   For example for forgotten items it makes sense for there to be items that have been forgotten at or near
     *   a hut without a reservation ever having been made at that date.
     * - Make a consistent choice on the destroyed/out of order naming dilemma.
     * - Remove norwegian database tables.
     * - Create a database test data generator.
     */
    private static final String sqlHutList          = "SELECT * FROM huts;";
    private static final String sqlReservationList  = "SELECT * FROM reservations;";
    private static final String sqlEquipmentList    = "SELECT * FROM equipment;";
    private static final String sqlForgottenList    = "SELECT * FROM forgotten_items;";
    private static final String sqlDestroyedList    = "SELECT * FROM out_of_order;";

    private static final String sqlInsertReservation    =
            "INSERT INTO reservations (hut_id, date, name, email, count, comment) VALUES (?, ?, ?, ?, ?, ?);";

    private static final String sqlInsertDestroyed      =
            "INSERT INTO out_of_order (reservation_id, item, fixed) VALUES (?, ?, ?);";

    private static final String sqlInsertForgotten      =
            "INSERT INTO forgotten_items (reservation_id, item, delivered, comment) VALUES (?, ?, ?, ?);";

    private final Logger logger;

    private final PreparedStatement reservationInsertStmt;
    private final PreparedStatement destroyedInsertStmt;
    private final PreparedStatement forgottenInsertStmt;

    /* We maintain all data from all tables in the database as ObservableList<> objects.
     *
     * This allows very simple/fast code outside of the DataModel. It does however cause overhead relative to the size
     * of the database, first in the retrieval of the data, subsequently in memory usage. As long as the database
     * remains small this should be fine, but if this code ever goes into an actual production scenario this situation
     * should be changed.
     */
    private final ObservableList<Hut>           hutList;
    private final ObservableList<Reservation>   reservationList;
    private final ObservableList<Equipment>     equipmentList;
    private final ObservableList<Forgotten>     forgottenList;
    private final ObservableList<Destroyed>     destroyedList;

    /**
     * Wrap the given connection in a data model object.
     *
     * @param logger        Logger object to be used for all logging by the data model
     * @param connection    Connection to the SQL database.
     * @throws SQLException
     */
    public DataModel(Logger logger, Connection connection) throws SQLException {
        this.logger = logger;

        logger.fine("Initializing data model...");
        logger.finest("Preparing data model statements...");

        PreparedStatement hutStmt           = connection.prepareStatement(sqlHutList);
        PreparedStatement reservationStmt   = connection.prepareStatement(sqlReservationList);
        PreparedStatement equipmentStmt     = connection.prepareStatement(sqlEquipmentList);
        PreparedStatement forgottenStmt     = connection.prepareStatement(sqlForgottenList);
        PreparedStatement destroyedStmt     = connection.prepareStatement(sqlDestroyedList);

        reservationInsertStmt   = connection.prepareStatement(sqlInsertReservation, Statement.RETURN_GENERATED_KEYS);
        destroyedInsertStmt     = connection.prepareStatement(sqlInsertDestroyed,   Statement.RETURN_GENERATED_KEYS);
        forgottenInsertStmt     = connection.prepareStatement(sqlInsertForgotten,   Statement.RETURN_GENERATED_KEYS);

        hutList = forceList(hutStmt, resultSet -> new Hut(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getInt("capacity"),
                resultSet.getInt("firewood")
        ));

        reservationList = forceList(reservationStmt, resultSet -> {
            Integer hutID = resultSet.getInt("hut_id");
            // TODO: Don't unwrap optional.
            Hut hut = getHutFromID(hutID).get();
            return new Reservation(
                    hut,
                    resultSet.getInt("id"),
                    hutID,
                    resultSet.getDate("date"),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getInt("count"),
                    resultSet.getString("comment")
            );
        });

        equipmentList = forceList(equipmentStmt, resultSet -> {
            Integer hutID = resultSet.getInt("hut_id");
            // TODO: Don't unwrap optional.
            Hut hut = getHutFromID(hutID).get();
            return new Equipment(
                    hut,
                    resultSet.getInt("id"),
                    hutID,
                    resultSet.getString("name"),
                    resultSet.getDate("purchase_date"),
                    resultSet.getInt("count")
            );
        });

        forgottenList = forceList(forgottenStmt, resultSet -> {
            Integer reservationID = resultSet.getInt("reservation_id");
            // TODO: Don't unwrap optional.
            Reservation reservation = getReservationFromID(reservationID).get();
            return new Forgotten(
                    reservation,
                    resultSet.getInt("id"),
                    reservationID,
                    resultSet.getString("item"),
                    resultSet.getBoolean("delivered"),
                    resultSet.getString("comment")
            );
        });

        destroyedList   = forceList(destroyedStmt, resultSet -> {
            Integer reservationID = resultSet.getInt("reservation_id");
            // TODO: Don't unwrap optional.
            Reservation reservation = getReservationFromID(reservationID).get();
            return new Destroyed(
                    reservation,
                    resultSet.getInt("id"),
                    reservationID,
                    resultSet.getString("item"),
                    resultSet.getBoolean("fixed")
            );
        });

        logger.fine("Data model successfully initialized.");
    }

    /**
     * Return the list of huts.
     *
     * @return
     */
    public ObservableList<Hut> getHutList() {
        return hutList;
    }

    /**
     * Return the hut object corresponding to the given database ID if it exists.
     *
     * TODO: Change access specifier to private and deprecate.
     *
     * @param ID
     * @return
     */
    public Optional<Hut> getHutFromID(Integer ID) {
        // TODO: Improve search algorithm.
        for (Hut hut : getHutList()) {
            if (hut.getID() == ID) { return Optional.of(hut); }
        }
        return Optional.empty();
    }

    /**
     * Return the list of reservations.
     *
     * @return
     */
    public ObservableList<Reservation> getReservationList() {
        return reservationList;
    }

    /**
     * Return the list of reservations for the given hut.
     *
     * @param hut
     * @return
     */
    public ObservableList<Reservation> getReservationListForHut(Hut hut) {
        return getReservationList().filtered(reservation -> reservation.getHutID() == hut.getID());
    }

    /**
     * Return the reservation object corresponding to the given database ID if it exists.
     *
     * TODO: Change access specifier to private and deprecate.
     *
     * @param ID
     * @return
     */
    public Optional<Reservation> getReservationFromID(Integer ID) {
        for (Reservation reservation : getReservationList()) {
            if (reservation.getID() == ID) { return Optional.of(reservation); }
        }
        return Optional.empty();
    }

    /**
     * Insert a reservation object into the database, setting the ID of the object to the database ID used.
     *
     * @param reservation
     * @throws SQLException
     */
    public void insertReservation(Reservation reservation) throws SQLException {
        // TODO: Validate reservation.

        logger.info("Adding reservation to database...");

        reservationInsertStmt.setInt(1, reservation.getHutID());
        // TODO: Date handling.
        reservationInsertStmt.setDate(2, new java.sql.Date(reservation.getDate().getTime()));
        reservationInsertStmt.setString(3, reservation.getName());
        reservationInsertStmt.setString(4, reservation.getEmail());
        reservationInsertStmt.setInt(5, reservation.getCount());
        reservationInsertStmt.setString(6, reservation.getComment());

        reservationInsertStmt.executeUpdate();

        ResultSet resultSet = reservationInsertStmt.getGeneratedKeys();

        if (resultSet.next()) {
            logger.fine("Database returned primary key for reservation.");

            reservation.setID(resultSet.getInt(1));
        } else {
            logger.warning("Database failed to return primary key for reservation.");
            // TODO: Handle error.
        }

        logger.info("Adding new reservation to reservation list.");

        reservationList.add(reservation);
    }

    /**
     * Return the list of equipment.
     *
     * @return
     */
    public ObservableList<Equipment> getEquipmentList() {
        return equipmentList;
    }

    /**
     * Return the list of equipment for the given hut.
     *
     * @param hut
     * @return
     */
    public ObservableList<Equipment> getEquipmentListForHut(Hut hut) {
        return getEquipmentList().filtered(item -> item.getHutID() == hut.getID());
    }

    /**
     * Return the list of forgotten items.
     *
     * @return
     */
    public ObservableList<Forgotten> getForgottenList() {
        return forgottenList;
    }

    /**
     * Insert a forgotten item into the database, setting the ID of the object to the database ID used.
     *
     * @param forgotten
     * @throws SQLException
     */
    public void insertForgotten(Forgotten forgotten) throws SQLException {
        // TODO: Validate forgotten.

        logger.info("Adding forgotten item to database...");

        forgottenInsertStmt.setInt(1, forgotten.getReservationID());
        forgottenInsertStmt.setString(2, forgotten.getItem());
        forgottenInsertStmt.setBoolean(3, forgotten.getDelivered());
        forgottenInsertStmt.setString(4, forgotten.getComment());

        forgottenInsertStmt.executeUpdate();

        ResultSet resultSet = forgottenInsertStmt.getGeneratedKeys();

        if (resultSet.next()) {
            logger.fine("Database returned primary key for forgotten item.");

            forgotten.setID(resultSet.getInt(1));
        } else {
            logger.warning("Database failed to return primary key for forgotten item.");
        }

        logger.info("Adding new forgotten item to list.");
        getForgottenList().add(forgotten);
    }

    /**
     * Return the list of destroyed (or out of order) items.
     *
     * @return
     */
    public ObservableList<Destroyed> getDestroyedList() {
        return destroyedList;
    }

    /**
     * Insert a destroyed item into the database, setting the ID of the object to the database ID used.
     *
     * @param destroyed
     * @throws SQLException
     */
    public void insertDestroyed(Destroyed destroyed) throws SQLException {
        // TODO: Validate destroyed.

        logger.info("Adding destroyed item to database...");

        destroyedInsertStmt.setInt(1, destroyed.getReservationID());
        destroyedInsertStmt.setString(2, destroyed.getItem());
        destroyedInsertStmt.setBoolean(3, destroyed.getFixed());

        destroyedInsertStmt.executeUpdate();

        ResultSet resultSet = destroyedInsertStmt.getGeneratedKeys();

        if (resultSet.next()) {
            logger.fine("Database returned primary key for destroyed item.");

            destroyed.setID(resultSet.getInt(1));
        } else {
            logger.warning("Database failed to return primary key for destroyed item.");
        }

        // TODO: Move logging of list additions to conditional listeners on the lists themselves.
        logger.info("Adding new destroyed item to list.");

        destroyedList.add(destroyed);
    }

    /**
     * Retrieve a list of records from the database and create an ObservableList<> of objects.
     *
     * TODO: deprecate.
     *
     * @param stmt  Prepared statement to retrieve the records from the database.
     * @param fn    A function taking a resultSet and returning a list element.
     * @param <E>   The type of elements in the final list.
     * @return
     * @throws SQLException
     */
    private static <E> ObservableList<E> forceList(PreparedStatement stmt, SQLFunction<ResultSet, E> fn)
            throws SQLException {
        ObservableList<E> list = FXCollections.observableArrayList();

        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            list.add(fn.apply(resultSet));
        }

        return list;
    }
}

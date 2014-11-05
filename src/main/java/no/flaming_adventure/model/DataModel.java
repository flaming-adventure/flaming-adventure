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

    /**
     * Create a hut object from a resultSet containing the required fields (see source).
     */
    private final SQLFunction<ResultSet, Hut> hutFromResultSet = resultSet -> new Hut(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getInt("capacity"),
            resultSet.getInt("firewood")
    );

    /**
     * Create a reservation object from a resultSet containing the required fields (see source).
     */
    public final SQLFunction<ResultSet, Reservation> reservationFromResultSet = resultSet -> {
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
    };

    /**
     * Create an equipment item object from a resultSet containing the required fields (see source).
     */
    private final SQLFunction<ResultSet, Equipment> equipmentFromResultSet = resultSet -> {
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
    };

    /**
     * Create a forgotten item object from a resultSet containing the required fields (see source).
     */
    private final SQLFunction<ResultSet, Forgotten> forgottenFromResultSet = resultSet -> {
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
    };

    /**
     * Create a destroyed item object from a resultSet containing the required fields (see source).
     */
    private final SQLFunction<ResultSet, Destroyed> destroyedFromResultSet = resultSet -> {
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
    };

    /* End of SQL query section. */

    private final Logger logger;

    private final PreparedStatement hutStmt;
    private final PreparedStatement reservationStmt;
    private final PreparedStatement equipmentStmt;
    private final PreparedStatement forgottenStmt;
    private final PreparedStatement destroyedStmt;

    private final PreparedStatement reservationInsertStmt;
    private final PreparedStatement destroyedInsertStmt;
    private final PreparedStatement forgottenInsertStmt;

    /* We maintain all data from all tables in the database as ObservableList<> objects.
     *
     * This allows very simple/fast code outside of the DataModel. It does however cause overhead relative to the size
     * of the database, first in the retrieval of the data, subsequently in memory usage. As long as the database
     * remains small this should be fine, but if this code ever goes into an actual production scenario this situation
     * should be changed.
     *
     * Note that the lists are constructed when they are first requested. While this form of construction is nice
     * in some theoretical sense it's not really relevant to the application at this point and it does create issues
     * with users having to deal with SQLExceptions everywhere. (TODO: initialize lists on DataModel construction.)
     */
    private ObservableList<Hut>         hutList;
    private ObservableList<Reservation> reservationList;
    private ObservableList<Equipment>   equipmentList;
    private ObservableList<Forgotten>   forgottenList;
    private ObservableList<Destroyed>   destroyedList;

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

        hutStmt         = connection.prepareStatement(sqlHutList);
        reservationStmt = connection.prepareStatement(sqlReservationList);
        equipmentStmt   = connection.prepareStatement(sqlEquipmentList);
        forgottenStmt   = connection.prepareStatement(sqlForgottenList);
        destroyedStmt   = connection.prepareStatement(sqlDestroyedList);

        reservationInsertStmt   = connection.prepareStatement(sqlInsertReservation, Statement.RETURN_GENERATED_KEYS);
        destroyedInsertStmt     = connection.prepareStatement(sqlInsertDestroyed,   Statement.RETURN_GENERATED_KEYS);
        forgottenInsertStmt     = connection.prepareStatement(sqlInsertForgotten,   Statement.RETURN_GENERATED_KEYS);

        logger.fine("Data model successfully initialized.");
    }

    /**
     * Return the list of huts.
     *
     * Note: this function retrieves the list from the database when first called.
     *
     * @return
     * @throws SQLException
     */
    public ObservableList<Hut> getHutList() throws SQLException {
        if (hutList == null) {
            logger.info("Querying SQL server for hut list...");
            hutList = forceList(hutStmt, hutFromResultSet);
        }
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
        try {
            for (Hut hut : getHutList()) {
                if (hut.getID() == ID) { return Optional.of(hut); }
            }
        } catch (SQLException e) { }
        return Optional.empty();
    }

    /**
     * Return the list of reservations.
     *
     * Note: this function retrieves the list from the database when first called.
     *
     * @return
     * @throws SQLException
     */
    public ObservableList<Reservation> getReservationList() throws SQLException {
        if (reservationList == null) {
            logger.info("Querying SQL server for reservation list...");
            reservationList = forceList(reservationStmt, reservationFromResultSet);
        }
        return reservationList;
    }

    /**
     * Return the list of reservations for the given hut.
     *
     * Note: this function calls getReservationList() and may retrieve every reservation from the database.
     *
     * @param hut
     * @return
     * @throws SQLException
     */
    public ObservableList<Reservation> getReservationListForHut(Hut hut) throws SQLException {
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
        try {
            for (Reservation reservation : getReservationList()) {
                if (reservation.getID() == ID) { return Optional.of(reservation); }
            }
        } catch (SQLException e) { }
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

        // XXX: Ensure that the reservation list is forced.
        getReservationList();

        logger.info("Adding new reservation to reservation list.");

        reservationList.add(reservation);
    }

    /**
     * Return the list of equipment.
     *
     * Note: this function retrieves the list from the database when first called.
     *
     * @return
     * @throws SQLException
     */
    public ObservableList<Equipment> getEquipmentList() throws SQLException {
        if (equipmentList == null) {
            equipmentList = forceList(equipmentStmt, equipmentFromResultSet);
        }
        return equipmentList;
    }

    /**
     * Return the list of equipment for the given hut.
     *
     * Note: this function calls getEquipmentList() and may retrieve every reservation from the database.
     *
     * @param hut
     * @return
     * @throws SQLException
     */
    public ObservableList<Equipment> getEquipmentListForHut(Hut hut) throws SQLException {
        return getEquipmentList().filtered(item -> item.getHutID() == hut.getID());
    }

    /**
     * Return the list of forgotten items.
     *
     * Note: this function retrieves the list from the database when first called.
     *
     * @return
     * @throws SQLException
     */
    public ObservableList<Forgotten> getForgottenList() throws SQLException {
        if (forgottenList == null) {
            forgottenList = forceList(forgottenStmt, forgottenFromResultSet);
        }
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
     * Note: this function retrieves the list from the database when first called.
     *
     * @return
     * @throws SQLException
     */
    public ObservableList<Destroyed> getDestroyedList() throws SQLException {
        if (destroyedList == null) {
            destroyedList = forceList(destroyedStmt, destroyedFromResultSet);
        }
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

        // XXX: Ensure that the destroyed list is forced.
        getDestroyedList();

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

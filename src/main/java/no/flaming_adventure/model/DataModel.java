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

    private final SQLFunction<ResultSet, Hut> hutFromResultSet = resultSet -> new Hut(
            resultSet.getInt("id"),
            resultSet.getString("name"),
            resultSet.getInt("capacity"),
            resultSet.getInt("firewood")
    );

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

    private ObservableList<Hut>         hutList;
    private ObservableList<Reservation> reservationList;
    private ObservableList<Equipment>   equipmentList;
    private ObservableList<Forgotten>   forgottenList;
    private ObservableList<Destroyed>   destroyedList;

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
    }

    public ObservableList<Hut> getHutList() throws SQLException {
        if (hutList == null) {
            logger.info("Querying SQL server for hut list...");
            hutList = forceList(hutStmt, hutFromResultSet);
        }
        return hutList;
    }

    public Optional<Hut> getHutFromID(Integer ID) {
        // TODO: Improve search algorithm.
        try {
            for (Hut hut : getHutList()) {
                if (hut.getID() == ID) { return Optional.of(hut); }
            }
        } catch (SQLException e) { }
        return Optional.empty();
    }

    public ObservableList<Reservation> getReservationList() throws SQLException {
        if (reservationList == null) {
            logger.info("Querying SQL server for reservation list...");
            reservationList = forceList(reservationStmt, reservationFromResultSet);
        }
        return reservationList;
    }

    public ObservableList<Reservation> getReservationListForHut(Hut hut) throws SQLException {
        return getReservationList().filtered(reservation -> reservation.getHutID() == hut.getID());
    }

    public Optional<Reservation> getReservationFromID(Integer ID) {
        try {
            for (Reservation reservation : getReservationList()) {
                if (reservation.getID() == ID) { return Optional.of(reservation); }
            }
        } catch (SQLException e) { }
        return Optional.empty();
    }

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

    public ObservableList<Equipment> getEquipmentList() throws SQLException {
        if (equipmentList == null) {
            equipmentList = forceList(equipmentStmt, equipmentFromResultSet);
        }
        return equipmentList;
    }

    public ObservableList<Equipment> getEquipmentListForHut(Hut hut) throws SQLException {
        return getEquipmentList().filtered(item -> item.getHutID() == hut.getID());
    }

    public ObservableList<Forgotten> getForgottenList() throws SQLException {
        if (forgottenList == null) {
            forgottenList = forceList(forgottenStmt, forgottenFromResultSet);
        }
        return forgottenList;
    }

    public ObservableList<Destroyed> getDestroyedList() throws SQLException {
        if (destroyedList == null) {
            destroyedList = forceList(destroyedStmt, destroyedFromResultSet);
        }
        return destroyedList;
    }

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

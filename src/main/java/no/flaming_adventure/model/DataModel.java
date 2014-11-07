package no.flaming_adventure.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.flaming_adventure.SQLFunction;
import no.flaming_adventure.shared.*;

import java.sql.*;
import java.util.Optional;
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
 * <p> The following section is copied from Ivar Nyland's system architecture description, updated to reflect the
 * current state of the database and translated by me (Erik Nyhus) to English.
 *
 * <blockquote>
 * <p> Because of time constraints, the database will not be highly normalized. We can for example look at the use of
 * the name and e-mail fields in the reservation table, where we would have preferred to build a customer register
 * with its own customer table. We regard this as good enough given the scope of the project.
 *
 * <p> In the <em>reservations</em> table we store all information needed to make a reservation for a hut. The
 * primary key <em>id</em> is an auto incrementing integer that is unique for each reservation.
 * <em>hut_id</em> keeps track of which hut the reservation is for. In addition we keep the date of the
 * reservation (<em>date</em>), the name and e-mail of the person responsible for the order (<em>name</em>
 * and <em>email</em>), the number of people (<em>count</em>) and a comment with any additional information
 * (<em>comment</em>).
 *
 * <p> The <em>huts</em> table keeps track of the different huts available for reservations. Each hut has a
 * <em>name</em>, a unique <em>id</em>, a <em>capacity</em> and a number of bags of <em>firewood</em>.
 *
 * <p> <em>equipment</em> is a table records for equipment available at the different huts. Each item has the
 * obligatory <em>id</em>, <em>hut_id</em> and <em>name</em> fields. Additionally we keep the data at which a set of
 * items were purchased (<em>purchase_date</em>) and the number of items purchased at that date <em>count</em>.
 *
 * <p> Tables for forgotten items (<em>forgotten_items</em>) and destroyed/out of order items (<em>out_of_order</em>)
 * also exist. These are relatively similar, both containing the obligatory <em>id</em> as well as being linked to a
 * reservation (<em>reservation_id</em>) and having a short name (<em>item</em>). The destroyed/out of order table
 * contains a <em>fixed</em> field of boolean type and the forgotten item table has a <em>delivered</em> field with
 * much the same function. Finally the forgotten item table has a <em>comment</em> field for additional information.
 * </blockquote>
 *
 * <ul>
 *     <li>TODO #36 (low priority): make a consistent decision on the naming of the destroyed/out of order table.
 *     <li>TODO #35 (enhancement): change the destroyed table's dependency on a reservation to a dependency on a hut.
 *     <li>TODO #40 (enhancement): change the forgotten table's dependency on a reservation to a dependency on a hut.
 * </ul>
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
     *
     * See issue #39 and issue #41.
     */
    private final ObservableList<Hut>           hutList;
    private final ObservableList<Reservation>   reservationList;
    private final ObservableList<Equipment>     equipmentList;
    private final ObservableList<Forgotten>     forgottenList;
    private final ObservableList<Destroyed>     destroyedList;

    /**
     * Create a data model from the given SQL connection.
     *
     * @param logger            Logger object to be used for all logging by the data model
     * @param connection        Connection to the SQL database.
     * @throws SQLException     Any conceivable SQL exception.
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

    /* --- end of SQL section. --- */

    /**
     * Return the list of huts.
     *
     * @return  The list of all huts.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Hut> getHutList() {
        return hutList;
    }

    /**
     * Return the hut object corresponding to the given database ID if it exists.
     *
     * @param ID the identity of the hut to retrieve.
     * @return either a hut or nothing, depending on the existence of a hut with the given ID.
     * @deprecated a new data model API is under development and will replace the current one.
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
     * @return the list of all reservations.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Reservation> getReservationList() {
        return reservationList;
    }

    /**
     * Return the list of reservations for the given hut.
     *
     * @param hut the hut whose reservations should be returned.
     * @return the list of all reservations for the given hut.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Reservation> getReservationListForHut(Hut hut) {
        return getReservationList().filtered(reservation -> reservation.getHutID() == hut.getID());
    }

    /**
     * Return the reservation object corresponding to the given database ID if it exists.
     *
     * @param ID the identity of the reservation to retrieve.
     * @return either a reservation or nothing, depending on the existence of a reservation with the given ID.
     * @deprecated a new data model API is under development and will replace the current one.
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
     * @param reservation the reservation object to insert into the database.
     * @throws SQLException any conceivable SQL exception.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public void insertReservation(Reservation reservation) throws SQLException {
        logger.info("Adding reservation to database...");

        reservationInsertStmt.setInt(1, reservation.getHutID());
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
        }

        logger.info("Adding new reservation to reservation list.");

        reservationList.add(reservation);
    }

    /**
     * Return the list of equipment.
     *
     * @return the list of all equipment for all huts.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Equipment> getEquipmentList() {
        return equipmentList;
    }

    /**
     * Return the list of equipment for the given hut.
     *
     * @param hut the hut whose equipment should be returned.
     * @return A list of equipment for the given hut.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Equipment> getEquipmentListForHut(Hut hut) {
        return getEquipmentList().filtered(item -> item.getHutID() == hut.getID());
    }

    /**
     * Return the list of forgotten items.
     *
     * @return the list of all forgotten items.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Forgotten> getForgottenList() {
        return forgottenList;
    }

    /**
     * Insert a forgotten item into the database, setting the ID of the object to the database ID used.
     *
     * @param forgotten the item to insert into the database.
     * @throws SQLException any conceivable SQL exception.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public void insertForgotten(Forgotten forgotten) throws SQLException {
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
     * @return a list of all destroyed items for all huts.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Destroyed> getDestroyedList() {
        return destroyedList;
    }

    /**
     * Insert a destroyed item into the database, setting the ID of the object to the database ID used.
     *
     * @param destroyed the item to insert into the database.
     * @throws SQLException any conceivable SQL exception.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public void insertDestroyed(Destroyed destroyed) throws SQLException {
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

        logger.info("Adding new destroyed item to list.");

        destroyedList.add(destroyed);
    }

    /**
     * Retrieve a list of records from the database and create an ObservableList<> of objects.
     *
     * @param stmt  Prepared statement to retrieve the records from the database.
     * @param fn    A function taking a resultSet and returning a list element.
     * @param <E>   The type of elements in the final list.
     * @throws SQLException any conceivable SQL exception.
     * @deprecated a new data model API is under development and will replace the current one.
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

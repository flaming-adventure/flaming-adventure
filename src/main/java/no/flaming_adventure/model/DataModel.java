package no.flaming_adventure.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import no.flaming_adventure.SQLFunction;

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

    private static final String sqlHutList              = "SELECT * FROM huts;";
    private static final String sqlReservationList      = "SELECT * FROM reservations;";
    private static final String sqlEquipmentList        = "SELECT * FROM equipment;";
    private static final String sqlForgottenItemList    = "SELECT * FROM forgotten_items;";
    private static final String sqlBrokenItemList       = "SELECT * FROM broken_items;";

    private static final String sqlInsertReservation    =
            "INSERT INTO reservations (hut_id, date, name, email, count, comment) VALUES (?, ?, ?, ?, ?, ?);";

    private static final String sqlInsertBrokenItem     =
            "INSERT INTO broken_items (hut_id, item, date, fixed, comment) VALUES (?, ?, ?, ?, ?);";

    private static final String sqlInsertForgottenItem  =
            "INSERT INTO forgotten_items (hut_id, item, name, contact, date, delivered, comment) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?);";

    private static final Logger LOGGER = Logger.getLogger(DataModel.class.getName());;

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final Statement statement;

    /************************************************************************
     *
     * Fields (deprecated)
     *
     ************************************************************************/

    private final PreparedStatement reservationInsertStmt;
    private final PreparedStatement brokenItemInsertStmt;
    private final PreparedStatement forgottenItemInsertStmt;

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
    private final ObservableList<ForgottenItem> forgottenItemList;
    private final ObservableList<BrokenItem>    brokenItemList;

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
        statement = connection.createStatement();

        LOGGER.fine("Initializing data model...");
        LOGGER.finest("Preparing data model statements...");

        PreparedStatement hutStmt           = connection.prepareStatement(sqlHutList);
        PreparedStatement reservationStmt   = connection.prepareStatement(sqlReservationList);
        PreparedStatement equipmentStmt     = connection.prepareStatement(sqlEquipmentList);
        PreparedStatement forgottenItemStmt = connection.prepareStatement(sqlForgottenItemList);
        PreparedStatement brokenItemStmt    = connection.prepareStatement(sqlBrokenItemList);

        reservationInsertStmt   = connection.prepareStatement(sqlInsertReservation, Statement.RETURN_GENERATED_KEYS);
        brokenItemInsertStmt    = connection.prepareStatement(sqlInsertBrokenItem, Statement.RETURN_GENERATED_KEYS);
        forgottenItemInsertStmt = connection.prepareStatement(sqlInsertForgottenItem, Statement.RETURN_GENERATED_KEYS);

        hutList = forceList(hutStmt, resultSet -> new Hut(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getInt("capacity"),
                resultSet.getInt("firewood")
        ));

        reservationList = forceList(reservationStmt, resultSet -> {
            Integer hutID = resultSet.getInt("hut_id");
            Hut hut = getHutFromID(hutID).get();
            return new Reservation(
                    resultSet.getInt("id"),
                    hut,
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getString("name"),
                    resultSet.getString("email"),
                    resultSet.getInt("count"),
                    resultSet.getString("comment")
            );
        });

        equipmentList = forceList(equipmentStmt, resultSet -> {
            Integer hutID = resultSet.getInt("hut_id");
            Hut hut = getHutFromID(hutID).get();
            return new Equipment(
                    resultSet.getInt("id"),
                    hut,
                    resultSet.getString("name"),
                    resultSet.getDate("purchase_date").toLocalDate(),
                    resultSet.getInt("count")
            );
        });

        forgottenItemList = forceList(forgottenItemStmt, resultSet -> {
            Integer hutID = resultSet.getInt("hut_id");
            Hut hut = getHutFromID(hutID).get();
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
        });

        brokenItemList = forceList(brokenItemStmt, resultSet -> {
            Integer hutID = resultSet.getInt("hut_id");
            Hut hut = getHutFromID(hutID).get();
            return new BrokenItem(
                    resultSet.getInt("id"),
                    hut,
                    resultSet.getString("item"),
                    resultSet.getDate("date").toLocalDate(),
                    resultSet.getBoolean("fixed"),
                    resultSet.getString("comment")
            );
        });

        LOGGER.fine("Data model successfully initialized.");
    }

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public ObservableList<Hut> getHutList() throws SQLException {
        ObservableList<Hut> huts = FXCollections.observableArrayList();
        String query = "SELECT * FROM huts;";
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            huts.add(hutFromResultSet(resultSet));
        }
        return huts;
    }

    /************************************************************************
     *
     * Public API (deprecated)
     *
     ************************************************************************/

    /**
     * Return the list of huts.
     *
     * @return  The list of all huts.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<Hut> getHutListDeprecated() {
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
        for (Hut hut : getHutListDeprecated()) {
            if (hut.getId() == ID) { return Optional.of(hut); }
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
        return getReservationList().filtered(reservation -> reservation.getHut().getId() == hut.getId());
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
            if (reservation.getId() == ID) { return Optional.of(reservation); }
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
        LOGGER.info("Adding reservation to database...");

        reservationInsertStmt.setInt(1, reservation.getHut().getId());
        reservationInsertStmt.setDate(2, Date.valueOf(reservation.getDate()));
        reservationInsertStmt.setString(3, reservation.getName());
        reservationInsertStmt.setString(4, reservation.getEmail());
        reservationInsertStmt.setInt(5, reservation.getCount());
        reservationInsertStmt.setString(6, reservation.getComment());

        reservationInsertStmt.executeUpdate();

        ResultSet resultSet = reservationInsertStmt.getGeneratedKeys();

        if (resultSet.next()) {
            LOGGER.fine("Database returned primary key for reservation.");

            reservation.setId(resultSet.getInt(1));
        } else {
            LOGGER.warning("Database failed to return primary key for reservation.");
        }

        LOGGER.info("Adding new reservation to reservation list.");

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
        return getEquipmentList().filtered(item -> item.getHut().getId() == hut.getId());
    }

    /**
     * Return the list of forgotten items.
     *
     * @return the list of all forgotten items.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<ForgottenItem> getForgottenItemList() {
        return forgottenItemList;
    }

    /**
     * Insert a forgottenItem item into the database, setting the ID of the object to the database ID used.
     *
     * @param forgottenItem the item to insert into the database.
     * @throws SQLException any conceivable SQL exception.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public void insertForgotten(ForgottenItem forgottenItem) throws SQLException {
        LOGGER.info("Adding forgottenItem item to database...");

        forgottenItemInsertStmt.setInt(1, forgottenItem.getHut().getId());
        forgottenItemInsertStmt.setString(2, forgottenItem.getItem());
        forgottenItemInsertStmt.setString(3, forgottenItem.getName());
        forgottenItemInsertStmt.setString(4, forgottenItem.getContact());
        forgottenItemInsertStmt.setDate(5, Date.valueOf(forgottenItem.getDate()));
        forgottenItemInsertStmt.setBoolean(6, forgottenItem.getDelivered());
        forgottenItemInsertStmt.setString(7, forgottenItem.getComment());

        forgottenItemInsertStmt.executeUpdate();

        ResultSet resultSet = forgottenItemInsertStmt.getGeneratedKeys();

        if (resultSet.next()) {
            LOGGER.fine("Database returned primary key for forgottenItem item.");

            forgottenItem.setId(resultSet.getInt(1));
        } else {
            LOGGER.warning("Database failed to return primary key for forgottenItem item.");
        }

        LOGGER.info("Adding new forgottenItem item to list.");
        getForgottenItemList().add(forgottenItem);
    }

    /**
     * Return the list of destroyed (or out of order) items.
     *
     * @return a list of all destroyed items for all huts.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public ObservableList<BrokenItem> getBrokenItemList() {
        return brokenItemList;
    }

    /**
     * Insert a brokenItem item into the database, setting the ID of the object to the database ID used.
     *
     * @param brokenItem the item to insert into the database.
     * @throws SQLException any conceivable SQL exception.
     * @deprecated a new data model API is under development and will replace the current one.
     */
    public void insertBrokenItem(BrokenItem brokenItem) throws SQLException {
        LOGGER.info("Adding brokenItem item to database...");

        brokenItemInsertStmt.setInt(1, brokenItem.getHut().getId());
        brokenItemInsertStmt.setString(2, brokenItem.getItem());
        brokenItemInsertStmt.setDate(3, Date.valueOf(brokenItem.getDate()));
        brokenItemInsertStmt.setBoolean(4, brokenItem.getFixed());
        brokenItemInsertStmt.setString(5, brokenItem.getComment());

        brokenItemInsertStmt.executeUpdate();

        ResultSet resultSet = brokenItemInsertStmt.getGeneratedKeys();

        if (resultSet.next()) {
            LOGGER.fine("Database returned primary key for brokenItem item.");

            brokenItem.setId(resultSet.getInt(1));
        } else {
            LOGGER.warning("Database failed to return primary key for brokenItem item.");
        }

        LOGGER.info("Adding new brokenItem item to list.");

        brokenItemList.add(brokenItem);
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

    /************************************************************************
     *
     * Private implementation (deprecated)
     *
     ************************************************************************/

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

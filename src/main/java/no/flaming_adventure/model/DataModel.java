package no.flaming_adventure.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager for the application's data layer. Responsible for communication with the SQL server.
 *
 * <p> See <pre>src/dist/schema.ddl</pre> for the database layout.
 */
public class DataModel {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    // The following strings are turned into prepared SQL statements on construction.

    /*language=MySQL*/ private static final String SQL_OVERVIEW_BETWEEN_DATES;
    /*language=MySQL*/ private static final String SQL_OVERVIEW_FROM_DATE;
    /*language=MySQL*/ private static final String SQL_OVERVIEW_TO_DATE;
    /*language=MySQL*/ private static final String SQL_OVERVIEW_ALL;
    /*language=MySQL*/ private static final String SQL_ALL_HUTS;
    /*language=MySQL*/ private static final String SQL_HUT_FOR_ID;
    /*language=MySQL*/ private static final String SQL_OCCUPANCY_AT_DATE;
    /*language=MySQL*/ private static final String SQL_INSERT_RESERVATION;
    /*language=MySQL*/ private static final String SQL_INSERT_FORGOTTEN_ITEM;
    /*language=MySQL*/ private static final String SQL_INSERT_BROKEN_ITEM;
    /*language=MySQL*/ private static final String SQL_INSERT_EQUIPMENT;

    private static final Logger LOGGER = Logger.getLogger(DataModel.class.getName());

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final Statement statement;

    private final PreparedStatement overviewBetweenDatesStmt;
    private final PreparedStatement overviewFromDateStmt;
    private final PreparedStatement overviewToDateStmt;
    private final PreparedStatement overviewAllStmt;
    private final PreparedStatement hutsAllStmt;
    private final PreparedStatement hutForIdStmt;
    private final PreparedStatement occupancyAtDateStmt;
    private final PreparedStatement reservationInsertStmt;
    private final PreparedStatement forgottenItemInsertStmt;
    private final PreparedStatement brokenItemInsertStmt;
    private final PreparedStatement equipmentInsertStmt;

    private final Map<Integer, Hut> hutMap = new HashMap<>();

    /************************************************************************
     *
     * Constructors
     *
     ************************************************************************/

    /**
     * Create a data model from the given SQL connection.
     *
     * @param connection Connection to the SQL database.
     * @throws java.sql.SQLException if an SQLException occurred.
     */
    public DataModel(Connection connection) throws SQLException {
        LOGGER.log(Level.FINE, "Initializing data model.");

        statement = connection.createStatement();

        overviewBetweenDatesStmt    = connection.prepareStatement(SQL_OVERVIEW_BETWEEN_DATES);
        overviewFromDateStmt        = connection.prepareStatement(SQL_OVERVIEW_FROM_DATE);
        overviewToDateStmt          = connection.prepareStatement(SQL_OVERVIEW_TO_DATE);
        overviewAllStmt             = connection.prepareStatement(SQL_OVERVIEW_ALL);

        hutsAllStmt                 = connection.prepareStatement(SQL_ALL_HUTS);
        hutForIdStmt                = connection.prepareStatement(SQL_HUT_FOR_ID);
        occupancyAtDateStmt         = connection.prepareStatement(SQL_OCCUPANCY_AT_DATE);

        reservationInsertStmt   = connection.prepareStatement(SQL_INSERT_RESERVATION,
                                                              Statement.RETURN_GENERATED_KEYS);
        forgottenItemInsertStmt = connection.prepareStatement(SQL_INSERT_FORGOTTEN_ITEM,
                                                              Statement.RETURN_GENERATED_KEYS);
        brokenItemInsertStmt    = connection.prepareStatement(SQL_INSERT_BROKEN_ITEM,
                                                              Statement.RETURN_GENERATED_KEYS);
        equipmentInsertStmt     = connection.prepareStatement(SQL_INSERT_EQUIPMENT,
                                                              Statement.RETURN_GENERATED_KEYS);

        LOGGER.fine("Data model successfully initialized.");
    }

    /************************************************************************
     *
     * Public API
     *
     ************************************************************************/

    public ObservableList<Hut> getHuts() throws SQLException {
        ObservableList<Hut> huts = FXCollections.observableArrayList();
        ResultSet resultSet = hutsAllStmt.executeQuery();
        while (resultSet.next()) {
            Hut hut = hutFromResultSet(resultSet);
            huts.add(hut);
            hutMap.put(hut.getId(), hut);
        }
        return huts;
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
        occupancyAtDateStmt.setInt(1, hut.getId());
        occupancyAtDateStmt.setDate(2, Date.valueOf(date));

        ResultSet resultSet = occupancyAtDateStmt.executeQuery();
        resultSet.next();
        return resultSet.getInt(1);
    }

    public Integer reservationCount(Hut hut, LocalDate fromDate, LocalDate toDate) throws SQLException {
        String query = genSQLGenericCount("reservations", hut, "date", fromDate, toDate, null);
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt(1);
    }


    public ObservableList<Reservation> reservationPage(Integer pageStart, Integer pageSize, Hut hut, LocalDate fromDate,
                                                       LocalDate toDate, String orderBy) throws SQLException {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        String query = genSQLGenericPage("reservations", pageStart, pageSize, "hut_id", hut, "date", fromDate, toDate,
                                         orderBy, null);
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            reservations.add(reservationFromResultSet(resultSet));
        }
        return reservations;
    }

    public Integer equipmentCount(Hut hut, LocalDate fromDate, LocalDate toDate) throws SQLException {
        String query = genSQLGenericCount("equipment", hut, "purchase_date", fromDate, toDate, null);
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt(1);
    }

    public ObservableList<Equipment> equipmentPage(Integer pageStart, Integer pageSize, Hut hut, LocalDate fromDate,
                                                   LocalDate toDate, String orderBy) throws SQLException {
        ObservableList<Equipment> items = FXCollections.observableArrayList();
        String query = genSQLGenericPage("equipment", pageStart, pageSize, "hut_id", hut, "purchase_date", fromDate,
                                         toDate, orderBy, null);
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            items.add(equipmentFromResultSet(resultSet));
        }
        return items;
    }

    public Integer brokenItemCount(Hut hut, LocalDate fromDate, LocalDate toDate, String filterBy) throws SQLException {
        String query = genSQLGenericCount("broken_items", hut, "date", fromDate, toDate, filterBy);
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt(1);
    }

    public ObservableList<BrokenItem> brokenItemPage(Integer pageStart, Integer pageSize, Hut hut,
                                                     LocalDate fromDate, LocalDate toDate, String orderBy,
                                                     String filterBy)
            throws SQLException {
        ObservableList<BrokenItem> brokenItems = FXCollections.observableArrayList();
        String query = genSQLGenericPage("broken_items", pageStart, pageSize, "hut_id", hut, "date", fromDate, toDate,
                                         orderBy, filterBy);
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            brokenItems.add(brokenItemFromResultSet(resultSet));
        }
        return brokenItems;
    }

    public Integer forgottenItemCount(Hut hut, LocalDate fromDate, LocalDate toDate, String filterBy)
            throws SQLException {
        String query = genSQLGenericCount("forgotten_items", hut, "date", fromDate, toDate, filterBy);
        ResultSet resultSet = statement.executeQuery(query);
        resultSet.next();
        return resultSet.getInt(1);
    }

    public ObservableList<ForgottenItem> forgottenItemPage(Integer pageStart, Integer pageSize, Hut hut,
                                                           LocalDate fromDate, LocalDate toDate, String orderBy,
                                                           String filterBy)
            throws SQLException {
        ObservableList<ForgottenItem> forgottenItems = FXCollections.observableArrayList();
        String query = genSQLGenericPage("forgotten_items", pageStart, pageSize, "hut_id", hut, "date", fromDate,
                                         toDate, orderBy, filterBy);
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            forgottenItems.add(forgottenItemFromResultSet(resultSet));
        }
        return forgottenItems;
    }


    /**
     * Retrieve the records required for the overview table.
     *
     * <p> This is a potentially costly query.
     */
    public ObservableList<OverviewRow> overviewRows(LocalDate fromDate, LocalDate toDate) throws SQLException {
        ObservableList<OverviewRow> overviewRows = FXCollections.observableArrayList();

        PreparedStatement stmt = prepareOverviewStmt(fromDate, toDate);

        long days = daysInRange(fromDate, toDate);

        ResultSet resultSet = stmt.executeQuery();
        while (resultSet.next()) {
            Hut hut = hutFromResultSet(resultSet);

            // Calculate occupancy.
            BigDecimal count = resultSet.getBigDecimal("count");
            BigDecimal occupancy = null;
            if (days > 0 && count != null) {
                BigDecimal totalCapacity = BigDecimal.valueOf(days * hut.getCapacity());
                occupancy = count.divide(totalCapacity, MathContext.DECIMAL32);
            }

            // Get possible next date.
            Date sqlDate = resultSet.getDate("next");
            LocalDate date = null;
            if (sqlDate != null) {
                date = sqlDate.toLocalDate();
            }

            Integer brokenCount = resultSet.getInt("broken_count");
            Integer forgottenCount = resultSet.getInt("forgotten_count");

            overviewRows.add(new OverviewRow(hut, brokenCount, forgottenCount, occupancy, date));
        }
        return overviewRows;
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

    public void insertForgottenItem(ForgottenItem forgottenItem) throws SQLException {
        LOGGER.log(Level.INFO, "Adding forgotten item to database.");

        forgottenItemInsertStmt.setInt(1, forgottenItem.getHut().getId());
        forgottenItemInsertStmt.setString(2, forgottenItem.getItem());
        forgottenItemInsertStmt.setString(3, forgottenItem.getName());
        forgottenItemInsertStmt.setString(4, forgottenItem.getContact());
        forgottenItemInsertStmt.setDate(5, Date.valueOf(forgottenItem.getDate()));
        forgottenItemInsertStmt.setBoolean(6, forgottenItem.getDelivered());
        forgottenItemInsertStmt.setString(7, forgottenItem.getComment());

        forgottenItemInsertStmt.executeUpdate();

        ResultSet resultSet = forgottenItemInsertStmt.getGeneratedKeys();
        resultSet.next();
        forgottenItem.setId(resultSet.getInt(1));
    }

    public void insertBrokenItem(BrokenItem brokenItem) throws SQLException {
        LOGGER.log(Level.INFO, "Adding broken item to database.");

        brokenItemInsertStmt.setInt(1, brokenItem.getHut().getId());
        brokenItemInsertStmt.setString(2, brokenItem.getItem());
        brokenItemInsertStmt.setDate(3, Date.valueOf(brokenItem.getDate()));
        brokenItemInsertStmt.setBoolean(4, brokenItem.getFixed());
        brokenItemInsertStmt.setString(5, brokenItem.getComment());

        brokenItemInsertStmt.executeUpdate();

        ResultSet resultSet = brokenItemInsertStmt.getGeneratedKeys();
        resultSet.next();
        brokenItem.setId(resultSet.getInt(1));
    }

    public void insertEquipment(Equipment item) throws SQLException {
        LOGGER.log(Level.INFO, "Adding broken item to database.");

        equipmentInsertStmt.setInt(1, item.getHut().getId());
        equipmentInsertStmt.setString(2, item.getName());
        equipmentInsertStmt.setDate(3, Date.valueOf(item.getPurchaseDate()));
        equipmentInsertStmt.setInt(4, item.getCount());

        equipmentInsertStmt.executeUpdate();

        ResultSet resultSet = equipmentInsertStmt.getGeneratedKeys();
        resultSet.next();
        item.setId(resultSet.getInt(1));
    }

    public void updateBrokenItemFixed(BrokenItem item) throws SQLException {
        /*language=MySQL*/
        String query = "UPDATE broken_items SET fixed=" + sqlBool(item.getFixed()) + "\n" +
                       "WHERE id=" + item.getId() + ';';
        statement.executeUpdate(query);
    }

    public void updateForgottenItemDelivered(ForgottenItem item) throws SQLException {
        /*language=MySQL*/
        String query = "UPDATE forgotten_items SET delivered=" +sqlBool(item.getDelivered()) + '\n' +
                       "WHERE id=" + item.getId() + ';';
        statement.executeUpdate(query);
    }

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    private static String sqlBool(Boolean bool) {
        if (bool == Boolean.TRUE) {
            return "TRUE";
        } else {
            return "FALSE";
        }
    }

    /**
     * Return an SQL query for a filtered count of records in the given table.
     *
     * @param table     the name of the database table.
     * @param hut       a hut to filter on (optionally <code>null</code>). Assumes that <code>hut_id</code> is a column
     *                  in the given table.
     * @param fromDate  exclude all records before this date. Assumes that <code>date</code> is a column in the given
     *                  table. Can optionally be <code>null</code>.
     * @param toDate    exclude all records after this date. Assumes that <code>date</code> is a column in the given
     *                  table. Can optionally be <code>null</code>.
     * @param filterBy  parameter to the WHERE clause.
     * @return a string with the requested SQL query.
     */
    private static String genSQLGenericCount(String table, Hut hut, String dateField,
                                             LocalDate fromDate, LocalDate toDate, String filterBy) {
        StringBuilder builder = new StringBuilder("SELECT COUNT(*) FROM ").append(table);
        String hutDatePredicate = genHutDatePredicate("hut_id", hut, dateField, fromDate, toDate);
        if (hutDatePredicate != null) {
            builder.append(" WHERE ")
                   .append(hutDatePredicate); }
        if (filterBy != null) {
            if (hutDatePredicate != null) {
                builder.append(" AND ");
            } else {
                builder.append(" WHERE ");
            }
            builder.append(filterBy);
        }
        return builder.append(';').toString();
    }

    /**
     * Return an SQL query for a filtered set of records from the given table.
     *
     * @param table     the name of the database table.
     * @param pageStart the first record to return. Counts from zero.
     * @param pageSize  the number of records to return.
     * @param hut       a hut to filter on (optionally <code>null</code>). Assumes that <code>hut_id</code> is a column
     *                  in the given table.
     * @param fromDate  exclude all records before this date. Assumes that <code>date</code> is a column in the given
     *                  table. Can optionally be <code>null</code>.
     * @param toDate    exclude all records after this date. Assumes that <code>date</code> is a column in the given
     *                  table. Can optionally be <code>null</code>.
     * @param orderBy   ordering, can optionally be <code>null</code>.
     * @param filterBy  parameter to the WHERE clause, can optionally be <code>null</code>.
     * @return a string with the requested SQL query.
     */
    private static String genSQLGenericPage(String table, Integer pageStart, Integer pageSize, String hutField,
                                            Hut hut, String dateField, LocalDate fromDate, LocalDate toDate,
                                            String orderBy, String filterBy) {
        StringBuilder builder = new StringBuilder("SELECT huts.name, ").append(table).append(".* FROM ").append(table)
                .append(" LEFT JOIN huts ON huts.id = ").append(table).append('.').append(hutField);
        String hutDatePredicate = genHutDatePredicate(hutField, hut, table + '.' + dateField, fromDate, toDate);
        if (hutDatePredicate != null) {
            builder.append(" WHERE ")
                   .append(hutDatePredicate);
        }
        if (filterBy != null) {
            if (hutDatePredicate != null) {
                builder.append(" AND ");
            } else {
                builder.append(" WHERE ");
            }
            builder.append(filterBy);
        }
        if (orderBy != null) {
            builder.append(" ORDER BY ")
                   .append(orderBy);
        }
        builder.append(" LIMIT ")
               .append(pageStart)
               .append(", ")
               .append(pageSize)
               .append(';');
        return builder.toString();
    }

    /**
     * Generate an SQL predicate for the given date range.
     *
     * @param field     the name of the field to filter on.
     * @param fromDate  exclude all records before this date. Can optionally be <code>null</code>.
     * @param toDate    exclude all records after this date. Can optionally be <code>null</code>.
     * @return a string containing an SQL predicate (the part following a <code>WHERE</code> clause).
     */
    private static String genDatePredicate(String field, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null) {
            return String.format("%s BETWEEN '%s' AND '%s'", field, Date.valueOf(fromDate), Date.valueOf(toDate));
        } else if (fromDate != null) {
            return String.format("%s >= '%s'", field, Date.valueOf(fromDate));
        } else if (toDate != null) {
            return String.format("%s <= '%s'", field, Date.valueOf(toDate));
        } else {
            return null;
        }
    }

    /**
     * Generate an SQL predicate for the given hut and date range.
     *
     * @param hutField  the name of the hut column.
     * @param hut       the hut to filter on. Can optionally be <code>null</code>.
     * @param dateField the name of the date column.
     * @param fromDate  exclude all records before this date. Can optionally be <code>null</code>.
     * @param toDate    exclude all records after this date. Can optionally be <code>null</code>.
     * @return a string containing an SQL predicate (the part following a <code>WHERE</code> clause).
     */
    private static String genHutDatePredicate(String hutField, Hut hut,
                                              String dateField, LocalDate fromDate, LocalDate toDate) {
        String datePredicate = genDatePredicate(dateField, fromDate, toDate);
        if (hut != null && datePredicate != null) {
            return String.format("%s = %d AND %s", hutField, hut.getId(), datePredicate);
        } else if (hut != null) {
            return String.format("%s = %d", hutField, hut.getId());
        } else if (datePredicate != null) {
            return datePredicate;
        } else {
            return null;
        }
    }

    /**
     * Return the correct, fully prepared, overview statement for the given dates.
     *
     * @param fromDate  exclude all records before this date. Can optionally be <code>null</code>.
     * @param toDate    exclude all records after this date. Can optionally be <code>null</code>.
     * @return a prepared statement filtered on the given dates.
     * @throws SQLException if an SQLException occurred.
     */
    private PreparedStatement prepareOverviewStmt(LocalDate fromDate, LocalDate toDate) throws SQLException {
        PreparedStatement stmt;
        if (fromDate != null && toDate != null) {
            stmt = overviewBetweenDatesStmt;
            for (int i = 1; i <= 6; ) {
                stmt.setDate(i++, Date.valueOf(fromDate));
                stmt.setDate(i++, Date.valueOf(toDate));
            }
        } else if (fromDate != null) {
            stmt = overviewFromDateStmt;
            for (int i = 1; i <= 3; ) {
                stmt.setDate(i++, Date.valueOf(fromDate));
            }
        } else if (toDate != null) {
            stmt = overviewToDateStmt;
            for (int i = 1; i <= 3; ) {
                stmt.setDate(i++, Date.valueOf(toDate));
            }
        } else {
            stmt = overviewAllStmt;
        }
        return stmt;
    }

    /**
     * Return the hut matching the given database ID if at all possible.
     *
     * @param id the ID of the hut to return.
     * @return the hut.
     * @throws SQLException if an SQLException occurred.
     */
    private Hut hutFromId(Integer id) throws SQLException {
        Hut hut = hutMap.get(id);
        if (hut == null) {
            hutForIdStmt.setInt(1, id);
            ResultSet resultSet = hutForIdStmt.executeQuery();
            resultSet.next();
            hut = hutFromResultSet(resultSet);
        }
        return hut;
    }

    /**
     * Attempt to create a hut from the given result set.
     *
     * @param resultSet a result set pointing at a valid hut record.
     * @return a hut.
     * @throws SQLException if an SQLException occurred.
     */
    private Hut hutFromResultSet(ResultSet resultSet) throws SQLException {
        return new Hut(resultSet.getInt("huts.id"),
                       resultSet.getString("huts.name"),
                       resultSet.getInt("huts.capacity"),
                       resultSet.getInt("huts.firewood"));
    }

    private Reservation reservationFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutFromId(resultSet.getInt("reservations.hut_id"));
        return new Reservation(resultSet.getInt("reservations.id"),
                               hut,
                               resultSet.getDate("reservations.date").toLocalDate(),
                               resultSet.getString("reservations.name"),
                               resultSet.getString("reservations.email"),
                               resultSet.getInt("reservations.count"),
                               resultSet.getString("reservations.comment"));
    }

    private ForgottenItem forgottenItemFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutFromId(resultSet.getInt("forgotten_items.hut_id"));
        return new ForgottenItem(resultSet.getInt("forgotten_items.id"),
                                 hut,
                                 resultSet.getString("forgotten_items.item"),
                                 resultSet.getString("forgotten_items.name"),
                                 resultSet.getString("forgotten_items.contact"),
                                 resultSet.getDate("forgotten_items.date").toLocalDate(),
                                 resultSet.getBoolean("forgotten_items.delivered"),
                                 resultSet.getString("forgotten_items.comment"));
    }

    private BrokenItem brokenItemFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutFromId(resultSet.getInt("broken_items.hut_id"));
        return new BrokenItem(resultSet.getInt("broken_items.id"),
                              hut,
                              resultSet.getString("broken_items.item"),
                              resultSet.getDate("broken_items.date").toLocalDate(),
                              resultSet.getBoolean("broken_items.fixed"),
                              resultSet.getString("broken_items.comment"));
    }

    private Equipment equipmentFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutFromId(resultSet.getInt("equipment.hut_id"));
        return new Equipment(resultSet.getInt("equipment.id"),
                             hut,
                             resultSet.getString("equipment.name"),
                             resultSet.getDate("equipment.purchase_date").toLocalDate(),
                             resultSet.getInt("equipment.count"));
    }

    private long daysInRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return 0;
        } else {
            // Add 1 for inclusive range.
            return to.getLong(ChronoField.EPOCH_DAY) - from.getLong(ChronoField.EPOCH_DAY) + 1;
        }
    }

    static {
        SQL_OVERVIEW_BETWEEN_DATES = "SELECT\n" +
                                     "  huts.*, R.count, R.next, B.broken_count, F.forgotten_count\n" +
                                     "FROM huts\n" +
                                     "  LEFT JOIN (SELECT hut_id, date, SUM(count) AS count, MIN(date) AS next\n" +
                                     "             FROM reservations\n" +
                                     "             WHERE date BETWEEN ? AND ?\n" +
                                     "             GROUP BY hut_id)\n" +
                                     "    AS R ON R.hut_id = huts.id\n" +
                                     "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS broken_count\n" +
                                     "             FROM broken_items\n" +
                                     "             WHERE date BETWEEN ? AND ?\n" +
                                     "             GROUP BY hut_id)\n" +
                                     "    AS B ON B.hut_id = huts.id\n" +
                                     "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS forgotten_count\n" +
                                     "             FROM forgotten_items\n" +
                                     "             WHERE date BETWEEN ? AND ?\n" +
                                     "             GROUP BY hut_id)\n" +
                                     "    AS F ON F.hut_id = huts.id ;";

        SQL_OVERVIEW_FROM_DATE = "SELECT\n" +
                                 "  huts.*, R.count, R.next, B.broken_count, F.forgotten_count\n" +
                                 "FROM huts\n" +
                                 "  LEFT JOIN (SELECT hut_id, date, SUM(count) AS count, MIN(date) AS next\n" +
                                 "             FROM reservations\n" +
                                 "             WHERE date >= ?\n" +
                                 "             GROUP BY hut_id)\n" +
                                 "    AS R ON R.hut_id = huts.id\n" +
                                 "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS broken_count\n" +
                                 "             FROM broken_items\n" +
                                 "             WHERE date >= ?\n" +
                                 "             GROUP BY hut_id)\n" +
                                 "    AS B ON B.hut_id = huts.id\n" +
                                 "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS forgotten_count\n" +
                                 "             FROM forgotten_items\n" +
                                 "             WHERE date >= ?\n" +
                                 "             GROUP BY hut_id)\n" +
                                 "    AS F ON F.hut_id = huts.id ;";


        SQL_OVERVIEW_TO_DATE = "SELECT\n" +
                               "  huts.*, R.count, R.next, B.broken_count, F.forgotten_count\n" +
                               "FROM huts\n" +
                               "  LEFT JOIN (SELECT hut_id, date, SUM(count) AS count, MIN(date) AS next\n" +
                               "             FROM reservations\n" +
                               "             WHERE date <= ?\n" +
                               "             GROUP BY hut_id)\n" +
                               "    AS R ON R.hut_id = huts.id\n" +
                               "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS broken_count\n" +
                               "             FROM broken_items\n" +
                               "             WHERE date <= ?\n" +
                               "             GROUP BY hut_id)\n" +
                               "    AS B ON B.hut_id = huts.id\n" +
                               "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS forgotten_count\n" +
                               "             FROM forgotten_items\n" +
                               "             WHERE date <= ?\n" +
                               "             GROUP BY hut_id)\n" +
                               "    AS F ON F.hut_id = huts.id ;";

        SQL_OVERVIEW_ALL = "SELECT\n" +
                           "  huts.*, R.count, R.next, B.broken_count, F.forgotten_count\n" +
                           "FROM huts\n" +
                           "  LEFT JOIN (SELECT hut_id, date, SUM(count) AS count, MIN(date) AS next\n" +
                           "             FROM reservations\n" +
                           "             GROUP BY hut_id)\n" +
                           "    AS R ON R.hut_id = huts.id\n" +
                           "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS broken_count\n" +
                           "             FROM broken_items\n" +
                           "             GROUP BY hut_id)\n" +
                           "    AS B ON B.hut_id = huts.id\n" +
                           "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS forgotten_count\n" +
                           "             FROM forgotten_items\n" +
                           "             GROUP BY hut_id)\n" +
                           "    AS F ON F.hut_id = huts.id ;";

        SQL_INSERT_RESERVATION =
                "INSERT INTO reservations (hut_id, date, name, email, count, comment)\n" + "VALUES (?, ?, ?, ?, ?, ?);";

        SQL_INSERT_FORGOTTEN_ITEM = "INSERT INTO forgotten_items\n" +
                                    "(hut_id, item, name, contact, date, delivered, comment)\n" +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

        SQL_INSERT_BROKEN_ITEM =
                "INSERT INTO broken_items (hut_id, item, date, fixed, comment)\n" + "VALUES (?, ?, ?, ?, ?);";

        SQL_INSERT_EQUIPMENT =
                "INSERT INTO equipment (hut_id, name, purchase_date, count)\n" +
                "VALUES (?, ?, ?, ?);";

        SQL_ALL_HUTS = "SELECT * FROM huts;";

        SQL_OCCUPANCY_AT_DATE = "SELECT SUM(reservations.count)\n" +
                                "FROM reservations\n" +
                                "WHERE reservations.hut_id = ?\n" +
                                "      AND reservations.date = ?;";
        SQL_HUT_FOR_ID = "SELECT * FROM huts WHERE id = ?;";
    }
}

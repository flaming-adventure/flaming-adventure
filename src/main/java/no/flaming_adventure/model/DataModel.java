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
 */
public class DataModel {

    /************************************************************************
     *
     * Static fields
     *
     ************************************************************************/

    private static final String SQL_INSERT_RESERVATION =
            "INSERT INTO reservations (hut_id, date, name, email, count, comment) VALUES (?, ?, ?, ?, ?, ?);";

    private static final String SQL_INSERT_FORGOTTEN_ITEM =
            "INSERT INTO forgotten_items (hut_id, item, name, contact, date, delivered, comment) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?);";

    private static final String SQL_INSERT_BROKEN_ITEM =
            "INSERT INTO broken_items (hut_id, item, date, fixed, comment) " +
                    "VALUES (?, ?, ?, ?, ?);";

    private static final Logger LOGGER = Logger.getLogger(DataModel.class.getName());

    /************************************************************************
     *
     * Fields
     *
     ************************************************************************/

    private final Statement statement;
    private final Statement altStatement;

    private final PreparedStatement occupancyStmt;
    private final PreparedStatement reservationInsertStmt;
    private final PreparedStatement forgottenItemInsertStmt;
    private final PreparedStatement brokenItemInsertStmt;

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
        altStatement = connection.createStatement();
        occupancyStmt = connection.prepareStatement("SELECT SUM(reservations.count) FROM reservations " +
                        "WHERE reservations.hut_id = ? AND reservations.date = ?;");

        reservationInsertStmt   = connection.prepareStatement(SQL_INSERT_RESERVATION, Statement.RETURN_GENERATED_KEYS);
        forgottenItemInsertStmt = connection.prepareStatement(SQL_INSERT_FORGOTTEN_ITEM,
                Statement.RETURN_GENERATED_KEYS);
        brokenItemInsertStmt    = connection.prepareStatement(SQL_INSERT_BROKEN_ITEM, Statement.RETURN_GENERATED_KEYS);

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

    public Integer reservationCount() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM reservations;");
        resultSet.next();
        return resultSet.getInt(1);
    }

    public ObservableList<Reservation> reservationPage(Integer pageStart, Integer pageSize) throws SQLException {
        ObservableList<Reservation> reservations = FXCollections.observableArrayList();
        String query = String.format("SELECT * FROM reservations LIMIT %d, %d;", pageStart, pageSize);
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            reservations.add(reservationFromResultSet(resultSet));
        }
        return reservations;
    }

    public Integer brokenItemCount() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM broken_items;");
        resultSet.next();
        return resultSet.getInt(1);
    }

    public ObservableList<BrokenItem> brokenItemPage(Integer pageStart, Integer pageSize) throws SQLException {
        ObservableList<BrokenItem> brokenItems = FXCollections.observableArrayList();
        String query = String.format("SELECT * FROM broken_items LIMIT %d, %d;", pageStart, pageSize);
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            brokenItems.add(brokenItemFromResultSet(resultSet));
        }
        return brokenItems;
    }

    public Integer forgottenItemCount() throws SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM forgotten_items;");
        resultSet.next();
        return resultSet.getInt(1);
    }

    public ObservableList<ForgottenItem> forgottenItemPage(Integer pageStart, Integer pageSize) throws SQLException {
        ObservableList<ForgottenItem> forgottenItems = FXCollections.observableArrayList();
        String query = String.format("SELECT * FROM forgotten_items LIMIT %d, %d;", pageStart, pageSize);
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
    public ObservableList<OverviewRow> overviewRows(LocalDate from, LocalDate to) throws SQLException {
        final String queryFormat = "SELECT" +
                "  huts.*, R.count, R.next, B.broken_count, F.forgotten_count " +
                "FROM huts " +
                "  LEFT JOIN (SELECT hut_id, date, SUM(count) AS count, MIN(date) as next" +
                "             FROM reservations" +
                "             %1$s" +
                "             GROUP BY hut_id)" +
                "    AS R ON R.hut_id = huts.id" +
                "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS broken_count" +
                "             FROM broken_items" +
                "             %1$s" +
                "             GROUP BY hut_id)" +
                "    AS B ON B.hut_id = huts.id" +
                "  LEFT JOIN (SELECT hut_id, date, COUNT(id) AS forgotten_count" +
                "             FROM forgotten_items" +
                "             %1$s" +
                "             GROUP BY hut_id)" +
                "    AS F ON F.hut_id = huts.id " +
                ";";
        String query;
        long days = 0;
        if (from != null && to != null) {
            String predicate = String.format(
                    "WHERE date BETWEEN '%s' AND '%s'",
                    Date.valueOf(from).toString(), Date.valueOf(to).toString());
            query = String.format(queryFormat, predicate);
            days =  to.getLong(ChronoField.EPOCH_DAY) - from.getLong(ChronoField.EPOCH_DAY) + 1;
        } else {
            query = String.format(queryFormat, "");
        }

        ObservableList<OverviewRow> overviewRows = FXCollections.observableArrayList();
        ResultSet resultSet = statement.executeQuery(query);
        while (resultSet.next()) {
            Hut hut = hutFromResultSet(resultSet);

            // Calculate occupancy percentage.
            BigDecimal count = resultSet.getBigDecimal("count");
            BigDecimal occupancy = null;
            if (days > 0 && count != null) {
                BigDecimal totalCapacity = BigDecimal.valueOf(days * hut.getCapacity());
                occupancy = count.divide(totalCapacity, MathContext.DECIMAL32)
                        .multiply(BigDecimal.valueOf(100), MathContext.DECIMAL32);
            }

            // Get possible next date.
            Date sqlDate = resultSet.getDate("next");
            LocalDate date = null;
            if (sqlDate != null) { date = sqlDate.toLocalDate(); }

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

    /************************************************************************
     *
     * Private implementation
     *
     ************************************************************************/

    private Hut hutFromId(Integer id) throws SQLException {
        Hut hut = hutMap.get(id);
        if (hut == null) {
            String query = String.format("SELECT * FROM huts WHERE id = %d;", id);
            ResultSet resultSet = altStatement.executeQuery(query);
            // The result set might not contain a hut, but we simply let exception be thrown.
            resultSet.next();
            hut = hutFromResultSet(resultSet);
        }
        return hut;
    }

    private Hut hutFromResultSet(ResultSet resultSet) throws SQLException {
        return new Hut(
                resultSet.getInt("id"),
                resultSet.getString("name"),
                resultSet.getInt("capacity"),
                resultSet.getInt("firewood")
        );
    }

    private Reservation reservationFromResultSet(ResultSet resultSet) throws SQLException {
        Hut hut = hutFromId(resultSet.getInt("hut_id"));
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
        Hut hut = hutFromId(resultSet.getInt("hut_id"));
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
        Hut hut = hutFromId(resultSet.getInt("hut_id"));
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
        Hut hut = hutFromId(resultSet.getInt("hut_id"));
        return new Equipment(
                resultSet.getInt("id"),
                hut,
                resultSet.getString("name"),
                resultSet.getDate("purchase_date").toLocalDate(),
                resultSet.getInt("count")
        );
    }
}

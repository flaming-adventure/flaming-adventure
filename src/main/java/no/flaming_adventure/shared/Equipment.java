package no.flaming_adventure.shared;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class Equipment {
    protected final IntegerProperty ID;
    protected final IntegerProperty hutID;
    protected final StringProperty item;
    protected final StringProperty date;
    protected final IntegerProperty count;

    public Equipment(Integer ID, Integer hutID, String item, String date, Integer count) {
        this.ID = new SimpleIntegerProperty(ID);
        this.hutID = new SimpleIntegerProperty(hutID);
        this.item = new SimpleStringProperty(item);
        this.date = new SimpleStringProperty(date);
        this.count = new SimpleIntegerProperty(count);
    }

    public static Equipment fromResultSet(ResultSet resultSet, SimpleDateFormat dateFormat) throws SQLException {
        return new Equipment(
                resultSet.getInt("id"),
                resultSet.getInt("hut_id"),
                resultSet.getString("name"),
                dateFormat.format(resultSet.getDate("purchase_date")),
                resultSet.getInt("count"));
    }

    public String getItem() {
        return item.get();
    }

    public int getID() {
        return ID.get();
    }

    public IntegerProperty IDProperty() {
        return ID;
    }

    public int getHutID() {
        return hutID.get();
    }

    public IntegerProperty hutIDProperty() {
        return hutID;
    }

    public StringProperty itemProperty() {
        return item;
    }

    public String getDate() {
        return date.get();
    }

    public StringProperty dateProperty() {
        return date;
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }
}

package no.flaming_adventure.shared;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Equipment {
    protected StringProperty item;
    protected StringProperty date;
    protected IntegerProperty count;

    public Equipment(String item, String date, Integer count) {
        this.item = new SimpleStringProperty(item);
        this.date = new SimpleStringProperty(date);
        this.count = new SimpleIntegerProperty(count);
    }

    public String getItem() {
        return item.get();
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

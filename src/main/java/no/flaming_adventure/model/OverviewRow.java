package no.flaming_adventure.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class OverviewRow {
    private final ObjectProperty<Hut>           hut;
    private final IntegerProperty               brokenCount;
    private final IntegerProperty               forgottenCount;
    private final ObjectProperty<Number>        occupancy;
    private final ObjectProperty<LocalDate>     nextReservation;

    public OverviewRow(Hut hut, Integer brokenCount, Integer forgottenCount, Number occupancy,
                       LocalDate nextReservation) {
        this.hut = new SimpleObjectProperty<>(hut);
        this.brokenCount = new SimpleIntegerProperty(brokenCount);
        this.forgottenCount = new SimpleIntegerProperty(forgottenCount);
        this.occupancy = new SimpleObjectProperty<>(occupancy);
        this.nextReservation = new SimpleObjectProperty<>(nextReservation);
    }

    public Hut getHut() {
        return hut.get();
    }

    public ObjectProperty<Hut> hutProperty() {
        return hut;
    }

    public int getBrokenCount() {
        return brokenCount.get();
    }

    public IntegerProperty brokenCountProperty() {
        return brokenCount;
    }

    public int getForgottenCount() {
        return forgottenCount.get();
    }

    public IntegerProperty forgottenCountProperty() {
        return forgottenCount;
    }

    public Number getOccupancy() {
        return occupancy.get();
    }

    public ObjectProperty<Number> occupancyProperty() {
        return occupancy;
    }

    public LocalDate getNextReservation() {
        return nextReservation.get();
    }

    public ObjectProperty<LocalDate> nextReservationProperty() {
        return nextReservation;
    }
}

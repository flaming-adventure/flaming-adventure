package no.flaming_adventure.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public class OverviewRow {
    private final ObjectProperty<Hut>           hut;
    private final IntegerProperty               brokenCount;
    private final IntegerProperty               forgottenCount;
    private final ObjectProperty<BigDecimal>    occupancy;
    private final ObjectProperty<LocalDate>     nextReservation;

    public OverviewRow(Hut hut, Integer brokenCount, Integer forgottenCount, BigDecimal occupancy,
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

    public BigDecimal getOccupancy() {
        return occupancy.get();
    }

    public ObjectProperty<BigDecimal> occupancyProperty() {
        return occupancy;
    }

    public LocalDate getNextReservation() {
        return nextReservation.get();
    }

    public ObjectProperty<LocalDate> nextReservationProperty() {
        return nextReservation;
    }
}

package no.flaming_adventure.model;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Reservation {
    private final IntegerProperty           id;
    private final ObjectProperty<Hut>       hut;
    private final ObjectProperty<LocalDate> date;
    private final StringProperty            name;
    private final StringProperty            email;
    private final IntegerProperty           count;
    private final StringProperty            comment;

    public Reservation(Integer id, Hut hut, LocalDate date, String name, String email, Integer count, String comment) {
        this.id = new SimpleIntegerProperty(id);
        this.hut = new SimpleObjectProperty<>(hut);
        this.date = new SimpleObjectProperty<>(date);
        this.name = new SimpleStringProperty(name);
        this.email = new SimpleStringProperty(email);
        this.count = new SimpleIntegerProperty(count);
        this.comment = new SimpleStringProperty(comment);
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public Hut getHut() {
        return hut.get();
    }

    public ObjectProperty<Hut> hutProperty() {
        return hut;
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public String getComment() {
        return comment.get();
    }

    public StringProperty commentProperty() {
        return comment;
    }
}

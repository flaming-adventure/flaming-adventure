package no.flaming_adventure.shared;

import java.util.Date;

public class Booking {
    protected final Integer ID;
    protected final Integer hutID;
    protected final Date date;
    protected final String name;
    protected final String email;
    protected final Integer count;
    protected final String comment;

    public Booking(Integer ID, Integer hutID, Date date, String name, String email, Integer count, String comment) {
        this.comment = comment;
        this.count = count;
        this.email = email;
        this.name = name;
        this.date = date;
        this.hutID = hutID;
        this.ID = ID;
    }

    public Integer getID() {
        return ID;
    }

    public Integer getHutID() {
        return hutID;
    }

    public Date getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Integer getCount() {
        return count;
    }

    public String getComment() {
        return comment;
    }
}

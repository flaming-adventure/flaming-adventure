package no.flaming_adventure.shared;

public class Hut {
    protected final Integer ID;
    protected final String name;
    protected final Integer capacity;
    protected final Integer firewood;

    /**
     * Constructor.
     *
     * @param ID       ID of the hut within the database, should be null if the hut does not yet exist in the database.
     * @param name     Name of the hut.
     * @param capacity Total number of beds in the hut.
     * @param firewood The number of sacks of firewood in the hut.
     */
    public Hut(Integer ID, String name, Integer capacity, Integer firewood) {
        this.ID = ID;
        this.name = name;
        this.capacity = capacity;
        this.firewood = firewood;
    }

    @Override
    public String toString() {
        return name;
    }

    public Integer getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Integer getFirewood() {
        return firewood;
    }
}
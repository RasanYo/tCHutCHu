package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.Objects;

/**
 * classe représentant les stations du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Station {

    private final int id;
    private final String name;

    /**
     * construit une station
     *
     * @param id   identité de la station
     * @param name nom de la station.
     * @Throws IllegalArgumentException si l'id est < 0.
     */
    public Station(int id, String name) {
        Preconditions.checkArgument(id >= 0);
        this.id = id;
        this.name = Objects.requireNonNull(name);
    }

    /**
     * @return l'identité de la station.
     */
    public int id() {
        return id;
    }

    /**
     * @return nom de la station.
     */
    public String name() {
        return name;
    }

    /**
     * @return nom de la station.
     */
    @Override
    public String toString() {
        return name;
    }
}

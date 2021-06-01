package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * classe représentant les trajets.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Trip {

    private final Station from;
    private final Station to;
    private final int points;

    public Trip(Station from, Station to, int points) {

        this.from = Objects.requireNonNull(from);
        this.to = Objects.requireNonNull(to);
        Preconditions.checkArgument(points > 0);
        this.points = points;
    }

    /**
     * @param from   liste des gares de depart
     * @param to     liste des gares d'arrivee
     * @param points valeur de points des trajets que nous cherchons
     * @return la liste de tous les trajets possibles allant d'une des
     * gares de la premiere liste (from) à l'une des gares de
     * la seconde liste (to), chacun valant le nombre de points donne
     * @throws IllegalArgumentException si une des listes est vide et si on passe
     * en parametre un nombre de point negatif
     */
    public static List<Trip> all(List<Station> from, List<Station> to, int points) {
        Preconditions.checkArgument(!from.isEmpty() && !to.isEmpty());

        List<Trip> connection = new ArrayList<>();

        for (Station f : from) {
            for (Station t : to) {
                connection.add(new Trip(f, t, points));
            }
        }
        return connection;
    }

    /**
     * @return la gare de depart
     */
    public Station from() {
        return from;
    }

    /**
     * @return la gare d'arrivee
     */
    public Station to() {
        return to;
    }

    /**
     * @return le nombre de points que vaut le trajet
     */
    public int points() {
        return points;
    }

    /**
     * @param connectivity la connectivite du reseau du joueur
     * @return le nombre de point que vaut le trajet pour la connectivite donne
     */
    public int points(StationConnectivity connectivity) {
        return connectivity.connected(from, to) ? points : -points;
    }
}
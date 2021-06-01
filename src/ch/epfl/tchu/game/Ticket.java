package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * classe représentant les billets du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Ticket implements Comparable<Ticket> {

    private final List<Trip> tripList;
    private final String textToCompute;

    /**
     * @param trips liste de chemins.
     */
    public Ticket(List<Trip> trips) {
        Preconditions.checkArgument(!trips.isEmpty());
        this.tripList = new ArrayList<>();
        String tmp = trips.get(0).from().name();
        for (Trip t : trips) {
            Preconditions.checkArgument(t.from().name().equals(tmp));
            this.tripList.add(t);
        }
        textToCompute = computeText(tripList);

    }

    /**
     * construit un ticket
     *
     * @param from   Station de départ
     * @param to     station d'arrivée
     * @param points points du billets.
     */
    public Ticket(Station from, Station to, int points) {
        this(List.of(new Trip(from, to, points)));
    }

    /**
     * @param list liste de chemins.
     * @return une chaine de caractères représentant les chemins de la listes.
     */
    private static String computeText(List<Trip> list) {
        TreeSet<String> destinationList = new TreeSet<>();

        list.forEach(t -> destinationList.add(String.format("%s (%s)", t.to().name(), t.points())));

        return destinationList.size() > 1 ?
                String.format("%s - {%s}", list.get(0).from().name(), String.join(", ", destinationList)) :
                String.format("%s - %s", list.get(0).from().name(), String.join("", destinationList));

    }

    /**
     * @return une chaine de caractères représentant le ticket.
     */
    public String text() {
        return textToCompute;
    }

    /**
     * @return une chaine de caractères représentant le ticket.
     */
    @Override
    public String toString() {
        return textToCompute;
    }

    /**
     * @param connectivity les stations connectées
     * @return les points obtenues en ayant connectés différentes stations.
     */
    public int points(StationConnectivity connectivity) {
        return tripList.stream().mapToInt(t -> t.points(connectivity)).max().orElse(0);
    }

    /**
     * @param that un billet a comparé avec le billet courant.
     * @return retourne un entier strictement négatif
     * si this est strictement plus petit que that,
     * un entier strictement positif si this est strictement plus grand que that,
     * et zéro si les deux sont égaux.
     * il compare le billet auquel on l'applique l'applique à celui passé en argument.
     */
    @Override
    public int compareTo(Ticket that) {
        return this.text().compareTo(that.text());

    }
}

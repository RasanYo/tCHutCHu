package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * classe représentant l'état publique des joueurs.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public class PublicPlayerState {

    private final int ticketCount;
    private final int cardCount;
    private final List<Route> routes;
    private final int carCount;
    private final int claimPoints;

    /**
     * @param ticketCount nombre de billets que le joueur possède
     * @param cardCount   nombre de wagons du joueur
     * @param routes      liste de routes que possède le joueur.
     * @Throws IllegalArgumentException si le nombre de billets est inférieur 0,
     * ou si le nombre de wagons est inférieur à 0.
     */
    public PublicPlayerState(int ticketCount, int cardCount, List<Route> routes) {
        Preconditions.checkArgument(!(ticketCount < 0 || cardCount < 0));
        this.ticketCount = ticketCount;
        this.cardCount = cardCount;
        this.routes = new ArrayList<>(routes);
        int carCount = Constants.INITIAL_CAR_COUNT;
        int points = 0;
        for (Route r : routes) {
            points += r.claimPoints();
            carCount -= r.length();
        }
        this.carCount = carCount;
        this.claimPoints = points;
    }

    /**
     * @return nombre de billets du joueur.
     */
    public int ticketCount() {
        return ticketCount;
    }

    /**
     * @return nombre de wagons du joueur.
     */
    public int cardCount() {
        return cardCount;
    }

    /**
     * @return liste des routes que possède le joueur.
     */
    public List<Route> routes() {
        return routes;
    }

    /**
     * @return nombre de wagons du joueur.
     */
    public int carCount() {
        return carCount;
    }

    /**
     * @return les points du joueurs.
     */
    public int claimPoints() {
        return claimPoints;
    }
}
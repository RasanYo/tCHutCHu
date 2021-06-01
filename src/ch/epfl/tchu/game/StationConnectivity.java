package ch.epfl.tchu.game;

/**
 * interface représentant les connexions de stations.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public interface StationConnectivity {
    /**
     * @param s1 station 1
     * @param s2 station 2
     * @return vrai si les gares donnees sont reliees par le reseau du joueur.
     */
    boolean connected(Station s1, Station s2);
}

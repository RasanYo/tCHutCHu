package ch.epfl.tchu.game;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * classe représentant les chemins.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Trail {

    private final int length;
    private final Station station1;
    private final Station station2;
    private final List<Route> routes;

    /**
     * Construit un chemin avec les parametres suivants
     * @param station1 station  de depart
     * @param station2 station d'arrivee
     * @param routes List<Route> liste de routes qu'empruntent le chemin
     */
    private Trail(Station station1, Station station2, List<Route> routes) {
        int length = 0;
        this.station1 = station1;
        this.station2 = station2;
        this.routes = new ArrayList<>();
        for(Route r : routes) {
            this.routes.add(r);
            length += r.length();
        }
        this.length = length;
    }

    /**
     *
     * @param routes List<Route> liste de routes parmi laquelle nous cherchons le plus chemin
     * @return le plus long chemin du reseau constitue des routes donnees
     */
    public static Trail longest(List<Route> routes) {
        Trail maxLengthTrail = new Trail(new Station(0, "null1"), new Station(1, "null2"), List.of());
        if (routes.isEmpty()) {
            return maxLengthTrail;
        }
        List<Trail> trailList = new ArrayList<>();
        for (Route r : routes) {
            trailList.add(new Trail(r.station1(), r.station2(), List.of(r)));
            trailList.add(new Trail(r.station2(), r.station1(), List.of(r)));
            if (trailList.get(trailList.size() - 1).length() > maxLengthTrail.length()) {
                maxLengthTrail = trailList.get(trailList.size() - 1);
            }
        }
        while (!trailList.isEmpty()) {
            List<Trail> trails = new ArrayList<>();
            for (Trail c : trailList) {
                for (Route r : routes) {
                    List<Route> routeList = new ArrayList<>(c.routes);
                    if (!routeList.contains(r) && (c.station2().equals(r.station1()) || c.station2().equals(r.station2()))) {
                        routeList.add(r);
                        Station lastStation = r.stationOpposite(c.station2());
                        trails.add(new Trail(c.station1(), lastStation, routeList));

                        if (trails.get(trails.size() - 1).length() > maxLengthTrail.length()) {
                            maxLengthTrail = trails.get(trails.size() - 1);
                        }
                    }

                }
            }
            trailList = trails;
        }

        return maxLengthTrail;
    }


    /**
     *
     * @return int la longueur du chemin
     */
    public int length() {return this.length;}

    /**
     *
     * @return Station la 1ere gare du chemin
     */
    public Station station1() {
        return length() == 0 ? null : station1;
    }

    /**
     *
     * @return Station la derniere gare du chemin
     */
    public Station station2() {
        return length() == 0 ? null : station2;

    }

    @Override
    public String toString() {
        if(station1() == null || station2() == null) {
            return "Pas de stations";
        }
        return String.format("%s - %s (%s)", station1().toString(), station2().toString(), length());
    }



}
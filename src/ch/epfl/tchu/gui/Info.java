package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.Card;
import ch.epfl.tchu.game.Route;
import ch.epfl.tchu.game.Trail;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * classe représentant les informations que peuvent recevoir les joueurs.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Info {

    private final String playerName;

    /**
     * @param playerName nom du joueurs emmetteur de messages.
     */
    public Info(String playerName) {
        this.playerName = playerName;
    }

    /**
     * @param route une route
     * @return le nom de la station de depart et celle d'arrivee.
     */
    private static String routeName(Route route) {
        List<String> stationName = List.of(route.station1().toString(), route.station2().toString());
        return String.join(StringsFr.EN_DASH_SEPARATOR, stationName);
    }

    /**
     * @param cards un multiensemble de cartes
     * @return la liste de toutes les cartes du multiensemble (sous forme de chaine de caracteres),
     * avec leur multiplicite a gauche, separee chacune par une virgule
     * (le derniere ensemble des cartes est separee du reste par un "et").
     */
    private static String cardToString(SortedBag<Card> cards) {
        List<String> cardNames = new ArrayList<>();
        for (Card c : cards.toSet()) {
            int n = cards.countOf(c);
            cardNames.add(n + " " + cardName(c, n));
        }
        if (cardNames.size() > 1) {
            return String.join(", ", cardNames.subList(0, cardNames.size() - 1)) +
                    StringsFr.AND_SEPARATOR + cardNames.get(cardNames.size() - 1);
        } else {
            return cardNames.get(cardNames.size() - 1);
        }
    }

    /**
     * @param trail un chemin
     * @return le nom du chemin (la station de depart et la station d'arrivee separee par un "et").
     */
    private static String trailToString(Trail trail) {
        List<String> stationsName = List.of(trail.station1().toString(), trail.station2().toString());
        return String.join(StringsFr.EN_DASH_SEPARATOR, stationsName);
    }

    /**
     * @param card  une carte
     * @param count le nombre de cartes
     * @return la couleur de la carte (sous forme de chaine de caracteres) ou des cartes si il y'en a plusieurs.
     */
    public static String cardName(Card card, int count) {
        switch (card) {

            case BLACK:
                return StringsFr.BLACK_CARD + StringsFr.plural(count);
            case VIOLET:
                return StringsFr.VIOLET_CARD + StringsFr.plural(count);
            case BLUE:
                return StringsFr.BLUE_CARD + StringsFr.plural(count);
            case GREEN:
                return StringsFr.GREEN_CARD + StringsFr.plural(count);
            case YELLOW:
                return StringsFr.YELLOW_CARD + StringsFr.plural(count);
            case ORANGE:
                return StringsFr.ORANGE_CARD + StringsFr.plural(count);
            case RED:
                return StringsFr.RED_CARD + StringsFr.plural(count);
            case WHITE:
                return StringsFr.WHITE_CARD + StringsFr.plural(count);
            case LOCOMOTIVE:
                return StringsFr.LOCOMOTIVE_CARD + StringsFr.plural(count);
            default:
                return "";
        }
    }

    /**
     * @param playerNames la liste du nom des joueur
     * @param points      le nombre de points des joueurs
     * @return retourne le message declarant que les joueurs,
     * dont les noms sont ceux donnes,
     * ont termine la partie ex æqo en ayant chacun remporte les points donnes.
     */
    public static String draw(List<String> playerNames, int points) {
        return String.format(StringsFr.DRAW, String.join(StringsFr.AND_SEPARATOR, playerNames), points);
    }

    /**
     * @return le message declarant que le joueur jouera en premier.
     */
    public String willPlayFirst() {
        return String.format(StringsFr.WILL_PLAY_FIRST, playerName);
    }

    /**
     * @param count nombre de billets garde
     * @return le message declarant que le joueur a garde le nombre de billets donne.
     */
    public String keptTickets(int count) {
        return String.format(StringsFr.KEPT_N_TICKETS, playerName, count, StringsFr.plural(count));
    }

    /**
     * @return le message declarant que le joueur peut jouer.
     */
    public String canPlay() {
        return String.format(StringsFr.CAN_PLAY, playerName);
    }

    /**
     * @param count nombre de billets retires
     * @return le message declarant que le joueur a tire le nombre donne de billets.
     */
    public String drewTickets(int count) {
        return String.format(StringsFr.DREW_TICKETS, playerName, count, StringsFr.plural(count));
    }

    /**
     * @return le message declarant que le joueur a tire une carte «à l'aveugle», c-a-d du sommet de la pioche.
     */
    public String drewBlindCard() {
        return String.format(StringsFr.DREW_BLIND_CARD, playerName);
    }

    /**
     * @param card carte tire par le joueur et qui est face visible
     * @return le message declarant que le joueur a tire la carte disposee face visible donnee.
     */
    public String drewVisibleCard(Card card) {
        return String.format(StringsFr.DREW_VISIBLE_CARD, playerName, Info.cardName(card, 1));
    }

    /**
     * @param route route dont le joueur s'est empare
     * @param cards multiensemble de cartes qui a permit au joueur de prendre possession de la route
     * @return le message declarant que le joueur s'est empare de la route donnee au moyen des cartes donnees.
     */
    public String claimedRoute(Route route, SortedBag<Card> cards) {
        return String.format(StringsFr.CLAIMED_ROUTE, playerName, routeName(route), cardToString(cards));
    }

    /**
     * @param route        route dont le joueur veut empare
     * @param initialCards multiensemble de cartes qui permettra au joueur de prendre possession de la route
     * @return le message declarant que le joueur desire
     * s'emparer de la route en tunnel donnee en utilisant initialement les cartes donnees.
     */
    public String attemptsTunnelClaim(Route route, SortedBag<Card> initialCards) {
        return String.format(StringsFr.ATTEMPTS_TUNNEL_CLAIM, playerName, routeName(route), cardToString(initialCards));
    }

    /**
     * @param drawnCards     multiensemble constitue de 3 cartes additionnelles.
     * @param additionalCost additionnel du nombre de cartes donne
     * @return retourne le message declarant que le joueur a tire les trois cartes additionnelles donnees,
     * et qu'elles impliquent un cout additionel du nombre de cartes donne
     */
    public String drewAdditionalCards(SortedBag<Card> drawnCards, int additionalCost) {
        if (additionalCost == 0) {
            return String.format(StringsFr.ADDITIONAL_CARDS_ARE, cardToString(drawnCards)) +
                    StringsFr.NO_ADDITIONAL_COST;
        } else {
            return String.format(StringsFr.ADDITIONAL_CARDS_ARE, cardToString(drawnCards)) +
                    String.format(StringsFr.SOME_ADDITIONAL_COST, additionalCost, StringsFr.plural(additionalCost));
        }
    }

    /**
     * @param route route dont le joueur n'a pas pu (ou voulu) s'emparer
     * @return le message declarant que le joueur n'a pas pu (ou voulu) s'emparer du tunnel donne
     */
    public String didNotClaimRoute(Route route) {
        return String.format(StringsFr.DID_NOT_CLAIM_ROUTE, playerName, routeName(route));
    }

    /**
     * @param carCount nombre de wagons restant du joueur
     * @return le message declarant que le joueur n'a plus que le nombre donne (et inferieur ou egale a 2) de wagons,
     * et que le dernier tour commence.
     */
    public String lastTurnBegins(int carCount) {
        return String.format(StringsFr.LAST_TURN_BEGINS, playerName, carCount, StringsFr.plural(carCount));
    }

    /**
     * @param longestTrail chemin le plus long de la partie
     * @return le message declarant que le joueur obtient le bonus de fin de partie grace au chemin donne,
     * qui est le plus long, ou l'un des plus longs.
     */
    public String getsLongestTrailBonus(Trail longestTrail) {
        return String.format(StringsFr.GETS_BONUS, playerName, trailToString(longestTrail));
    }

    /**
     * @param points du joueur gagnant
     * @param points du joeur perdant
     * @return le message declarant que le joueur remporte la partie avec le nombre de points donnes,
     * son adversaire n'en ayant obtenu que un certain nombre de points.
     */
    public String won(int points, int loserPoints) {
        return String.format(StringsFr.WINS,
                playerName,
                points,
                StringsFr.plural(points),
                loserPoints,
                StringsFr.plural(loserPoints));
    }

    public String destroyedRoute(Route route) {
        return playerName + " a detruit la route " + routeName(route);
    }

}


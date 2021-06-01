package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * classe représentant les routes du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Route {
    private final String id;
    private final Station stations1;
    private final Station stations2;
    private final int length;
    private final Level level;
    private final Color color;
    /**
     * Valide aux tests
     * Construit une nouvelle Route avec les attributs suivants
     *
     * @param id       String representant l'identite de la gars
     * @param station1 Station representant la premiere gare de la route
     * @param station2 Station representant la deuxieme gare de la route
     * @param length   int representant la longueur de la route
     * @param level    Level indiquant a quelle surface situe la route
     * @param color    Color indiquant la couleur de la route
     */
    public Route(String id, Station station1, Station station2, int length, Level level, Color color) {
        Preconditions.checkArgument(!station1.equals(station2) && length >= Constants.MIN_ROUTE_LENGTH && length <= Constants.MAX_ROUTE_LENGTH);
        this.id = Objects.requireNonNull(id);
        this.stations1 = Objects.requireNonNull(station1);
        this.stations2 = Objects.requireNonNull(station2);
        this.length = length;
        this.level = Objects.requireNonNull(level);
        this.color = color;
    }

    /**
     * @return String identite de la route
     */
    public String id() {
        return id;
    }

    /**
     * @return Station la premiere gare de la route
     */
    public Station station1() {
        return stations1;
    }

    /**
     * @return Station la seconde gare de la route
     */
    public Station station2() {
        return stations2;
    }

    /**
     * @return int la longueur de la route
     */
    public int length() {
        return this.length;
    }

    /**
     * @return Level de la route
     */
    public Level level() {
        return this.level;
    }

    /**
     * Valide aux tests
     *
     * @return Color la couleur de la route si non nulle, sinon retourne null
     */
    public Color color() {
        return this.color;
    }

    /**
     * @return List contenant les deux gares de la route, dans l'ordre du passage au constructeur
     */
    public List<Station> stations() {
        return List.of(station1(), station2());
    }

    /**
     * Valide aux tests
     *
     * @param station Station a tester
     * @return Station la station oppose
     */
    public Station stationOpposite(Station station) {
        Preconditions.checkArgument(station.equals(station1()) || station.equals(station2()));

        return station.equals(station1()) ? station2() : station1();
    }

    /**
     * @return List<SortedBag <Card>> la liste de tous les ensembles de cartes qui pourraient être jouer pour s'emparer
     * de la route en fonction de son type et de sa couleur (dans l'ordre croissant)
     */
    public List<SortedBag<Card>> possibleClaimCards() {
        List<SortedBag<Card>> list = new ArrayList<>();

        List<Card> cardPossibilities = this.color == null ? List.copyOf(Card.CARS) : List.of(Card.of(this.color));

        int NbrOfLocomotives = this.level == Level.UNDERGROUND ? this.length : 0;

        for (int i = 0; i <= NbrOfLocomotives; i++) {
            if (i != this.length) {
                for (Card cardPossibility : cardPossibilities) {
                    SortedBag<Card> claimCardCombination = SortedBag.of(this.length - i, cardPossibility, i, Card.LOCOMOTIVE);
                    list.add(claimCardCombination);
                }
            } else {
                list.add(SortedBag.of(i, Card.LOCOMOTIVE));
            }
        }
        return list;
    }

    /**
     * @param claimCards les cartes utilisees pour s'emparer d'une route
     * @param drawnCards les cartes additionnelles piochees
     * @return le nombre de cartes additionnelles a jouer pour s'emparer de la route (en tunnel)
     */
    public int additionalClaimCardsCount(SortedBag<Card> claimCards, SortedBag<Card> drawnCards) {
        Preconditions.checkArgument(level().
                equals(Level.UNDERGROUND) && drawnCards.size() == Constants.ADDITIONAL_TUNNEL_CARDS);
        return (int) drawnCards
                .toList()
                .stream()
                .filter(c -> claimCards.contains(c) || c.equals(Card.LOCOMOTIVE)).count();
    }

    /**
     * @return int nombre de points obtenus en s'emparant de la route
     */
    public int claimPoints() {
        return Constants.ROUTE_CLAIM_POINTS.get(length);
    }

    /**
     *
     * @return le voisin d'une route s'il existe, autrement soi-meme
     */
    public Route neighbor() {
        return ChMap.routes()
                .stream()
                .filter(r -> stations1.equals(r.stations1)
                        && stations2.equals(r.stations2)
                        && !this.id().equals(r.id()))
                .findFirst().orElse(this);
    }

    public enum Level {
        OVERGROUND,
        UNDERGROUND
    }

    @Override
    public String toString() {
        return station1().toString() + " - " + station2().toString();
    }


}

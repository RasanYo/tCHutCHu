package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * classe représentant l'état (privé) du joueur.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class PlayerState extends PublicPlayerState {

    private final SortedBag<Ticket> tickets;
    private final SortedBag<Card> cards;

    /**
     * construit l'etat d'un joueur possedant les billets, cartes et routes donnes.
     * @param tickets: tickets du joueur.
     * @param cards: cartes du joueur.
     * @param routes: routes du joueur.
     */
    public PlayerState(SortedBag<Ticket> tickets, SortedBag<Card> cards, List<Route> routes) {
        super(tickets.size(), cards.size(), routes);
        this.tickets = tickets;
        this.cards = cards;
    }

    /**
     *
     * @param initialCards: cartes initiales a distribue.
     * @return l'etat initial d'un joueur auquel les cartes initiales donnees ont ete distribuees.
     * @throws IllegalArgumentException si le nombre de cartes initiales ne vaut pas 4.
     */
    public static PlayerState initial(SortedBag<Card> initialCards) {
        Preconditions.checkArgument(initialCards.size() == Constants.INITIAL_CARDS_COUNT);
        return new PlayerState(SortedBag.of(), initialCards, List.of());
    }

    /**
     *
     * @return retourne les billets du joueur.
     */
    public SortedBag<Ticket> tickets() {
        return tickets;
    }

    /**
     *
     * @param newTickets billets a attribue au joueur.
     * @return un etat identique au recepteur, si ce n'est que le joueur possede en plus les billets donnes.
     */
    public PlayerState withAddedTickets(SortedBag<Ticket> newTickets) {
        return new PlayerState(tickets().union(newTickets), cards(), routes());
    }

    /**
     *
     * @return les cartes du joueur.
     */
    public SortedBag<Card> cards() {
        return cards;
    }

    /**
     *
     * @param card: carte a attribue au joueur.
     * @return un etat identique au recepteur, si ce n'est que le joueur possede en plus la carte donnee
     */
    public PlayerState withAddedCard(Card card) {
        return withAddedCards(SortedBag.of(card));
    }

    /**
     *
     * @param additionalCards: cartes a attribue au joueur.
     * @return un etat identique au recepteur, si ce n'est que le joueur possede en plus les cartes donnees.
     */
    public PlayerState withAddedCards(SortedBag<Card> additionalCards) {
        return new PlayerState(tickets(), cards.union(additionalCards), routes());
    }

    /**
     *
     * @param route: route que le joueur desire s'emparer.
     * @return vrai si le joueur peut s'emparer de la route donnee.
     */
    public boolean canClaimRoute(Route route) {
        if (carCount() < route.length()) {
            return false;
        }
        return carCount() >= route.length() && !possibleClaimCards(route).isEmpty();
    }

    /**
     *
     * @param route: route dont le joueur veut s'emparer.
     * @return la liste de tous les ensembles de cartes que le joueur pourrait utiliser pour prendre possession de la route donnee.
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route) {
        Preconditions.checkArgument(carCount() >= route.length());

        return route.possibleClaimCards().
                stream().
                filter(cards::contains).
                collect(Collectors.toList());
    }

    /**
     *
     * @param additionalCardsCount les cartes que le joueur doit encore posee pour s'emparer de la route.
     * @param initialCards cartes initialement posees par le joueur pour s'emparer de la route.
     * @return  la liste de tous les ensembles de cartes que le joueur pourrait utiliser pour s'emparer d'un tunnel,
     *         trie par ordre croissant du nombre de cartes locomotives.
     * @throws IllegalArgumentException si additionalCardsCount est < 0, si initialCards est vide, si drawnCards ne comprends pas 3 cartes ou si initialCards comporte des cartes d'au moins deux couleurs differentes.
     */
    public List<SortedBag<Card>> possibleAdditionalCards(int additionalCardsCount,
                                                         SortedBag<Card> initialCards) {

        Preconditions.checkArgument(additionalCardsCount > 0 &&
                additionalCardsCount <= Constants.ADDITIONAL_TUNNEL_CARDS &&
                !initialCards.isEmpty());

        Preconditions.checkArgument(initialCards.stream().
                noneMatch(c -> c.color() != null &&
                        c.color() != initialCards.get(0).color()));

        Color color = initialCards.stream().filter(Card.CARS::contains).map(Card::color).findFirst().orElse(null);

        SortedBag<Card> playableCards = SortedBag.of(cards().
                difference(initialCards).
                stream().
                filter(c -> c.color() == color || c.equals(Card.LOCOMOTIVE)).
                collect(Collectors.toList()));

        if (playableCards.size() < additionalCardsCount) {
            return List.of();
        }

        Set<SortedBag<Card>> cardsSet = playableCards.subsetsOfSize(additionalCardsCount);

        return cardsSet.stream().
                sorted(Comparator.comparingInt(cs -> cs.countOf(Card.LOCOMOTIVE))).
                collect(Collectors.toList());
    }

    /**
     *
     * @param route: route dont le joueur s'est empare.
     * @param claimCards: les cartes que le joueur a utilise pour s'emparer de la route.
     * @return retourne un etat identique au recepteur, si ce n'est que le joueur s'est de plus empare de la route donnee au moyen des cartes donnees.
     */
    public PlayerState withClaimedRoute(Route route, SortedBag<Card> claimCards) {
        List<Route> newRoutes = new ArrayList<>(routes());
        newRoutes.add(route);
        return new PlayerState(tickets(), cards().difference(claimCards),newRoutes);
    }

    /**
     *
     * @return le nombre de point(eventuellement negatif)btenus par le joueur grace a ses billets.
     */
    public int ticketPoints() {

        int stationCount = routes().stream().mapToInt(r -> Math.max(r.station1().id(), r.station2().id())).max().orElse(0);

        StationPartition.Builder stationPartitionB = new StationPartition.Builder(stationCount+1);
        routes().forEach(r -> stationPartitionB.connect(r.station1(), r.station2()));

        StationPartition stationPartition = stationPartitionB.build();

        return tickets().stream().mapToInt(t -> t.points(stationPartition)).sum();
    }

    /**
     *
     * @return la totalite des points obtenus par le joueur a la fin de la partie,
     *         a savoir la somme des points retournes par les methodes.
     */
    public int finalPoints() {
        return claimPoints() + ticketPoints();
    }

    //    ############################################## EXTENSIONS #####################################################

    public PlayerState withDestructedRoute(Route route) {
        List<Route> withoutDestroyedRoute = new ArrayList<>(routes());
        withoutDestroyedRoute.remove(route);
        return new PlayerState(tickets(), cards(), withoutDestroyedRoute);
    }

    public boolean canDestroyRoutes() {
        return cards().contains(Card.BOMB);
    }

    public PlayerState withoutCard(Card card) {
        return new PlayerState(tickets(), cards().difference(SortedBag.of(card)), routes());
    }

}




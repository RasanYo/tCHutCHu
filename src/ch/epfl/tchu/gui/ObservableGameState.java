package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.*;
import java.util.stream.Collectors;

import static ch.epfl.tchu.game.Constants.FACE_UP_CARD_SLOTS;

/**
 * classe représentant l'état observable du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class ObservableGameState {

    private final PlayerId ownId;
    private PublicGameState publicGameState;
    private PlayerState playerState;

    //groupe1
    private final IntegerProperty ticketsInDeckPercentage;
    private final IntegerProperty cardsInDeckPercentage;
    private final List<ObjectProperty<Card>> faceUpCardsProperty;
    private final Map<Route, ObjectProperty<PlayerId>> routeOwnerProperty;

    private final Map<Route, BooleanProperty> canDestroyRouteProperty;


    private final IntegerProperty actualTotalPoints;
    private final IntegerProperty actualTicketPoints;

    private final BooleanProperty isCurrentPlayerProperty;

    //groupe2
    private final Map<PlayerId, IntegerProperty> ticketsInHandNumber;
    private final Map<PlayerId, IntegerProperty> cardsInHandNumberProperty;
    private final Map<PlayerId, IntegerProperty> carNumberProperty;
    private final Map<PlayerId, IntegerProperty> constructionPointsProperty;
    //groupe2
    private final ObservableList<Ticket> ticketListProperty;
    private final Map<Card, IntegerProperty> numberCardOfPlayer;
    private final Map<Route, BooleanProperty> claimedRoute;




    public ObservableGameState(PlayerId ownId) {
        this.ownId = ownId;
        this.playerState = null;
        this.publicGameState = null;
        this.ticketsInDeckPercentage = new SimpleIntegerProperty();
        this.ticketsInDeckPercentage.set(0);
        this.cardsInDeckPercentage = new SimpleIntegerProperty();
        this.cardsInDeckPercentage.set(0);
        this.faceUpCardsProperty = createFaceUpCards();
        this.routeOwnerProperty = createRoutePossession();
        this.ticketsInHandNumber = inHandSetup();
        this.cardsInHandNumberProperty = inHandSetup();
        this.carNumberProperty = inHandSetup();
        this.constructionPointsProperty = inHandSetup();
        this.ticketListProperty = FXCollections.observableArrayList();
        this.numberCardOfPlayer = numberCardOfPlayer();
        this.claimedRoute = routeSetup();

        this.actualTotalPoints = new SimpleIntegerProperty(0);
        this.actualTicketPoints = new SimpleIntegerProperty(0);

        this.canDestroyRouteProperty = routeSetup();

        this.isCurrentPlayerProperty = new SimpleBooleanProperty(false);

    }

    /**
     * Methode qui met a jour l'état observale du jeu
     * @param newGameState le nouveau etat public du jeu
     * @param newPlayerState nouveau etat complet du joueur
     */
    public void setState(PublicGameState newGameState, PlayerState newPlayerState) {
        publicGameState = newGameState;
        playerState = newPlayerState;
        ticketsInDeckPercentage.set(newGameState.ticketsCount() * 100 / ChMap.tickets().size());
        cardsInDeckPercentage.set(newGameState.cardState().deckSize() * 100 / Constants.TOTAL_CARDS_COUNT);

        isCurrentPlayerProperty.set(ownId == newGameState.currentPlayerId());

        for (int slot : FACE_UP_CARD_SLOTS) {
            Card newCard = newGameState.cardState().faceUpCard(slot);
            faceUpCardsProperty.get(slot).set(newCard);
        }

        for (PlayerId player : PlayerId.ALL) {
            ticketsInHandNumber.get(player).set(newGameState.playerState(player).ticketCount());
            cardsInHandNumberProperty.get(player).set(newGameState.playerState(player).cardCount());
            carNumberProperty.get(player).set(newGameState.playerState(player).carCount());
            constructionPointsProperty.get(player).set(newGameState.playerState(player).claimPoints());
            newGameState.playerState(player).routes().forEach(r ->
                    routeOwnerProperty.get(r).set(player));
        }

        for (Card c : Card.ALL) {
            numberCardOfPlayer.get(c).set((int)newPlayerState.cards().stream().filter(c::equals).count());
        }

        for (Route r : ChMap.routes()) {
            claimedRoute.get(r).set(ownId == newGameState.currentPlayerId() &&
                    routeOwnerProperty(r).get() == null &&
                    routeOwnerProperty(r.neighbor()).get() == null &&
                    newPlayerState.canClaimRoute(r));

            if(!newGameState.claimedRoutes().contains(r)) {
                routeOwnerProperty.get(r).set(null);
            }

            canDestroyRouteProperty.get(r).set(ownId == newGameState.currentPlayerId() &&
                    newPlayerState.canDestroyRoutes() &&
                    newGameState.routeIsDestroyableFor(ownId, r) &&
                    routeOwnerProperty(r).get() != null);
        }



        actualTicketPoints.set(playerState.ticketPoints());
        actualTotalPoints.set(playerState.finalPoints());

        ticketListProperty.setAll(newPlayerState.tickets().toList());

    }


    /**
     * Methode privee qui initialise les proprietes contenant les cartes face visible a une valeur nulle
     * @return la liste des proprietes contenant les cartes face visible
     */
    private List<ObjectProperty<Card>> createFaceUpCards() {
        List<ObjectProperty<Card>> faceUpCards = new ArrayList<>();
        FACE_UP_CARD_SLOTS.forEach(i -> faceUpCards.add(new SimpleObjectProperty<>()));
        return faceUpCards;
    }

    /**
     * Methode privee qui initialise les proprietes contenant les joueurs proprietaires associe aux routes a une valeur
     * nulle
     * @return la map des proprietes contenant les proprietes contenant les joueurs proprietaires associe aux routes
     */
    private Map<Route, ObjectProperty<PlayerId>> createRoutePossession() {
        Map<Route, ObjectProperty<PlayerId>> routePossessions = new HashMap<>();
        ChMap.routes().forEach(r -> {
            ObjectProperty<PlayerId> owner = new SimpleObjectProperty<>();
            owner.setValue(null);
            routePossessions.putIfAbsent(r, owner);
        });

        return routePossessions;
    }

    /**
     * Methode privee qui initialise les proprietes contenant la disponibilite d'une route associe a cette derniere a
     * une valeur nulle
     * @return la map des proprietes contenant la disponibilite d'une route associe a cette derniere
     */
    private Map<Route, BooleanProperty> routeSetup(){
        return ChMap.routes().stream().collect(Collectors.toMap(r -> r,
                        r -> new SimpleBooleanProperty(false), (a, b) -> a));
    }

    /**
     *
     * @return la map associant une carte a la propriete contenant le nombre de ce tyoe de carte en possession du
     * joueur
     */
    private Map<Card, IntegerProperty> numberCardOfPlayer(){
        return
                Card.ALL.stream().collect(Collectors.toMap(c -> c,
                        c -> new SimpleIntegerProperty(0), (a, b) -> a));

    }

    /**
     * Methode privee qui facilite l'initialisation de la map associant une identite de joueur aux proprietes
     * contenant le nombre de cartes/billets a des valeurs nulles
     * @return map associant une identite de joueur au propriete contenant le nombre de cartes/billets null
     */
    private Map<PlayerId, IntegerProperty> inHandSetup(){
        return PlayerId.ALL.stream().collect(Collectors.toMap(id -> id,
                id -> new SimpleIntegerProperty(0), (a,b) -> a));
    }

    /**
     *
     * @param slot position de la carte
     * @return une vue sur la propriete contenant la carte a la position slot
     */
    public ReadOnlyObjectProperty<Card> faceUpCard(int slot) {
        return faceUpCardsProperty.get(slot);
    }

    /**
     *
     * @param player identite du joueur dont nous voulons observer le nombre de billets
     * @return  une vue sur la propriete contenant le nombre de billet de player
     */
    public ReadOnlyIntegerProperty ticketCountInHand(PlayerId player) {
        return ticketsInHandNumber.get(player);
    }

    /**
     *
     * @param player identite du joueur dont nous voulons observer le nombre de cartes
     * @return  une vue sur la propriete contenant le nombre de cartes de player
     */
    public ReadOnlyIntegerProperty cardCountInHand(PlayerId player) {
        return cardsInHandNumberProperty.get(player);
    }

    /**
     *
     * @param player identite du joueur dont nous voulons observer le nombre de wagons
     * @return  une vue sur la propriete contenant le nombre de wagons de player
     */
    public ReadOnlyIntegerProperty carCount(PlayerId player) {
        return carNumberProperty.get(player);
    }

    /**
     *
     * @param player identite du joueur dont nous voulons observer le nombre de points de construction
     * @return  une vue sur la propriete contenant le nombre de points de construction de player
     */
    public ReadOnlyIntegerProperty constructionPoints(PlayerId player) {
        return constructionPointsProperty.get(player);
    }

    /**
     *
     * @return une vue sur la liste de billets en possession du joueur qu'on représente localement
     */
    public ObservableList<Ticket> ticketList() {
        return FXCollections.unmodifiableObservableList(ticketListProperty);
    }

    /**
     *
     * @return la liste de proprietes stockant les cartes face visible
     */
    public List<ObjectProperty<Card>> faceUpCards() {
        return faceUpCardsProperty;
    }

    /**
     *
     * @param card la carte dont nous cherchons la quantite en possession du joueur
     * @return une vue sur la propriete contenant le nombre de card en possession du joueur
     */
    public ReadOnlyIntegerProperty numberCardOfPlayer(Card card){
        return numberCardOfPlayer.get(card);
    }

    /**
     *
     * @param route dont nous voulons voir la disponibilite
     * @return une vue sur la propriete contenant le boolean indiquant si un joueur s'est deja empare de la route
     */
    public ReadOnlyBooleanProperty possibleToClaimRoute(Route route){
        return claimedRoute.get(route);
    }

    /**
     *
     * @return une vue sur le nombre de billets contenu encore dans la pioche
     */
    public ReadOnlyIntegerProperty ticketsInDeckProperty(){
        return ticketsInDeckPercentage;
    }

    /**
     *
     * @return une vue sur le nombre de cartes contenu encore dans la pioche
     */
    public ReadOnlyIntegerProperty cardsInDeckPercentage(){
        return cardsInDeckPercentage;
    }

    /**
     *
     * @param route dont nous voulons voir quel joueur la possede
     * @return l'identite du joueur qui s'est empare de route (ou null si personne)
     */
    public ReadOnlyObjectProperty<PlayerId> routeOwnerProperty(Route route){
        return routeOwnerProperty.get(route);
    }

    /**
     *
     * @return true si le joueur peut tirer un billet
     */
    public boolean canDrawTickets() {
        return publicGameState.canDrawTickets();
    }

    /**
     *
     * @return true si le joueur peut tirer une carte
     */
    public boolean canDrawCards() {
        return publicGameState.canDrawCards();
    }

    /**
     *
     * @param route route dont le joueur souhaite s'emparer
     * @return la liste des multiensembles de cartes que le joueur peut utiliser pour s'emparer de route
     */
    public List<SortedBag<Card>> possibleClaimCards(Route route) {
        return playerState.possibleClaimCards(route);
    }

    //    ########################################### EXTENSIONS #########################################################

    public ReadOnlyIntegerProperty actualTicketPoints() {
        return actualTicketPoints;
    }

    public ReadOnlyIntegerProperty actualTotalPoints() {
        return actualTotalPoints;
    }

    public ReadOnlyBooleanProperty canDestroyRoute(Route route) {
        return canDestroyRouteProperty.get(route);
    }

    public ReadOnlyBooleanProperty isCurrentPlayer() {
        return isCurrentPlayerProperty;
    }
}

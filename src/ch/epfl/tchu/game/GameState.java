package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;

import java.util.*;

/**
 *
 * classe représentant l'état du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class GameState extends PublicGameState {

    private final Deck<Ticket> ticketDeck;
    private final PlayerId currentPlayer;
    private final Map<PlayerId, PlayerState> playerStateMap;
    private final CardState cardState;
    private final PlayerId lastPlayer;

    /**
     * construit l'état du jeu.
     *
     * @param ticketDeck    pioche de tickets,
     * @param cardState     etat des cartes,
     * @param currentPlayer joueur actuel,
     * @param playerState   etat des joueurs,
     * @param lastPlayer    dernier joueur a avoir jouer son tour ou null si le premier tour n'a pas ete joue.
     */
    private GameState(Deck<Ticket> ticketDeck, CardState cardState, PlayerId currentPlayer,
                      Map<PlayerId, PlayerState> playerState, PlayerId lastPlayer) {
        super(ticketDeck.size(), cardState, currentPlayer, Map.copyOf(playerState), lastPlayer);
        this.ticketDeck = ticketDeck;
        this.cardState = cardState;
        this.currentPlayer = currentPlayer;
        this.playerStateMap = Map.copyOf(playerState);
        this.lastPlayer = lastPlayer;
    }

    /**
     * @param tickets multiensemble de tickets qui va servir comme pioche de ticket
     * @param rng     generateur aleotoire
     * @return etat d'un jeu.
     */
    public static GameState initial(SortedBag<Ticket> tickets, Random rng) {
        Deck<Ticket> ticketDeck = Deck.of(tickets, rng);

        //Distribution des cartes dans le deck
        Deck<Card> allCardDeck = Deck.of(Constants.ALL_CARDS, rng);
        Deck<Card> initialCardDeck = allCardDeck.withoutTopCards(8);

        //Creation de la map et repartition des cartes au sein des joueurs
        Map<PlayerId, PlayerState> playerState = new EnumMap<>(PlayerId.class);

        playerState.put(PlayerId.PLAYER_1, PlayerState.initial(allCardDeck.topCards(Constants.INITIAL_CARDS_COUNT)));
        playerState.put(PlayerId.PLAYER_2, PlayerState.initial(allCardDeck.withoutTopCards(Constants.INITIAL_CARDS_COUNT).topCards(Constants.INITIAL_CARDS_COUNT)));

        //Choix du premier joueur
        PlayerId currentPlayer = PlayerId.ALL.get(rng.nextInt(2));

        return new GameState(ticketDeck, CardState.of(initialCardDeck), currentPlayer, playerState, null);

    }

    /**
     * @return retourne l'état complet du joueur d'identité donnée.
     */
    @Override
    public PlayerState playerState(PlayerId playerId) {
        return playerStateMap.get(playerId);
    }

    /**
     * @return retourner l'état complet du joueur courant.
     */
    @Override
    public PlayerState currentPlayerState() {
        return playerStateMap.get(currentPlayerId());
    }

    /**
     * @param count le nombre de tickets à retourner,
     * @return un multiensemble des tickets au sommet de la pioche,
     * @throws IllegalArgumentException si count n'est pas compris entre 0 et le nombre de tickets dans la pioche.
     */
    public SortedBag<Ticket> topTickets(int count) {
        Preconditions.checkArgument(0 <= count && count <= ticketsCount());
        return ticketDeck.topCards(count);
    }

    /**
     * @param count nombre de tickets au sommet de la pioche,
     * @return l'état du jeu sans les cartes au sommet de la pioche,
     * @throws IllegalArgumentException si count n'est pas compris entre 0 et la taille de la pioche.
     */
    public GameState withoutTopTickets(int count) {
        Preconditions.checkArgument(0 <= count && count <= ticketsCount());
        return new GameState(ticketDeck.withoutTopCards(count), cardState, currentPlayer, playerStateMap, lastPlayer);
    }

    /**
     * @return retourne la carte au sommet de la pioche,
     * @throws IllegalFormatWidthException si la pioche est vide.
     */
    public Card topCard() {
        Preconditions.checkArgument(!cardState.isDeckEmpty());
        return cardState.topDeckCard();
    }

    /**
     * @return un état identique au récepteur mais sans la carte au sommet de la pioche,
     * @throws IllegalArgumentException si la pioche est vide.
     */
    public GameState withoutTopCard() {
        Preconditions.checkArgument(!cardState.isDeckEmpty());
        return new GameState(ticketDeck, cardState.withoutTopDeckCard(), currentPlayerId(),
                new EnumMap<>(playerStateMap), lastPlayer());
    }

    /**
     * @param discardedCards la défausse,
     * @return un état identique au récepteur mais avec les cartes données ajoutées à la défausse.
     */
    public GameState withMoreDiscardedCards(SortedBag<Card> discardedCards) {
        return new GameState(ticketDeck, cardState.withMoreDiscardedCards(discardedCards),
                currentPlayerId(), new EnumMap<>(playerStateMap), lastPlayer());
    }

    /**
     * @param rng type random,
     * @return un état identique au récepteur sauf si la pioche de cartes est vide,
     * auquel cas elle est recréée à partir de la défausse,
     * mélangée au moyen du générateur aléatoire donné
     */
    public GameState withCardsDeckRecreatedIfNeeded(Random rng) {
        return !cardState.isDeckEmpty() ? this : new GameState(ticketDeck, cardState.withDeckRecreatedFromDiscards(rng),
                currentPlayerId(), Map.copyOf(playerStateMap), lastPlayer());
    }

    /**
     * @param playerId Identite du joueur,
     * @param chosenTickets les tickets choisis par le joueur,
     * @return un état identique au récepteur mais dans lequel
     * les billets donnés ont été ajoutés à la main du joueur donné,
     * @throws IllegalArgumentException si le joueur en question possède déjà au moins un billet.
     */
    public GameState withInitiallyChosenTickets(PlayerId playerId, SortedBag<Ticket> chosenTickets) {
        Preconditions.checkArgument(playerStateMap.get(playerId).tickets().isEmpty());
        Map<PlayerId, PlayerState> newMap = new EnumMap<>(Map.copyOf(playerStateMap));
        newMap.put(playerId, newMap.get(playerId).withAddedTickets(chosenTickets));
        return new GameState(ticketDeck, cardState, currentPlayerId(), newMap, lastPlayer());
    }

    /**
     * @param drawnTickets  tickets tire du sommet de la pioche,
     * @param chosenTickets les tickets que le joueur a choisi de garder,
     * @return un état identique au récepteur,
     * mais dans lequel le joueur courant a tiré les billets drawnTickets du sommet de la pioche,
     * et choisi de garder ceux contenus dans chosenTicket.
     * @throws IllegalArgumentException si l'ensemble des billets gardés n'est pas inclus dans celui des billets tirés.
     */
    public GameState withChosenAdditionalTickets(SortedBag<Ticket> drawnTickets, SortedBag<Ticket> chosenTickets) {
        Preconditions.checkArgument(drawnTickets.contains(chosenTickets));
        PlayerState player = currentPlayerState().withAddedTickets(chosenTickets);
        Map<PlayerId, PlayerState> newMap = new EnumMap<>(playerStateMap);
        newMap.put(currentPlayer, player);
        return new GameState(ticketDeck.withoutTopCards(drawnTickets.size()), cardState, currentPlayer,
                newMap, lastPlayer);
    }

    /**
     * @param slot emplacement de la carte face retournée,
     * @return un état identique au récepteur
     * si ce n'est que la carte face retournée à l'emplacement donné
     * a été placée dans la main du joueur courant, et remplacée par celle au sommet de la pioche,
     * @throws IllegalArgumentException s'il n'est pas possible de tirer des cartes.
     */
    public GameState withDrawnFaceUpCard(int slot) {
        Map<PlayerId, PlayerState> stateMap = new EnumMap<>(playerStateMap);
        stateMap.put(currentPlayerId(),
                stateMap.get(currentPlayerId()).withAddedCard(cardState().faceUpCard(slot)));
        CardState newCardState = cardState.withDrawnFaceUpCard(slot);
        return new GameState(ticketDeck, newCardState, currentPlayerId(), stateMap, lastPlayer());
    }

    /**
     * @return un état identique au récepteur
     * si ce n'est que la carte du sommet de la pioche a été placée dans la main du joueur courant,
     * @throws IllegalArgumentException s'il n'est pas possible de tirer des cartes.
     */
    public GameState withBlindlyDrawnCard() {
        Map<PlayerId, PlayerState> newMap = new EnumMap<>(playerStateMap);
        newMap.put(currentPlayerId(),
                newMap.get(currentPlayerId()).withAddedCard(cardState.topDeckCard()));
        return new GameState(ticketDeck, cardState.withoutTopDeckCard(), currentPlayerId(), newMap, lastPlayer());
    }

    /**
     * @param route route choisis par le joueur,
     * @param cards cartes utilisées pour s'emparer de la route,
     * @return un état identique au récepteur mais dans lequel le joueur courant
     * s'est emparé de la route donnée au moyen des cartes données.
     */
    public GameState withClaimedRoute(Route route, SortedBag<Card> cards) {
        Map<PlayerId, PlayerState> newMap = new EnumMap<>(playerStateMap);
        newMap.put(currentPlayerId(),
                newMap.get(currentPlayerId()).withClaimedRoute(route, cards));
        return new GameState(ticketDeck, cardState.withMoreDiscardedCards(cards), currentPlayerId(), newMap, lastPlayer());
    }

    /**
     * @return vrai ssi le dernier tour commence,
     * cette méthode doit être appelée uniquement à la fin du tour d'un joueur.
     */
    public boolean lastTurnBegins() {
        return (playerStateMap.get(currentPlayer).carCount() <= 2);
    }

    /**
     * @return qui termine le tour du joueur courant,
     * c-a-d retourne un état identique au récepteur
     * si ce n'est que le joueur courant est celui qui suit le joueur courant actuel.
     * de plus, si lastTurnBegins retourne vrai, le joueur courant actuel devient le dernier joueur.
     */
    public GameState forNextTurn() {
        return lastTurnBegins() ?
                new GameState(ticketDeck, cardState, currentPlayerId().next(), playerStateMap, currentPlayer) :
                new GameState(ticketDeck, cardState, currentPlayerId().next(), playerStateMap, lastPlayer);

    }

    //    ########################################### EXTENSIONS #################################################

    public GameState withDestructedRoute(Route route) {
        Map<PlayerId, PlayerState> newMap = new EnumMap<>(playerStateMap);
        newMap.put(currentPlayerId(), newMap.get(currentPlayerId()).withoutCard(Card.BOMB));
        newMap.put(currentPlayerId().next(), newMap.get(currentPlayerId().next()).withDestructedRoute(route));
        return new GameState(ticketDeck, cardState.withMoreDiscardedCards(SortedBag.of(Card.BOMB)), currentPlayerId(), newMap, lastPlayer());
    }
}
package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * classe représentant l'état publique du jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public class PublicGameState {

    private final int ticketDeckCount;
    private final PublicCardState cardState;
    private final PlayerId currentPlayerID;
    private final PlayerId lastPlayer;
    private final Map<PlayerId, PublicPlayerState> playerState;


    /**
     * construit un état publique du jeu.
     *
     * @param ticketsCount    taille de la pioche de billets
     * @param cardState       etat public des cartes wagon/locomotive
     * @param currentPlayerId le joueur courant
     * @param playerState     etat public des joueurs
     * @param lastPlayer      identite du dernier joueur
     */
    public PublicGameState(int ticketsCount, PublicCardState cardState, PlayerId currentPlayerId,
                           Map<PlayerId, PublicPlayerState> playerState, PlayerId lastPlayer) {
        Preconditions.checkArgument(ticketsCount >= 0 && playerState.size() == PlayerId.COUNT);
        this.ticketDeckCount = ticketsCount;
        this.cardState = Objects.requireNonNull(cardState);
        this.currentPlayerID = Objects.requireNonNull(currentPlayerId);
        this.lastPlayer = lastPlayer;
        this.playerState = Map.copyOf(playerState);
    }

    /**
     * @return la taille de la pioche de billets
     */
    public int ticketsCount() {
        return ticketDeckCount;
    }

    /**
     * @return true ssi il est possible de tirer des billets, c-a-d si la pioche n'est pas vide
     */
    public boolean canDrawTickets() {
        return ticketsCount() > 0;
    }

    /**
     * @return la partie publique de l'etat des cartes wagon/locomotive
     */
    public PublicCardState cardState() {
        return cardState;
    }

    /**
     * @return true si la somme de la taille de la pioche de carte et de la taille de la defausse est superieure ou egale a 5
     */
    public boolean canDrawCards() {
        return (cardState.deckSize() + cardState().discardsSize() >= Constants.FACE_UP_CARDS_COUNT);
    }

    /**
     * @return identite du joueur actuel
     */
    public PlayerId currentPlayerId() {
        return currentPlayerID;
    }

    /**
     * @param playerId identite du joueur dont nous voulons la partie publique
     * @return retourne la partie publique de l'etat du joueur d'identite donnee
     */
    public PublicPlayerState playerState(PlayerId playerId) {
        return playerState.get(playerId);
    }

    /**
     * @return la partie publique de l'etat du joueur courant
     */
    public PublicPlayerState currentPlayerState() {
        return playerState.get(currentPlayerID);
    }

    /**
     * @return la totalite des routes dont l'un ou l'autre des joueurs s'est empare
     */
    public List<Route> claimedRoutes() {
        List<Route> routes = new ArrayList<>();
        playerState.forEach((pId, pps) -> routes.addAll(pps.routes()));
        return routes;
    }

    /**
     * @return retourne l'identite du dernier joueur, ou null si elle n'est pas encore connue car le dernier tour n'a pas commence
     */
    public PlayerId lastPlayer() {
        return lastPlayer;
    }


    //    #################################################### EXTENSIONS #################################################

    public boolean routeIsDestroyableFor(PlayerId id, Route route) {
        return playerState.get(id.next()).routes().contains(route);
    }

}

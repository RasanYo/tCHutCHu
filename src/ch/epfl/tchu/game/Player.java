package ch.epfl.tchu.game;

import ch.epfl.tchu.SortedBag;

import java.util.List;
import java.util.Map;

/**
 *
 * interface représentant les différents actions/info que le joueur peut effectuer/recevoir
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public interface Player {
    enum TurnKind {
        //représente un tour durant lequel le joueur tire des billets.
        DRAW_TICKETS,
        //représente un tour durant lequel le joueur tire des cartes wagon/locomotive.
        DRAW_CARDS,
        //représente un tour durant lequel le joueur s'empare d'une route (ou tente en tout cas de le faire).
        CLAIM_ROUTE,

        DESTROY_ROUTE;


        public static final List<TurnKind> ALL = List.of(values());

    }
    /**
     *
     * @param ownId l'identité du joueur,
     * @param playerNames ensemble des identités de tout les joueurs,
     * cette méthode est appelée au début de la partie pour communiquer au joueur sa propre identité ownId,
     * ainsi que les noms des différents joueurs,
     *
     */
    void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames);

    /**
     *
     * @param info une information sous forme de chaine de caracteres,
     * est appelée chaque fois qu'une information doit être communiquée au joueur au cours de la partie.
     */
    void receiveInfo(String info);

    /**
     *
     * @param newState état public du jeu,
     * @param ownState état privée du joueur.
     * est appelée chaque fois que l'état du jeu a changé,
     * pour informer le joueur le nouvel état de jeu,
     * ainsi que de son propre état.
     */
    void updateState(PublicGameState newState, PlayerState ownState);

    /**
     *
     * @param tickets les tickets du joueur, qui est appelée au début de la partie pour communiquer au joueur les
     *                cinq billets qui lui ont été distribués.
     */
    void setInitialTicketChoice(SortedBag<Ticket> tickets);

    /**
     *
     * @return  qui est appelée au début de la partie pour demander au joueur lesquels des billets qu'on lui a
     *          distribué initialement.
     */
    SortedBag<Ticket> chooseInitialTickets();

    /**
     *
     * @return  qui est appelée au début du tour d'un joueur, pour savoir quel type d'action il désire effectuer
     *          durant ce tour
     */
    TurnKind nextTurn();

    /**
     *
     * @param options
     * @return  qui est appelée lorsque le joueur a décidé de tirer des billets supplémentaires en cours de partie,
     *          afin de lui communiquer les billets tirés et de savoir lesquels il garde.
     */
    SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options);

    /**
     *
     * @return  qui est appelée lorsque le joueur a décidé de tirer des cartes wagon/locomotive,
     *          afin de savoir d'où il désire les tirer: d'un des emplacements contenant une carte
     *          face visible auquel cas la valeur retourne est comprise entre 0 et 4 inclus —, ou
     *          de la pioche, auquel cas la valeur retournée vaut Constants.DECK_SLOT (c-à-d -1),
     */
    int drawSlot();

    /**
     *
     * @return  qui est appelée lorsque le joueur a décidé de (tenter de) s'emparer d'une route,
     *          afin de savoir de quelle route il s'agit
     */
    Route claimedRoute();

    /**
     *
     * @return  qui est appelée lorsque le joueur a décidé de (tenter de) s'emparer d'une route,
     *          afin de savoir quelle(s) carte(s) il désire initialement utiliser pour cela,
     */
    SortedBag<Card> initialClaimCards();

    /**
     *
     * @param options liste des ensemble de cartes que le joueur peut utilisé.
     * @return  qui est appelée lorsque le joueur a décidé de tenter de s'emparer d'un tunnel
     *          et que des cartes additionnelles sont nécessaires,
     *          afin de savoir quelle(s) carte(s) il désire utiliser pour cela,
     *          les possibilités lui étant passées en argument; si le multiensemble retourné est vide,
     *          cela signifie que le joueur ne désire pas (ou ne peut pas) choisir l'une de ces possibilités.
     */
    SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options);

    //    ############################################### EXTENSIONS #################################################

    String getName();

    Route destroyedRoute();

}

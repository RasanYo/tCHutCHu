package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.application.Platform;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * classe permettant d'adapter le joueur graphique en un type Player.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class GraphicalPlayerAdapter implements Player {


    private GraphicalPlayer graphicalPlayer;
    private final BlockingQueue<TurnKind> turnQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<SortedBag<Ticket>> ticketQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Route> routeQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<SortedBag<Card>> cardsQueue = new ArrayBlockingQueue<>(1);
    private final BlockingQueue<Integer> drawSlotQueue = new ArrayBlockingQueue<>(1);

    private final BlockingQueue<String> nameQueue = new ArrayBlockingQueue<>(1);


    /**
     *
     * @param ownId l'identité du joueur,
     * @param playerNames ensemble des identités de tout les joueurs,
     * cette méthode est appelée au début de la partie pour communiquer au joueur sa propre identité ownId,
     */
    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        Platform.runLater(() -> {
            try {
                graphicalPlayer = new GraphicalPlayer(ownId, playerNames);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     *
     * @param info une information sous forme de chaine de caracteres,
     */
    @Override
    public void receiveInfo(String info) {
        Platform.runLater(() -> graphicalPlayer.receiveInfo(info));
    }

    /**
     *
     * @param newState état public du jeu,
     * @param ownState état privée du joueur.
     * est appelée chaque fois que l'état du jeu a changé,
     * pour informer le joueur le nouvel état de jeu,
     */
    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        Platform.runLater(() -> graphicalPlayer.setState(newState, ownState));
    }

    /**
     *
     * @param tickets les tickets du joueur, qui est appelée au début de la partie pour
     *                communiquer au joueur les cinq billets qui lui ont été distribués.
     */
    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        Platform.runLater(() -> graphicalPlayer.chooseTickets(tickets, ticketQueue::add));
    }

    /**
     *
     * @return le multiensemble de billets initials choisis par le joueur, retire de la file bloquante
     */
    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        return retrieveFromQueue(ticketQueue);
    }

    /**
     *
     * @return  le type d'action de jeu que veut effectuer le joueurs, en le retirant de la file bloquante apres
     *          l'y avoir introduit
     */
    @Override
    public TurnKind nextTurn() {
        Platform.runLater(() -> {
            ActionHandlers.DrawTicketsHandler drawTicketsHandler = () ->
                turnQueue.add(TurnKind.DRAW_TICKETS);

            ActionHandlers.DrawCardHandler drawCardHandler = slot -> {
                turnQueue.add(TurnKind.DRAW_CARDS);
                drawSlotQueue.add(slot);
            };

            ActionHandlers.ClaimRouteHandler claimRouteHandler = (r, sb) -> {
                turnQueue.add(TurnKind.CLAIM_ROUTE);
                routeQueue.add(r);
                cardsQueue.add(sb);
            };

            ActionHandlers.DestroyRouteHandler destroyRouteHandler = r -> {
                turnQueue.add(TurnKind.DESTROY_ROUTE);
                routeQueue.add(r);

            };

            graphicalPlayer.startTurn(drawTicketsHandler, drawCardHandler, claimRouteHandler, destroyRouteHandler);
        });
        return retrieveFromQueue(turnQueue);
    }

    /**
     *
     * @param options multiensemble de billets disponibles a la selection
     * @return le multiensemble de billets choisis par le joueur
     * @throws Error
     */
    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) throws Error {
        setInitialTicketChoice(options);
        return chooseInitialTickets();
    }

    /**
     * Methode qui ajoute a la file bloquant l'indice de la carte choisie (0 a 4 pour les cartes face visible et -1
     * pour la pioche) et la retire ensuite (effectue l'action deux fois)
     * @return l'indice de l'emplacement de la carte choisie (0 a 4 pour les cartes face visible et -1 pour la pioche
     */
    @Override
    public int drawSlot() {
        if (drawSlotQueue.isEmpty()) {
            Platform.runLater(() -> graphicalPlayer.drawCard(drawSlotQueue::add)); // peut mettre put a la place add
        }
        return retrieveFromQueue(drawSlotQueue);

    }

    /**
     * Methode qui prend de la file bloquante la route dont le joueur s'empare
     * @return la route dont le joueur s'empare
     */
    @Override
    public Route claimedRoute() {
        return retrieveFromQueue(routeQueue);
    }

    /**
     *
     * @return  le multiensemble de cartes utilise pour s'emparer d'une route (claimedRoute), tire de la file
     *          bloquante
     */
    @Override
    public SortedBag<Card> initialClaimCards(){
        return retrieveFromQueue(cardsQueue);
    }

    /**
     *
     * @param options liste des multiensemble de cartes additionnelles que le joueur peut utilise pour s'emparer
     *                d'un tunnel
     * @return le multiensemble de cartes choisi, tire de la file
     */
    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) throws Error {
        Platform.runLater(() -> graphicalPlayer.chooseAdditionalCards(options, cardsQueue::add));
        return retrieveFromQueue(cardsQueue);
    }



    private <T> T retrieveFromQueue(BlockingQueue<T> queue) {
        try {
            return queue.take();
        }catch (InterruptedException e) {
            throw new Error();
        }
    }

//    ################################################ EXTENSIONS ##################################################


    @Override
    public String getName() {
        Platform.runLater(() -> GraphicalPlayer.getName(nameQueue::add));
        return retrieveFromQueue(nameQueue);
    }

    @Override
    public Route destroyedRoute() {
        return retrieveFromQueue(routeQueue);
    }


}

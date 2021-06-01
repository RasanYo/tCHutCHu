package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * classe représentant un mandataire.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class RemotePlayerProxy implements Player {

    BufferedWriter writer;
    BufferedReader reader;

    public RemotePlayerProxy(Socket socket) throws IOException {
        this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.US_ASCII));
        this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));

    }

    /**
     * Methode qui va ecrire dans le flot de sortie de la "prise" d'abord l'identite du message
     * suivi des informations qu'on veut communiquer, tous separees par des espaces
     * @param id identite du message
     * @param args les informations serialisees qu'on veut communiquer
     */
    public void sendMessage(MessageId id, String...args) {
        List<String> messages = new ArrayList<>();
        messages.add(id.name());
        messages.addAll(Arrays.asList(args));
        try {
            writer.write(String.join(" ", messages));
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Methode qui va lire la premiere ligne du flot d'entree de la "prise"
     * @return le message lu du flot d'entree jusqu'au premier retour a la ligne
     */
    public String receiveMessage() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    /**
     * Cette methode est appelee au debut de la partie pour communiquer au joueur sa propre identite ownId,
     * ainsi que les noms des différents joueurs.
     * En l'occurence, le proxy va communiquer ces informations aux clients a travers le flot de sortie
     * @param ownId l'identite du joueur,
     * @param playerNames ensemble des identites de tout les joueurs,
     */
    @Override
    public void initPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        sendMessage(MessageId.INIT_PLAYERS,
                Serdes.ONE_OF_PLAYER_ID.serialize(ownId),
                Serdes.LIST_OF_STRING.serialize(List.of(playerNames.get(PlayerId.PLAYER_1), playerNames.get(PlayerId.PLAYER_2))));
    }

    /**
     * Methode appelee chaque fois qu'une information doit etre communiquee au joueur au cours de la partie.
     * En l'occurence, le proxy va communiquer ces informations aux clients a travers le flot de sortie
     * @param info une information sous forme de chaine de caracteres,
     */
    @Override
    public void receiveInfo(String info) {
        sendMessage(MessageId.RECEIVE_INFO, Serdes.STRING_SERDE.serialize(info));
    }

    /**
     * Methode appelee chaque fois que l'état du jeu a change
     * pour informer le joueur le nouvel etat de jeu.
     * En l'occurence, le proxy va communiquer ces informations aux clients a travers le flot de sortie
     * @param newState état public du jeu,
     * @param ownState état privée du joueur.
     * ,
     */
    @Override
    public void updateState(PublicGameState newState, PlayerState ownState) {
        sendMessage(MessageId.UPDATE_STATE,
                Serdes.OF_PUBLIC_GAME_STATE.serialize(newState),
                Serdes.OF_PLAYER_STATE.serialize(ownState));
    }

    /**
     * Methode appelee au debut de la partie pour communiquer au joueur les cinq billets qui lui ont ete distribues.
     * En l'occurence, le proxy va communiquer ces informations aux clients a travers le flot de sortie
     * @param tickets les tickets du joueur,
     */
    @Override
    public void setInitialTicketChoice(SortedBag<Ticket> tickets) {
        sendMessage(MessageId.SET_INITIAL_TICKETS, Serdes.BAG_OF_TICKET.serialize(tickets));
        System.out.println(tickets);
    }

    /**
     * Methode appelee au debut de la partie pour demander au client a travers le flot de sortie quels billets
     * le joueur distant souhaite garder.
     * @return le multiensemble de billets proposes en debut de partie que le joueur a decide de garder,
     * communique par le client a travers le flot d'entree
     */
    @Override
    public SortedBag<Ticket> chooseInitialTickets() {
        sendMessage(MessageId.CHOOSE_INITIAL_TICKETS);
        return Serdes.BAG_OF_TICKET.deserialize(receiveMessage());
    }

    /**
     * Methode appelee au debut du tour du joueur distant, pour demander au client quelle action
     * il desire effectuer
     * @return l'action que veut effectuer le joueur communique par le client a travers le flot d'entree
     */
    @Override
    public TurnKind nextTurn() {
        sendMessage(MessageId.NEXT_TURN);
        return Serdes.ONE_OF_TURN_KIND.deserialize(receiveMessage());
    }

    /**
     * Methode appelee pour proposer au client un multiensemble de cartes a parmi lequel le joueur distant peut
     * choisir d'en garder s'il a communique le desir de tirer des billets
     * @param options multiensemble de billets parmi lequel le joueur peut choisir
     * @return le multiensemble de billet que le joueur distant a choisi de garder, communique par
     * le client a travers le flot d'entree
     */
    @Override
    public SortedBag<Ticket> chooseTickets(SortedBag<Ticket> options) {
        sendMessage(MessageId.CHOOSE_TICKETS, Serdes.BAG_OF_TICKET.serialize(options));
        return Serdes.BAG_OF_TICKET.deserialize(receiveMessage());
    }

    /**
     * Methode qui est appelee pour demander au client lorsque le joueur a decide de tirer des cartes
     * wagon/locomotive, afin de savoir d'ou il désire les tirer: d'un des emplacements contenant
     * une carte face visible auquel cas la valeur retourne est comprise entre 0 et 4 inclus —,
     * ou de la pioche, auquel cas la valeur retournee vaut Constants.DECK_SLOT (c-a-d -1),
     * @return l'emplacement que le joueur a choisi, communique par le client a travers le flot
     * d'entree
     */
    @Override
    public int drawSlot() {
        sendMessage(MessageId.DRAW_SLOT);
        return Serdes.INTEGER_SERDE.deserialize(receiveMessage());
    }

    /**
     * Methode qui est appelee pour demander au client lorsque le joueur a decide de
     * (tenter de) s'emparer d'une route, afin de savoir de quelle route il s'agit
     * @return la route que le joueur distant veut tenter de s'emparer, communique par le client
     * a travers le flot d'entree
     */
    @Override
    public Route claimedRoute() {
        sendMessage(MessageId.ROUTE);
        return Serdes.ONE_OF_ROUTE.deserialize(receiveMessage());
    }

    /**
     * Methode qui est appelee pour demander au client lorsque le joueur a decide de (tenter de)
     * s'emparer d'une route, afin de savoir quelle(s) carte(s) il desire initialement utiliser
     * pour cela.
     * @return le multiensemble de cartes que le joueur distant veut utiliser pour s'emparer
     * de la route desiree (communique par le client a travers le flot d'entree)
     */
    @Override
    public SortedBag<Card> initialClaimCards() {
        sendMessage(MessageId.CARDS);
        return Serdes.BAG_OF_CARD.deserialize(receiveMessage());
    }

    /**
     * Methode qui est appelee pour proposer au client les options d'ensembles de cartes jouables
     * lorsque le joueur distant a desire de s'emparer d'un tunnel
     * @param options liste des multiensembles de cartes que le joueur peut utiliser.
     * @return le multiensemble que le joueur distant veut utiliser (communique par le client
     * a travers le flot d'entree)
     */
    @Override
    public SortedBag<Card> chooseAdditionalCards(List<SortedBag<Card>> options) {
        sendMessage(MessageId.CHOOSE_ADDITIONAL_CARDS, Serdes.LIST_OF_CARD_BAGS.serialize(options));
        return Serdes.BAG_OF_CARD.deserialize(receiveMessage());
    }

//    ############################################## EXTENSIONS ###################################################


    @Override
    public String getName() {
        sendMessage(MessageId.SEND_NAME);
        return Serdes.STRING_SERDE.deserialize(receiveMessage());
    }

    @Override
    public Route destroyedRoute() {
        sendMessage(MessageId.DESTROY_ROUTE);
        return Serdes.ONE_OF_ROUTE.deserialize(receiveMessage());
    }


}

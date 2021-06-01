package ch.epfl.tchu.net;

import ch.epfl.tchu.game.*;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static ch.epfl.tchu.game.PlayerId.PLAYER_1;
import static ch.epfl.tchu.game.PlayerId.PLAYER_2;
import static java.nio.charset.StandardCharsets.US_ASCII;

/**
 * classe représentant le client de joueur distant.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class RemotePlayerClient {

    private final Player player;
    private final String name;
    private final int port;

    /**
     * représente un client de joueur distant.
     *
     * @param player joueur auquel on doit fournir un accès distant,
     * @param name   nom du joueur,
     * @param port   port à utiliser pour se connecter au mandataire.
     */
    public RemotePlayerClient(Player player, String name, int port) {
        this.player = player;
        this.name = name;
        this.port = port;
    }

    /**
     * méthode utiliser pour répondre au message du mandataire (RemotePlayerProxy).
     * elle effectue une boucle qui attend un message du mandataire (sérialiser),
     * la découpe par un espace,
     * détermine le type du message (MessageId) en fonction de la première chaîne de caractères du découpage,
     * puis en fonction du message, désérialise les arguments et appel la méthode correspondante du joueur (player),
     *
     * est utilisé pour répondre au message du mandataire (RemotePlayerProxy).
     */
    public void run() {
        try (Socket s = new Socket(name, port);
             BufferedReader r =
                     new BufferedReader(
                             new InputStreamReader(s.getInputStream(),
                                     US_ASCII));

             BufferedWriter w =
                     new BufferedWriter(
                             new OutputStreamWriter(s.getOutputStream(),
                                     US_ASCII))) {
            String receivedString;

            while((receivedString=r.readLine()) != null) {
                String[] str = receivedString.split(Pattern.quote(" "), -1);
                switch (MessageId.valueOf(str[0])) {

                    case INIT_PLAYERS:
                        List<String> playerNames = Serdes.LIST_OF_STRING.deserialize(str[2]);
                        player.initPlayers(PlayerId.ALL.get(Integer.parseInt(str[1])),
                                Map.of(PLAYER_1, playerNames.get(0), PLAYER_2,
                                        playerNames.get(1)));
                        break;

                    case RECEIVE_INFO:
                        player.receiveInfo(Serdes.STRING_SERDE.deserialize(str[1]));
                        break;

                    case UPDATE_STATE:
                        player.updateState(Serdes.OF_PUBLIC_GAME_STATE.deserialize(str[1]),
                                Serdes.OF_PLAYER_STATE.deserialize(str[2]));
                        break;

                    case SET_INITIAL_TICKETS:
                        player.setInitialTicketChoice(Serdes.BAG_OF_TICKET.deserialize(str[1]));
                        break;

                    case CHOOSE_INITIAL_TICKETS:
                        write(w, Serdes.BAG_OF_TICKET.serialize(player.chooseInitialTickets()));
                        break;

                    case NEXT_TURN:
                        write(w, Serdes.ONE_OF_TURN_KIND.serialize(player.nextTurn()));
                        break;

                    case CHOOSE_TICKETS:
                        write(w,
                                Serdes.BAG_OF_TICKET
                                        .serialize(player
                                                .chooseTickets(Serdes.BAG_OF_TICKET
                                                        .deserialize(str[1]))));
                        break;

                    case DRAW_SLOT:
                        write(w, Serdes.INTEGER_SERDE.serialize(player.drawSlot()));
                        break;

                    case ROUTE:
                        write(w, Serdes.ONE_OF_ROUTE.serialize(player.claimedRoute()));
                        break;

                    case CARDS:
                        write(w, Serdes.BAG_OF_CARD.serialize(player.initialClaimCards()));
                        break;

                    case CHOOSE_ADDITIONAL_CARDS:
                        write(w,
                                Serdes.BAG_OF_CARD
                                        .serialize(player
                                                .chooseAdditionalCards(Serdes.LIST_OF_CARD_BAGS
                                                        .deserialize(str[1]))));
                        break;

                    case SEND_NAME:
                        write(w, Serdes.STRING_SERDE.serialize(player.getName()));
                        break;

                    case DESTROY_ROUTE:
                        write(w, Serdes.ONE_OF_ROUTE.serialize(player.destroyedRoute()));
                        break;

                    default:
                        throw new IllegalArgumentException();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void write(BufferedWriter w, String message) throws IOException {
        w.write(message);
        w.write('\n');
        w.flush();
    }
}

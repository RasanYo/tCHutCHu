package ch.epfl.tchu.net;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * classe contenant toute les serdes utilent au projet.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Serdes {

    private Serdes() {}

    /**
     * Serde utiliser pour (de)serialiser des messages composé d'un Integer
     */
    public static final Serde<Integer> INTEGER_SERDE = Serde.of(
            String::valueOf,
            Integer::parseInt
    );

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une String
     * encode en Base64
     */
    public static final Serde<String> STRING_SERDE = Serde.of(
            s -> Base64.getEncoder().encodeToString(s.getBytes()),
            str -> new String(Base64.getDecoder().decode(str))
    );

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'unme identite de joueur (PlayerId)
     */
    public static final Serde<PlayerId> ONE_OF_PLAYER_ID = Serde.oneOf(PlayerId.ALL);

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une action de jeu (TurnKind)
     */
    public static final Serde<Player.TurnKind> ONE_OF_TURN_KIND = Serde.oneOf(Player.TurnKind.ALL);

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une carte (Card)
     */
    public static final Serde<Card> ONE_OF_CARD = Serde.oneOf(Card.ALL);

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une route (Route)
     */
    public static final Serde<Route> ONE_OF_ROUTE = Serde.oneOf(ChMap.routes());

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un billet (Ticket)
     */
    public static final Serde<Ticket> ONE_OF_TICKET = Serde.oneOf(ChMap.tickets());

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une liste de Strings
     */
    public static final Serde<List<String>> LIST_OF_STRING = Serde.listOf(STRING_SERDE, ",");

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une liste de cartes
     */
    public static final Serde<List<Card>> LIST_OF_CARD = Serde.listOf(ONE_OF_CARD, ",");

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une liste de routes
     */
    public static final Serde<List<Route>> LIST_OF_ROUTE = Serde.listOf(ONE_OF_ROUTE, ",");

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un multiensemble de cartes
     */
    public static final Serde<SortedBag<Card>> BAG_OF_CARD = Serde.bagOf(ONE_OF_CARD, ",");

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un multiensemble de billets
     */
    public static final Serde<SortedBag<Ticket>> BAG_OF_TICKET = Serde.bagOf(ONE_OF_TICKET, ",");

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'une liste de multiensembles de cartes
     */
    public static final Serde<List<SortedBag<Card>>> LIST_OF_CARD_BAGS = Serde.listOf(BAG_OF_CARD, ";");


    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un etat de cartes public (PublicCardState)
     */
    public static final Serde<PublicCardState> OF_PUBLIC_CARD_STATE = Serde.of(
            pcs -> String.format("%s;%s;%s",
                    LIST_OF_CARD.serialize(pcs.faceUpCards()),
                    INTEGER_SERDE.serialize(pcs.deckSize()),
                    INTEGER_SERDE.serialize(pcs.discardsSize())),
            str -> {
                String[] serializedElements = str.split(Pattern.quote(";"), -1);
                return new PublicCardState (
                        LIST_OF_CARD.deserialize(serializedElements[0]),
                        INTEGER_SERDE.deserialize(serializedElements[1]),
                        INTEGER_SERDE.deserialize(serializedElements[2])
                    );
            }
    );

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un etat de joueurs public (PublicPlayerState)
     */
    public static final Serde<PublicPlayerState> OF_PUBLIC_PLAYER_STATE = Serde.of(

            pps -> String.format("%s;%s;%s",
                    INTEGER_SERDE.serialize(pps.ticketCount()),
                    INTEGER_SERDE.serialize(pps.cardCount()),
                    LIST_OF_ROUTE.serialize(pps.routes())),
            str -> {
                String[] serializedElements = str.split(Pattern.quote(";"), -1);
                return new PublicPlayerState(
                        INTEGER_SERDE.deserialize(
                                serializedElements[0]
                        ),
                        INTEGER_SERDE.deserialize(
                                serializedElements[1]
                        ),
                        LIST_OF_ROUTE.deserialize(
                                serializedElements[2])
                );
            }
    );

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un etat de joueur (PlayerState)
     */
    public static final Serde<PlayerState> OF_PLAYER_STATE = Serde.of(
            ps -> String.format("%s;%s;%s",
                    BAG_OF_TICKET.serialize(ps.tickets()),
                    BAG_OF_CARD.serialize(ps.cards()),
                    LIST_OF_ROUTE.serialize(ps.routes())),
            str -> {
                String[] serializedElements = str.split(Pattern.quote(";"), -1);
                return new PlayerState(
                        BAG_OF_TICKET.deserialize(
                                serializedElements[0]
                        ),
                        BAG_OF_CARD.deserialize(
                                serializedElements[1]
                        ),
                        LIST_OF_ROUTE.deserialize(
                                serializedElements[2])
                );
            }
    );

    /**
     * Serde utiliser pour (de)serialiser des messages composés d'un etat de jeu public (PublicGameState)
     */
    public static final Serde<PublicGameState> OF_PUBLIC_GAME_STATE = Serde.of(
            pgs -> String.format("%s:%s:%s:%s:%s:%s",
                    INTEGER_SERDE.serialize(pgs.ticketsCount()),
                    OF_PUBLIC_CARD_STATE.serialize(pgs.cardState()),
                    ONE_OF_PLAYER_ID.serialize(pgs.currentPlayerId()),
                    OF_PUBLIC_PLAYER_STATE.serialize(pgs.playerState(PlayerId.PLAYER_1)),
                    OF_PUBLIC_PLAYER_STATE.serialize(pgs.playerState(PlayerId.PLAYER_2)),
                    pgs.lastPlayer() == null ? "" : ONE_OF_PLAYER_ID.serialize(pgs.lastPlayer())),
            str -> {
                String[] serializedElements = str.split(Pattern.quote(":"), -1);
                return new PublicGameState(
                        INTEGER_SERDE.deserialize(
                                serializedElements[0]
                        ),
                        OF_PUBLIC_CARD_STATE.deserialize(
                                serializedElements[1]
                        ),
                        ONE_OF_PLAYER_ID.deserialize(
                                serializedElements[2]
                        ),
                        Map.of(
                                PlayerId.PLAYER_1,
                                OF_PUBLIC_PLAYER_STATE.deserialize(
                                        serializedElements[3]
                                ),
                                PlayerId.PLAYER_2,
                                OF_PUBLIC_PLAYER_STATE.deserialize(
                                        serializedElements[4]
                                )
                        ),
                        serializedElements[5].equals("") ? null :
                                ONE_OF_PLAYER_ID.deserialize(serializedElements[5])
                );
            }


    );
}

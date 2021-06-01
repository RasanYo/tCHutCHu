package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.gui.Info;
import ch.epfl.tchu.gui.StringsFr;

import java.util.*;

/**
 * Classe du jeu permettant de faire tourner le jeu.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class Game {

    private static Map<PlayerId, Player> playerMap;
    private static Map<PlayerId, String> playerNameMap;
    private static Map<PlayerId, Info> playerInfoMap;
    private static Player player1;
    private static Player player2;
    private static Info playerInfo1;
    private static Info playerInfo2;
    private static GameState gameState;

    private Game() {}

    /**
     * @param initialCards cartes utilisees pour s'emparer de la route
     * @param drawnCards   cartes piochees
     * @return nombre de cartes additionelles que le joueur doit jouer
     */
    private static int numberOfAdditionalCards(SortedBag<Card> initialCards, SortedBag<Card> drawnCards, Route route) {
        return route.level() == Route.Level.UNDERGROUND ?
                route.additionalClaimCardsCount(initialCards, drawnCards) :
                0;
    }

    /**
     * méthode pour faire tourner le jeu.
     *
     * @param players     liste des joueurs.
     * @param tickets     tickets que le joueur
     * @param rng         Random.
     */
    public static void play(Map<PlayerId, Player> players,
                            SortedBag<Ticket> tickets,
                            Random rng) {
        //check si les maps playerNames et players ont une taille de 2.
        Preconditions.checkArgument(players.size() == PlayerId.COUNT);

        boolean lastTurnBegins = false;
        playerMap = Map.copyOf(players);
        String player1Name = players.get(PlayerId.PLAYER_1).getName();
        String player2Name = players.get(PlayerId.PLAYER_2).getName();

        playerNameMap = Map.of(PlayerId.PLAYER_1, player1Name, PlayerId.PLAYER_2, player2Name);
        player1 = players.get(PlayerId.PLAYER_1);
        player2 = players.get(PlayerId.PLAYER_2);
        playerInfo1 = new Info(player1Name);
        playerInfo2 = new Info(player2Name);
        playerInfoMap = Map.of(PlayerId.PLAYER_1, playerInfo1, PlayerId.PLAYER_2, playerInfo2);
        gameState = GameState.initial(tickets, rng);
        //debut de la partie
        beginGame();

        while (!lastTurnBegins) {
            lastTurnBegins = playerTurn(rng);
        }

        //le dernier tour commence
        announceInfo(playerInfoMap.get(gameState.currentPlayerId().next())
                .lastTurnBegins(gameState.playerState(gameState.currentPlayerId().next()).carCount()));
        for (PlayerId id : PlayerId.ALL) {
            playerTurn(rng);
        }
        gameOver();
    }

    /**
     * met à jour le jeu pour les deux joueurs.
     */
    private static void update() {
        playerMap.keySet().forEach(p -> playerMap.get(p).updateState(gameState, gameState.playerState(p)));
    }

    /**
     * annonce les informations au joueurs.
     *
     * @param info information a communiquer
     */
    private static void announceInfo(String info) {
        playerMap.values().forEach(p -> p.receiveInfo(info));
    }

    /**
     * methode appelee pour que le joueur tire des billets
     *
     * @param currentPlayer     le joueur actuel
     * @param currentPlayerInfo instance Info liee au joueur
     */
    private static void drawTickets(Player currentPlayer,
                                    Info currentPlayerInfo) {
        announceInfo(currentPlayerInfo.drewTickets(Constants.IN_GAME_TICKETS_COUNT));
        SortedBag<Ticket> topTickets = gameState.topTickets(Constants.IN_GAME_TICKETS_COUNT);
        SortedBag<Ticket> chosenTickets = currentPlayer.chooseTickets(topTickets);
        gameState = gameState.
                withChosenAdditionalTickets(topTickets, chosenTickets);
        announceInfo(currentPlayerInfo.keptTickets(chosenTickets.size()));
    }

    /**
     * methode appelee pour que le joueur tire des cartes
     *
     * @param currentPlayer     joueur actuel
     * @param currentPlayerInfo instance Info liee au joueur
     * @param rng               generateur aleatoir
     */
    private static void drawCards(Player currentPlayer,
                                  Info currentPlayerInfo,
                                  Random rng) {
        gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);

        for (int i = 0; i < 2; ++i) {
            update();
            int cardSlot = currentPlayer.drawSlot();
            //le joueur tire une carte de la pioche
            if (cardSlot == Constants.DECK_SLOT) {
                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                gameState = gameState.withBlindlyDrawnCard();
                announceInfo(currentPlayerInfo.drewBlindCard());
            }
            //le joueur choisit parmi les cartes faces ouvertes
            else {
                gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
                Card card = gameState.cardState().faceUpCard(cardSlot);
                gameState = gameState.withDrawnFaceUpCard(cardSlot);
                announceInfo((currentPlayerInfo.drewVisibleCard(card)));
            }

        }
    }

    /**
     * methode appelee pour que le joueur s'empare d'une route
     *
     * @param currentPlayer     joueur actuel
     * @param currentPlayerInfo instance Info liee au joueur
     * @param rng               generateur aleatoir
     */
    private static void claimRoute(Route route,
                                   Player currentPlayer,
                                   Info currentPlayerInfo,
                                   Random rng) {
        SortedBag<Card> initialClaimCards = currentPlayer.initialClaimCards();
        List<Card> topDeckCards = new ArrayList<>();
        gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);

        if(route.level() == Route.Level.UNDERGROUND){
            claimUndergroundRoute(topDeckCards, rng, initialClaimCards, route, currentPlayerInfo, currentPlayer);
        } else {
            claimOvergroundRoute(route, initialClaimCards, currentPlayerInfo);
        }
    }
    private static void claimOvergroundRoute(Route route,
                                             SortedBag<Card> initialClaimCards,
                                             Info currentPlayerInfo){
        gameState = gameState.withClaimedRoute(route, initialClaimCards);
        announceInfo(currentPlayerInfo.claimedRoute(route, initialClaimCards));
    }
    private static void claimUndergroundRoute(List<Card> topDeckCards,
                                              Random rng,
                                              SortedBag<Card> initialClaimCards,
                                              Route route,
                                              Info currentPlayerInfo,
                                              Player currentPlayer) {

        //sequence pour obtenir les 3 cartes du sommet de la pioche
        gameState = gameState.withMoreDiscardedCards(SortedBag.of());
        for (int i = 0; i < Constants.ADDITIONAL_TUNNEL_CARDS; ++i) {
            topDeckCards.add(gameState.topCard());
            gameState = gameState.withoutTopCard();
            gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);
        }
        gameState = gameState.withMoreDiscardedCards(initialClaimCards);


        SortedBag<Card> drawnCards = SortedBag.of(topDeckCards);
        int additionalCardsNumber = numberOfAdditionalCards(initialClaimCards, SortedBag.of(topDeckCards), route);

        announceInfo(currentPlayerInfo.attemptsTunnelClaim(route, initialClaimCards));
        announceInfo(currentPlayerInfo.drewAdditionalCards(drawnCards, additionalCardsNumber));
        if (additionalCardsNumber > 0) {
            gameState = gameState.withCardsDeckRecreatedIfNeeded(rng);

            List<SortedBag<Card>> possibleAdditionalCards = gameState.playerState(gameState.currentPlayerId()).
                    possibleAdditionalCards(additionalCardsNumber, initialClaimCards);//a appele que si possible additional card est>0.
            if (!possibleAdditionalCards.isEmpty()) {
                SortedBag<Card> additionalCards = currentPlayer.chooseAdditionalCards(possibleAdditionalCards);

                if (!additionalCards.isEmpty()) {
                    SortedBag<Card> totalCardsToPlay = initialClaimCards.union(additionalCards);
                    gameState = gameState.withClaimedRoute(route,
                            totalCardsToPlay);
                    announceInfo(currentPlayerInfo.claimedRoute(route,
                            totalCardsToPlay));
                } else {
                    announceInfo(currentPlayerInfo.didNotClaimRoute(route));
                }
            } else {
                announceInfo(currentPlayerInfo.didNotClaimRoute(route));
            }
        }else{
            claimOvergroundRoute(route, initialClaimCards, currentPlayerInfo);
        }
    }

    /**
     * methode représentant un tour effectuer par le joueur courant.
     * Il peut choisir de tirer des billets, des cartes ou de tenter de
     * s'emparer d'une route
     *
     * @param rng generateur aleatoir
     */
    private static boolean playerTurn(Random rng) {
        Player currentPlayer = playerMap.get(gameState.currentPlayerId());
        Info currentPlayerInfo = new Info(playerNameMap.get(gameState.currentPlayerId()));

        update();
        announceInfo(currentPlayerInfo.canPlay());
        Player.TurnKind nextTurn = currentPlayer.nextTurn();

        switch (nextTurn) {

            case DRAW_TICKETS:
                drawTickets(currentPlayer, currentPlayerInfo);
                break;

            case DRAW_CARDS:
                drawCards(currentPlayer, currentPlayerInfo, rng);
                break;

            case CLAIM_ROUTE:
                claimRoute(currentPlayer.claimedRoute(), currentPlayer, currentPlayerInfo, rng);
                break;

            case DESTROY_ROUTE:
                destroyRoute(currentPlayer.destroyedRoute(), currentPlayerInfo);

        }
        update();

        boolean lastTurnBegin = gameState.lastTurnBegins();
        gameState = gameState.forNextTurn();
        return lastTurnBegin;
    }

    /**
     * methode appele pour débuter une partie:
     *  - initialise les joueurs
     *  - les fait choisir leur tickets de depart
     *  - determine qui va jouer en premier
     */
    private static void beginGame() {
        //communique a chaque joueur son id + noms des joueurs.

        playerMap.keySet().forEach(p -> playerMap.get(p).initPlayers(p, playerNameMap));

        Info currentPlayerInfo = new Info(playerNameMap.get(gameState.currentPlayerId()));

        announceInfo(currentPlayerInfo.willPlayFirst());
        //communique aux deux joueurs les tickets distribuer.
        playerMap.values().forEach(p -> {
            p.setInitialTicketChoice(gameState.topTickets(Constants.INITIAL_TICKETS_COUNT));
            gameState = gameState.withoutTopTickets(Constants.INITIAL_TICKETS_COUNT);
        });

        update();

        //tickets choisie par les joueurs
        SortedBag<Ticket> t1 = player1.chooseInitialTickets();
        SortedBag<Ticket> t2 = player2.chooseInitialTickets();
        gameState = gameState.withInitiallyChosenTickets(PlayerId.PLAYER_1, t1).
                withInitiallyChosenTickets(PlayerId.PLAYER_2, t2);

        announceInfo(playerInfo1.keptTickets(t1.size()));
        announceInfo(playerInfo2.keptTickets(t2.size()));



        update();

    }

    /**
     * méthode appelé lors de la fin de partie
     */
    private static void gameOver() {
        Trail trailPlayer1 = Trail.longest(gameState.playerState(PlayerId.PLAYER_1).routes());
        Trail trailPlayer2 = Trail.longest(gameState.playerState(PlayerId.PLAYER_2).routes());
        int fPoints1 = gameState.playerState(PlayerId.PLAYER_1).finalPoints();
        int fPoints2 = gameState.playerState(PlayerId.PLAYER_2).finalPoints();

        if (trailPlayer1.length() > trailPlayer2.length()) {
            announceInfo(playerInfo1.getsLongestTrailBonus(trailPlayer1));
            fPoints1 += Constants.LONGEST_TRAIL_BONUS_POINTS;
        } else if (trailPlayer1.length() < trailPlayer2.length()) {
            announceInfo(playerInfo2.getsLongestTrailBonus(trailPlayer2));
            fPoints2 += Constants.LONGEST_TRAIL_BONUS_POINTS;
        } else {
            announceInfo(playerInfo1.getsLongestTrailBonus(trailPlayer1));
            fPoints1 += Constants.LONGEST_TRAIL_BONUS_POINTS;
            announceInfo(playerInfo2.getsLongestTrailBonus(trailPlayer2));
            fPoints2 += Constants.LONGEST_TRAIL_BONUS_POINTS;
        }

        //si player 1 a le meme nombre de points que player 2.
        if (fPoints1 > fPoints2) {
            announceInfo(playerInfo1.won(fPoints1, fPoints2));
        }//si player1 a moins de points que player2.
        else if (fPoints1 < fPoints2) {
            announceInfo(playerInfo2.won(fPoints2, fPoints1));
        }//si player 1 a plus de points que player 2.
        else {
            List<String> playerNames2 = List.of(playerNameMap.get(PlayerId.PLAYER_1), playerNameMap.get(PlayerId.PLAYER_2));
            announceInfo(Info.draw(playerNames2, fPoints1));
        }
    }

//    ################################# EXTENSIONS ###############################

    private static void destroyRoute(Route route,
                                     Info currentPlayerInfo) {
        gameState = gameState.withDestructedRoute(route);
        announceInfo(currentPlayerInfo.destroyedRoute(route));

    }
}

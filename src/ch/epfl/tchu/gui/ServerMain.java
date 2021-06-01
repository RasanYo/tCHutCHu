package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RemotePlayerClient;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.application.Application;
import javafx.stage.Stage;

import java.net.ServerSocket;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * classe contenant le programme du server.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class ServerMain extends Application {
    public static void main(String[] args){ launch(args); }

    /**
     * Methode qui creer le server hebergeur du jeu, tout en instanciant une joueur graphique local et un proxy
     * qui va gerer la communication avec le joueur distant
     * @param primaryStage fenetre d'affichage (en l'occurence inutile)
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        List<String> param = getParameters().getRaw();
        String firstName = param.size() > 0 ? param.get(0) : "Ada";
        String secondName = param.size() > 1 ? param.get(1) : "Charles";

        Map<PlayerId, String> playerNames = Map.of(
                PlayerId.PLAYER_1, firstName,
                PlayerId.PLAYER_2, secondName);

        ServerSocket servSocket = new ServerSocket(5108);

        Map<PlayerId, Player> players = Map.of(
                PlayerId.PLAYER_1, new GraphicalPlayerAdapter(),
                PlayerId.PLAYER_2, new RemotePlayerProxy(servSocket.accept()));

        new Thread(() -> Game.play(players, SortedBag.of(ChMap.tickets()),
                new Random())).start();
    }
}
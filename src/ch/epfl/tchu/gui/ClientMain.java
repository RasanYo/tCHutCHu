package ch.epfl.tchu.gui;

import ch.epfl.tchu.net.RemotePlayerClient;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.List;

/**
 * classe permettant de créer le client
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class ClientMain extends Application {
    public static void main(String[] args){ launch(args); }


    /**
     * Methode qui va creer un joueur distant et ensuite le connecter au server hebergeur du jeu sur un nouveau file
     * d'execution
     * @param primaryStage fenetre d'affichage (en l'occurence inutile)
     */
    @Override
    public void start(Stage primaryStage) {
        List<String> param = getParameters().getRaw();

        String hostName = param.size() > 0 ? param.get(0) : "localhost";
        int port = param.size() > 1 ? Integer.parseInt(param.get(1)) : 5108;

        RemotePlayerClient distantClient = new RemotePlayerClient(new GraphicalPlayerAdapter(),
                hostName,
                port);

        new Thread(distantClient::run).start();

    }
}

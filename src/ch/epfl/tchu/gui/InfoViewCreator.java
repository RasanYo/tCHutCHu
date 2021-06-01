package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.gui.ObservableGameState;
import ch.epfl.tchu.gui.StringsFr;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.Iterator;
import java.util.Map;

/**
 *
 * classe permettant de créer la vue des informations.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
final class InfoViewCreator {

    private InfoViewCreator() {}

    /**
     * Methode qui cree la representation graphique des informations publiques du jeu contenant:
     * - les noms des joueurs
     * - leurs nombres de wagons
     * - leurs points de construction totals
     * - le nombre de billet en leurs possessions
     * - le nombre de cartes en leurs possessions
     * - les informations contenant les actions de jeu effectuees
     * @param ownId l'identite du joueur qu'on joue
     * @param playerNames la map liant l'identite d'un joueur a son nom
     * @param gameState l'etat observable du jeu
     * @param gameInfo les informations textuelles publique sur le jeu
     * @return le noeud graphique representant les informatiosn textuelles sur le jeu public
     */
    public static Node createInfoView(PlayerId ownId,
                                      Map<PlayerId, String> playerNames,
                                      ObservableGameState gameState,
                                      ObservableList<Text> gameInfo) {

        VBox infoView = new VBox();
        infoView.getStylesheets().addAll("info.css", "colors.css");

        TextFlow messages = new TextFlow();
        messages.setId("game-info");
        Bindings.bindContent(messages.getChildren(), gameInfo);

        Separator separator = new Separator();
        separator.setOrientation(Orientation.HORIZONTAL);

        VBox playerStats = new VBox();
        playerStats.setId("player-stats");


        PlayerId id = ownId;
        for (int i = 0; i < PlayerId.COUNT; ++i) {
            TextFlow playerStatistics = new TextFlow();
            playerStatistics.getStyleClass().add(id.name());

            Circle circle = new Circle(5);
            circle.getStyleClass().add("filled");

            Text text = new Text();
            text.textProperty().bind(Bindings.format(StringsFr.PLAYER_STATS,
                    playerNames.get(id),
                    gameState.ticketCountInHand(id),
                    gameState.cardCountInHand(id),
                    gameState.carCount(id),
                    gameState.constructionPoints(id)));
            playerStatistics.getChildren().addAll(circle, text);


            playerStats.getChildren().add(playerStatistics);
            id = ownId.next();
        }
        Label pointsLabel = new Label();

        pointsLabel.textProperty().bind(Bindings.format(StringsFr.SHOW_ACTUAL_POINTS,
                gameState.actualTicketPoints(),
                gameState.actualTotalPoints()));

        ToggleButton ticketPointsButton = new ToggleButton("Points actuels");
        pointsLabel.visibleProperty().bind(ticketPointsButton.selectedProperty());



        infoView.getChildren().addAll(playerStats, separator, messages, ticketPointsButton, pointsLabel);

        return infoView;
    }
}

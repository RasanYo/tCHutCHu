package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import javax.script.Bindings;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 *  classe permettant de créer la vue de la carte.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
final class MapViewCreator {

    private MapViewCreator() { }

    /**
     * Methode qui cree la representation graphique de la carte du jeu contenant les routes et les villes/pays, avec
     * laquelle on interagit egalement pour prendre possession d'une route
     * @param observableGameState etat observable du jeu
     * @param claimRouteHandler gestionnaire d'action pour l'emparage d'une route
     * @param cardChooser gestionnaire de selection de cartes/multiensembles de cartes
     * @return le noeud graphique representant la carte du jeu avec laquelle on peut interagir
     */
    public static Pane createMapView(ObservableGameState observableGameState,
                                     ObjectProperty<ActionHandlers.DestroyRouteHandler> destroyRouteHandler,
                                     ObjectProperty<ActionHandlers.ClaimRouteHandler> claimRouteHandler,
                                     CardChooser cardChooser) {        Pane mapView = new Pane();
        mapView.getStylesheets().addAll("map.css", "colors.css");
        mapView.getChildren().add(new ImageView());
        List<Node> routes = new ArrayList<>();
        List<Node> hovers = new ArrayList<>();

        for (Route r : ChMap.routes()) {
            Group group = createGroup(r);

            group.setOnMouseClicked(e -> {
                if (observableGameState.canDestroyRoute(r).get()) {
                    destroyRouteHandler.get().onDestroyRoute(r);
                } else {
                    List<SortedBag<Card>> possibleClaimCards = observableGameState.possibleClaimCards(r);
                    ActionHandlers.ChooseCardsHandler chooseCardsH =
                            chosenCards -> claimRouteHandler.get().onClaimRoute(r, chosenCards);

                    if (possibleClaimCards.size() == 1) {
                        claimRouteHandler.get().onClaimRoute(r, possibleClaimCards.get(0));
                    } else {
                        cardChooser.chooseCards(possibleClaimCards, chooseCardsH);
                    }
                }
            });

            observableGameState.routeOwnerProperty(r).addListener((owner, o, n) -> {
                if (n == null && (group.getStyleClass().contains(PlayerId.PLAYER_1.name()) || group.getStyleClass().contains(PlayerId.PLAYER_2.name()))) {
                    group.getStyleClass().set(group.getStyleClass().size()-1, "");
                } else if (n != null)
                    group.getStyleClass().add(n.name());
            });

//            group.disableProperty().bind((claimRouteHandler.isNull()
//                    .or(observableGameState.possibleToClaimRoute(r).not()))
//                    .and(destroyRouteHandler.isNull()
//                            .or(observableGameState.canDestroyRoute(r)).not()));

            group.disableProperty().bind(
                    ((claimRouteHandler.isNull()).or(observableGameState.possibleToClaimRoute(r).not()))
                            .and((destroyRouteHandler.isNull()).or(observableGameState.canDestroyRoute(r).not()))
            );

            Group hoverGroup = labelGroup(r.toString());
            hoverGroup.setVisible(false);

            group.setOnMouseMoved(e -> {
                hoverGroup.setLayoutX(e.getX()+10);
                hoverGroup.setLayoutY(e.getY());
                hoverGroup.setVisible(true);
            });


            group.setOnMouseExited(e -> hoverGroup.setVisible(false));

            routes.add(group);
            hovers.add(hoverGroup);
        }

        mapView.getChildren().addAll(routes);
        mapView.getChildren().addAll(hovers);

        Group turnGroup = new Group();
        Rectangle rectangle = new Rectangle(151, 31);
        rectangle.setStyle("-fx-arc-height: 5; -fx-arc-width: 5;-fx-fill: #a9c6e1; -fx-stroke: #444080; -fx-stroke-type: inside;");
        Text turnLabel = new Text("C'est à ton tour de jouer");
        turnLabel.setStyle("-fx-font-family: 'Maiandra GD'; -fx-font-size: 14");
        turnLabel.setLayoutX(0);
        turnLabel.setLayoutY(20);
        turnGroup.setLayoutX(966);
        turnGroup.setLayoutY(0);


        turnGroup.getChildren().addAll(rectangle, turnLabel);
        observableGameState.isCurrentPlayer().addListener((observable, oldValue, newValue) -> {
            if(newValue) {
                turnGroup.setVisible(true);
                Timer timer = new Timer();
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        turnGroup.setVisible(false);
                    }
                };
                timer.schedule(task, 3000);
            } else {
                turnGroup.setVisible(false);
            }
        });

        mapView.getChildren().add(turnGroup);

        return mapView;
    }

    /**
     * Methode privee pour faciliter la creation des noeud de groupes representant une route et ses differentes cases
     * en fonction de la longueur de la route. Chaque groupe est aussi interagissable pour pouvoir s'en emparer
     * ulterieurement
     * @param route representee par le groupe
     * @return le noeud graphique du groupe representant la route passee en argument
     */
    private static Group createGroup(Route route) {
        Group routeGroup = new Group();

        routeGroup.setId(route.id());

        routeGroup.getStyleClass().addAll(
                "route",
                route.level().name(),
                route.color() == null ? "NEUTRAL" : route.color().name());

        for (int i = 0; i < route.length(); ++i) {
            Group routeCell = new Group();

            routeCell.setId(String.format("%s_%d", route.id(), i+1));

            Rectangle trackGroup = new Rectangle(36, 12);
            trackGroup.getStyleClass().addAll("track", "filled");

            Group carGroup = new Group();

            Rectangle rectangleGroup = new Rectangle(36, 12);
            rectangleGroup.getStyleClass().add("filled");



            Circle circle1 = new Circle(12, 6, 3);
            Circle circle2 = new Circle(24, 6, 3);
            carGroup.getStyleClass().add("car");
            carGroup.getChildren().addAll(rectangleGroup,circle1,circle2);


            routeCell.getChildren().addAll(trackGroup, carGroup);

            routeGroup.getChildren().add(routeCell);
        }

        return routeGroup;
    }

    private static Group labelGroup(String text) {
        Group group = new Group();

        Rectangle rectangle = new Rectangle(140, 20);
        rectangle.setStyle("-fx-arc-height: 5; -fx-arc-width: 5;-fx-fill: #a9c6e1; -fx-stroke: #444080; -fx-stroke-type: inside;");

        Text label = new Text(text);
        label.setStyle("-fx-font-family: 'Maiandra GD'; -fx-font-size: 9");

        label.setLayoutX(5);
        label.setLayoutY(13);


        group.getChildren().addAll(rectangle, label);

        return group;
    }

    /**
     * Interface representant un gestionnaire qui s'occupe de la selection de cartes/multiensembles de cartes
     */
    @FunctionalInterface
    public interface CardChooser {
        void chooseCards(List<SortedBag<Card>> options,
                         ActionHandlers.ChooseCardsHandler handler);
    }
}

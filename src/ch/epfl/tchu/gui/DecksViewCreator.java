package ch.epfl.tchu.gui;

import ch.epfl.tchu.game.*;
import ch.epfl.tchu.gui.ActionHandlers.DrawTicketsHandler;
import ch.epfl.tchu.gui.ActionHandlers.DrawCardHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;


import static ch.epfl.tchu.game.Card.*;
import static javafx.beans.binding.Bindings.*;

/**
 * classe représentant l'interface graphique de la pioche et de la main du joueur.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
final class DecksViewCreator {


    private DecksViewCreator(){ }

    /**
     * Methode qui cree la partie de l'interface graphique d'un joueur contenant les cartes face visible et les boutons
     * pour tirer les cartes ou les billets des pioches correspondantes
     * @param gm etat observale du jeu
     * @param ticketDrawProp propriete contenant le gestionnaire de tirage de billets
     * @param cardDrawProp propriete contenant le gestionnaire de tirage de cartes
     * @return le noeud contenant les cartes face visible et les boutons
     */
    public static Node createCardsView(ObservableGameState gm,
                                       ObjectProperty<DrawTicketsHandler> ticketDrawProp,
                                       ObjectProperty<DrawCardHandler> cardDrawProp) {

        VBox cardsView = new VBox();
        cardsView.setId("card-pane");
        cardsView.getStylesheets().addAll("decks.css", "colors.css");

        Button ticketsDeck = gaugedButtonCreator(gm.ticketsInDeckProperty());
        ticketsDeck.setText(StringsFr.TICKETS);
        ticketsDeck.disableProperty().bind(ticketDrawProp.isNull());
        ticketsDeck.setOnMouseClicked(e -> ticketDrawProp.get().onDrawTickets());

        cardsView.getChildren().add(ticketsDeck);

        for (int i : Constants.FACE_UP_CARD_SLOTS) {
            StackPane cardSlot = new StackPane();
            gm.faceUpCard(i).addListener((cardPrp, pV, nV) ->{
                cardSlot.getStyleClass().setAll("card", "interact", nV == LOCOMOTIVE ? "NEUTRAL" :
                        nV.name());
            });

            Rectangle outside = new Rectangle(60, 90);
            outside.getStyleClass().add("outside");

            Rectangle inside = new Rectangle(40, 70);
            inside.getStyleClass().addAll("inside", "filled");

            Rectangle image = new Rectangle(40, 70);
            image.getStyleClass().add("train-image");

            cardSlot.getChildren().addAll(outside, inside, image);
            cardSlot.setOnMouseClicked(e -> cardDrawProp.get().onDrawCard(i));
            cardSlot.disableProperty().bind(cardDrawProp.isNull());

            cardsView.getChildren().add(cardSlot);

        }

        Button cardsDeck = gaugedButtonCreator(gm.cardsInDeckPercentage());
        cardsDeck.setText(StringsFr.CARDS);

        cardsDeck.setOnAction(event -> cardDrawProp.get().onDrawCard(Constants.DECK_SLOT));
        cardsDeck.disableProperty().bind(cardDrawProp.isNull());
        cardsView.getChildren().add(cardsDeck);

        return cardsView;
    }

    /**
     *Methode qui cree la partie de l'interface graphique du joueur contenant les cartes et les billets en possession
     * du joueur
     * @return le noeud contenant les cartes et les billets du joueurs
     */
    public static Node createHandView(ObservableGameState gm){

        HBox handView = new HBox();

        handView.getStylesheets().addAll("decks.css", "colors.css");

        ListView<Ticket> listView = new ListView<>(gm.ticketList());
        listView.setId("tickets");

        HBox cardBox = new HBox();
        cardBox.setId("hand-pane");

        Card.ALL.forEach(c -> {
            StackPane stackPaneCard = stackPaneC(c, gm.numberCardOfPlayer(c));
            stackPaneCard.visibleProperty().bind(greaterThan(gm.numberCardOfPlayer(c), 0));
            cardBox.getChildren().add(stackPaneCard);
        });

        handView.getChildren().addAll(listView, cardBox);

        return handView;
    }

    /**
     * Methode qui cree l'affichage graphique des cartes
     * @param card qu'on veut afficher
     * @return le noeud representant graphiquement la carte
     */
    private static StackPane stackPaneC(Card card,
                                        ReadOnlyIntegerProperty cardCount){
        StackPane stackPane = new StackPane();

        if (card.color() == Color.BOMB) {
            stackPane.getStyleClass().addAll("BOMB", "card");
        } else {
            stackPane.getStyleClass().addAll(card.color() == null ?
                            "NEUTRAL" : card.name(),
                    "card");
        }

        Rectangle rect1 = new Rectangle(60, 90);
        rect1.getStyleClass().add("outside");

        Rectangle rect2 = new Rectangle(40, 70);
        rect2.getStyleClass().addAll("filled", "inside");

        Rectangle rect3 = new Rectangle(40, 70);
        rect3.getStyleClass().add("train-image");

        Text txt = new Text();
        txt.textProperty().bind(Bindings.convert(cardCount));
        txt.visibleProperty().bind(Bindings.greaterThan(cardCount, 0));
        txt.getStyleClass().add("count");

        stackPane.getChildren().addAll(rect1, rect2, rect3, txt);


        return stackPane;
    }

    /**
     * Methode qui cree des boutons avec une jauge representant la quantite d'element contenu dans l'instance liee
     * au bouton
     * @return le bouton avec la jauge graphique
     */
    private static Button gaugedButtonCreator(ReadOnlyIntegerProperty observablePct) {
        Button button = new Button();
        button.getStyleClass().add("gauged");

        Group gauge = new Group();

        Rectangle background = new Rectangle(50, 5);
        background.getStyleClass().add("background");
        gauge.getChildren().add(background);


        Rectangle foreground = new Rectangle(50, 5);
        foreground.widthProperty().bind(observablePct.multiply(50).divide(100));
        foreground.getStyleClass().add("foreground");

        gauge.getChildren().add(foreground);
        button.setGraphic(gauge);

        return button;
    }
}

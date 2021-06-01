package ch.epfl.tchu.gui;

import ch.epfl.tchu.Preconditions;
import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCharacterCombination;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ch.epfl.tchu.gui.StringsFr.*;
import static javafx.application.Platform.isFxApplicationThread;

/**
 * classe représentant le joueur graphique.
 *
 * @author Rasan Younis (329880)
 *         Elyes Ben Chaabane (330441)
 */
public final class GraphicalPlayer {

    private final ObjectProperty<ActionHandlers.ClaimRouteHandler> claimRouteHandlerProperty;
    private final ObjectProperty<ActionHandlers.DrawCardHandler> drawCardHandlerProperty;
    private final ObjectProperty<ActionHandlers.DrawTicketsHandler> drawTicketHandlerProperty;
    private final ObjectProperty<ActionHandlers.DestroyRouteHandler> destroyRouteHandlerObjectProperty;


    private final ObservableGameState gameState;
    private final ObservableList<Text> observableList;
    private final Stage mainWindow;


    private final ObjectProperty<MediaPlayer> actualPlayer;


    private final MediaPlayer YOSHI_MUSIC_PLAYER =
            new MediaPlayer(
                    new Media(getClass().getResource("/Music.wav").toURI().toString()));

    private final MediaPlayer DOOM_MUSIC_PLAYER =
            new MediaPlayer(
                    new Media(getClass().getResource("/Doom.wav").toURI().toString()));

    private final MediaPlayer SUNNY_WEATHER_MUSIC_PLAYER =
            new MediaPlayer(
                    new Media(getClass().getResource("/Sunny-Weather.wav").toURI().toString()));


    /**
     * Constructeur de l'interface graphique du jeu d'un joueur
     * @param ownId propre identite du joueur
     * @param playerNames map liant l'identite d'un joueur a son nom
     */
    public GraphicalPlayer(PlayerId ownId,
                           Map<PlayerId, String> playerNames) throws URISyntaxException {
        assert isFxApplicationThread();

        this.claimRouteHandlerProperty = new SimpleObjectProperty<>();
        this.drawCardHandlerProperty = new SimpleObjectProperty<>();
        this.drawTicketHandlerProperty = new SimpleObjectProperty<>();
        this.destroyRouteHandlerObjectProperty = new SimpleObjectProperty<>();

        this.actualPlayer = new SimpleObjectProperty<>();

        this.gameState = new ObservableGameState(ownId);
        this.observableList = FXCollections.observableArrayList();

        Node mapView = MapViewCreator.createMapView(
                gameState,
                destroyRouteHandlerObjectProperty,
                claimRouteHandlerProperty,
                this::chooseClaimCards);

        Node infoView =
                InfoViewCreator.createInfoView(ownId,
                        playerNames,
                        gameState,
                        observableList);

        Node cardsView =
                DecksViewCreator.createCardsView(
                        gameState,
                        drawTicketHandlerProperty,
                        drawCardHandlerProperty);

        Node handView = DecksViewCreator.createHandView(gameState);

        MenuBar menuBar = new MenuBar();

        Stage parametersWindow = createParamWindow();

        Menu parameters = new Menu("Paramètres");
        parameters.setGraphic(new ImageView("options-pixelated.gif"));

        MenuItem item = new MenuItem("Paramètres de sons");

        item.setOnAction(e -> parametersWindow.show());

        parameters.getItems().addAll(item);

        BorderPane windowView = new BorderPane(mapView, menuBar, cardsView, handView, infoView);
        Scene scene = new Scene(windowView);




        menuBar.getMenus().add(parameters);

        mainWindow = new Stage();
        mainWindow.setScene(scene);
        mainWindow.setTitle("tChu \u2014 " + playerNames.get(ownId));
        mainWindow.show();
    }

    /**
     * Methode qui met a jour l'etat observable du jeu
     * @param newGameState nouveau etat de jeu public
     * @param newPlayerState nouveau etat complet du joueur
     */
    public void setState(PublicGameState newGameState, PlayerState newPlayerState) {
        assert isFxApplicationThread();
        gameState.setState(newGameState, newPlayerState);
    }

    /**
     * Methode qui recoit et affiche les informations publiques sur le jeu a communiquer au joueur sur l'interface
     * (au maximum les 5 derniers messages sont affiches)
     * @param message le message a ajoute a la liste d'informations
     */
    public void receiveInfo(String message) {
        assert isFxApplicationThread();

        observableList.add(new Text(message));
        while (observableList.size() > 5){
            observableList.remove(0);
        }

    }


    /**
     * Methode qui en fonction des actions disponibles cree les gestionnaires d'actions.
     * A la fin d'une action, les proprietes contenant les gestionnaires sont videes, indiquant que l'action
     * n'est plus disponible
     * @param drawTicketsHandler gestionnaire de tirage de billets
     * @param drawCardHandler gestionnaire de tirage de cartes
     * @param claimRouteHandler gestionnaire d'emparage de routes
     */
    public void startTurn(ActionHandlers.DrawTicketsHandler drawTicketsHandler,
                          ActionHandlers.DrawCardHandler drawCardHandler,
                          ActionHandlers.ClaimRouteHandler claimRouteHandler,
                          ActionHandlers.DestroyRouteHandler destroyRouteHandler) {
        assert isFxApplicationThread();

        Label itsTurn = new Label("C'est ton tour de jouer !");
        itsTurn.setLayoutX(200);
        itsTurn.setLayoutY(200);



        if (gameState.canDrawTickets()) {
            drawTicketHandlerProperty.set(() -> {
                drawTicketsHandler.onDrawTickets();
                emptyHandlers();
            });
        }
        if (gameState.canDrawCards()) {
            drawCardHandlerProperty.set((slot) -> {
                drawCardHandler.onDrawCard(slot);
                emptyHandlers();
//                drawCard(drawCardHandler);
            });
        }
        claimRouteHandlerProperty.set((r, sb) -> {
            claimRouteHandler.onClaimRoute(r, sb);
            emptyHandlers();
        });

        destroyRouteHandlerObjectProperty.set(r -> {
            destroyRouteHandler.onDestroyRoute(r);
            emptyHandlers();
        });


    }

    private void emptyHandlers() {
        drawTicketHandlerProperty.set(null);
        drawCardHandlerProperty.set(null);
        claimRouteHandlerProperty.set(null);
        destroyRouteHandlerObjectProperty.set(null);
    }

    /**
     * Methode qui cree le menu de selection pour choisir parmi un choix de billets (on peut choisir entre 1 et 3/5
     * billets)
     * @param ticketOptions les billets disponibles
     * @param chooseTicketsHandler gestionnaire de selection de billets
     */
    public void chooseTickets(SortedBag<Ticket> ticketOptions,
                              ActionHandlers.ChooseTicketsHandler chooseTicketsHandler) {
        assert isFxApplicationThread();

        Preconditions.checkArgument(ticketOptions.size() == Constants.IN_GAME_TICKETS_COUNT || ticketOptions.size() == Constants.INITIAL_TICKETS_COUNT);

        ObservableList<Ticket> tickets = FXCollections.observableArrayList(ticketOptions.toList());
        ListView<Ticket> choiceList = new ListView<>(tickets);
        choiceList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Text text = new Text(String.format(StringsFr.CHOOSE_TICKETS, ticketOptions.size()-Constants.DISCARDABLE_TICKETS_COUNT,
                plural(ticketOptions.size()-Constants.DISCARDABLE_TICKETS_COUNT)));

        Button button = new Button(CHOOSE);
        button.disableProperty().bind(Bindings.greaterThan(ticketOptions.size()-Constants.DISCARDABLE_TICKETS_COUNT,
                Bindings.size(choiceList.getSelectionModel().getSelectedItems())));

        Stage menu = selectionMenu(choiceList, button, text);
        menu.setTitle(TICKETS_CHOICE);

        button.setOnAction(e -> {
            menu.hide();
            chooseTicketsHandler.
                    onChooseTickets(SortedBag.of(choiceList.getSelectionModel().getSelectedItems()));
        });
        menu.show();
    }


    /**
     * Methode qui rempli la propriete contenant le gestionnaire de tirage de carte du jeu avec le gestionnaire
     * passee en argument, puis elle se vid elle-meme.
     * Cette methode sert a etre appeler lorsque le joueur a deja tirer une premiere carte.
     * @param drawCardHandler le gestionnaire de tirage de carte
     */
    public void drawCard(ActionHandlers.DrawCardHandler drawCardHandler) {
        assert isFxApplicationThread();

        drawCardHandlerProperty.set(slot -> {
            drawCardHandler.onDrawCard(slot);
            this.drawCardHandlerProperty.set(null);
        });
    }

    /**
     * Methode qui cree le menu de selection pour choisir parmi un choix de multiensembles de cartes pour s'emparer
     * d'une route
     * @param initialClaimCards multiensemble de carte utilise initialement pour s'emparer de la route
     * @param chooseCardsHandler gestionnaire de selection de multiensembles de cartes
     */
    public void chooseClaimCards(List<SortedBag<Card>> initialClaimCards,
                                 ActionHandlers.ChooseCardsHandler chooseCardsHandler) {
        assert isFxApplicationThread();

        Text txt = new Text(CHOOSE_CARDS);

        ListView<SortedBag<Card>> choiceList = createCardsListView(initialClaimCards);


        Button button = new Button(CHOOSE);
        Stage menu = selectionMenu(choiceList, button, txt);
        menu.setTitle(CARDS_CHOICE);

        button.disableProperty().bind(Bindings.size(choiceList.getSelectionModel().getSelectedItems()).lessThan(1));
        button.setOnAction(e -> {
            menu.hide();
            chooseCardsHandler.onChooseCards(choiceList.getSelectionModel().getSelectedItem());
        });

        menu.show();
    }

    /**
     * Methode qui cree le menu de selection pour choisir parmi un choix de cartes additionnelles en cas de tunnel
     * @param possibleAdditionalClaimCards multiensembles de cartes additionnelles disponibles
     * @param chooseCardsHandler gestionnaire de selection de multiensembles de cartes
     */
    public void chooseAdditionalCards(List<SortedBag<Card>> possibleAdditionalClaimCards,
                                      ActionHandlers.ChooseCardsHandler chooseCardsHandler) {
        assert isFxApplicationThread();

        Text txt = new Text(CHOOSE_ADDITIONAL_CARDS);

        TextFlow txtF = new TextFlow();
        txtF.getChildren().add(txt);

        ListView<SortedBag<Card>> choiceList = createCardsListView(possibleAdditionalClaimCards);


        Button button = new Button(CHOOSE);
        Stage menu = selectionMenu(choiceList, button, txt);
        menu.setTitle(CARDS_CHOICE);

        button.setOnAction(e -> {
            if (choiceList.getSelectionModel().getSelectedItems().isEmpty()) {
                chooseCardsHandler.onChooseCards(SortedBag.of());
            } else {
                chooseCardsHandler.onChooseCards(choiceList.getSelectionModel().getSelectedItem());
            }
            menu.hide();

        });

        menu.show();

    }

     private ListView<SortedBag<Card>> createCardsListView(List<SortedBag<Card>> cardsBag) {
         ObservableList<SortedBag<Card>> claimCards = FXCollections.observableArrayList(cardsBag);
         ListView<SortedBag<Card>> choiceList = new ListView<>(claimCards);
         choiceList.setCellFactory(v ->
                 new TextFieldListCell<>(new CardBagStringConverter()));
         return choiceList;
     }


    /**
     * Methode privee pour faciliter la creation d'un menu de selection graphique
     * @param listView la vue de la liste representant les possibilites de selection
     * @param button de confirmation
     * @param text decrivant le role du menu de selection
     * @param <T> type des elements disponibles a la selection
     * @return la fenetre representant le menu de selection
     */
    private <T> Stage selectionMenu(ListView<T> listView, Button button, Text text) {
        Stage menu = new Stage(StageStyle.UTILITY);

        TextFlow textFlow = new TextFlow(text);

        VBox box = new VBox(textFlow, listView, button);

        Scene chooserScene = new Scene(box);
        chooserScene.getStylesheets().add("chooser.css");
        menu.initOwner(mainWindow);
        menu.initModality(Modality.WINDOW_MODAL);
        menu.setScene(chooserScene);
        menu.setOnCloseRequest(Event::consume);
        return menu;
    }

    /**
     * Classe privee qui redefinit la representation textuelle d'un multiensemble de cartes
     */
    private static class CardBagStringConverter extends StringConverter<SortedBag<Card>>{

        /**
         * Methode toString adaptee a la representation textuelle des multiensembles de cartes pour les menus de
         * selection (sous formes de phrases au lieu de liste)
         * @param object multiensemble represente
         * @return la string avec la nouvelle representation textuelle
         */
        @Override
        public String toString(SortedBag<Card> object) {
            List<String> cardNames = new ArrayList<>();
            object.toSet().forEach(c -> {
                int n = object.countOf(c);
                cardNames.add(n + " " + Info.cardName(c, n));
            });
            return cardNames.size() > 1 ?
                    String.join(", ", cardNames.subList(0, cardNames.size() - 1)) +
                            StringsFr.AND_SEPARATOR + cardNames.get(cardNames.size() - 1) :
                    cardNames.get(cardNames.size() - 1);
        }


        @Override
        public SortedBag<Card> fromString(String string) {
            throw new UnsupportedOperationException();
        }
    }

    //    ################################################## EXTENSIONS ##################################################

    private Stage createParamWindow(){

        Slider volumeSlider = new Slider();
        volumeSlider.prefWidth(205);
        volumeSlider.prefHeight(65);
        volumeSlider.setLayoutX(374);
        volumeSlider.setLayoutY(263);

        ToggleGroup musicGroup = new ToggleGroup();

        RadioButton yoshiButton = new RadioButton("Yoshi");
        yoshiButton.setLayoutX(61);
        yoshiButton.setLayoutY(55);
        yoshiButton.selectedProperty().addListener((observable, oldValue, newValue) ->{
            if(actualPlayer.get() == null) {
                actualPlayer.set(YOSHI_MUSIC_PLAYER);
            }else{
                actualPlayer.get().stop();
                actualPlayer.set(YOSHI_MUSIC_PLAYER);
            }
        });

        RadioButton doomButton = new RadioButton("DOOM");
        doomButton.setLayoutX(153);
        doomButton.setLayoutY(55);
        doomButton.selectedProperty().addListener(((observable, oldValue, newValue) ->{
            if(actualPlayer.get() == null) {
                actualPlayer.set(DOOM_MUSIC_PLAYER);
            }else{
                actualPlayer.get().stop();
                actualPlayer.set(DOOM_MUSIC_PLAYER);
            }
        }));

        RadioButton sunnyWeatherButton = new RadioButton("Sunny Weather");
        sunnyWeatherButton.setLayoutX(240);
        sunnyWeatherButton.setLayoutY(55);
        sunnyWeatherButton.selectedProperty().addListener(((observable, oldValue, newValue) ->{
            if(actualPlayer.get() == null) {
                actualPlayer.set(SUNNY_WEATHER_MUSIC_PLAYER);
            }else{
                actualPlayer.get().stop();
                actualPlayer.set(SUNNY_WEATHER_MUSIC_PLAYER);
            }
        }));

        musicGroup.getToggles().addAll(yoshiButton, doomButton, sunnyWeatherButton);


        Button play = new Button("Play");
        play.setFont(Font.font(30));
        play.setLayoutX(44);
        play.setLayoutY(263);
        play.setGraphic(new ImageView("play.png"));
        play.setOnAction(e -> {
            if(musicGroup.getSelectedToggle() != null) {
                actualPlayer.get().play();
                actualPlayer.get().setCycleCount(MediaPlayer.INDEFINITE);
                actualPlayer.get().volumeProperty().bind(volumeSlider.valueProperty().divide(200));
            }
        });

        Button stop = new Button("Stop");
        stop.setFont(Font.font(30));
        stop.setLayoutX(138);
        stop.setLayoutY(263);
        stop.setGraphic(new ImageView("stop.png"));
        stop.setOnAction(e -> {
            if(musicGroup.getSelectedToggle() != null) {
                actualPlayer.get().stop();
            }
        });


        Button pause = new Button("Pause");
        pause.setFont(Font.font(30));
        pause.setLayoutX(240);
        pause.setLayoutY(263);
        pause.setGraphic(new ImageView("pause.gif"));
        pause.setOnAction(e -> {
            if(musicGroup.getSelectedToggle() != null) {
                actualPlayer.get().pause();
            }
        });

        /*yoshiButton, turnOffMusicButton, volumeText, volumeSlider*/
        AnchorPane pane = new AnchorPane(volumeSlider, play, stop, pause, yoshiButton, doomButton, sunnyWeatherButton);
        pane.prefWidth(600);
        pane.prefHeight(400);

        Stage window = new Stage(StageStyle.UTILITY);
        window.setScene(new Scene(pane));

        return window;
    }

    public static void getName(ActionHandlers.ChooseNameHandler chooseNameHandler) {
        assert isFxApplicationThread();
        chooseNameHandler.onChooseName(LaunchMenu.getOwnName());
        System.out.println("check getName");
    }

}

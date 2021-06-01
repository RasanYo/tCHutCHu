package ch.epfl.tchu.gui;

import ch.epfl.tchu.SortedBag;
import ch.epfl.tchu.game.ChMap;
import ch.epfl.tchu.game.Game;
import ch.epfl.tchu.game.Player;
import ch.epfl.tchu.game.PlayerId;
import ch.epfl.tchu.net.RemotePlayerClient;
import ch.epfl.tchu.net.RemotePlayerProxy;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static javafx.application.Platform.isFxApplicationThread;

public class LaunchMenu {

    private static String ownName;
    private final ObjectProperty<MediaPlayer> musicPlayer = new SimpleObjectProperty<>();
    private final MediaPlayer ZELDA_EASTER_EGG_MUSIC =
            new MediaPlayer(
                    new Media(getClass().getResource("/LOZ_Secret.wav").toURI().toString()));

    public LaunchMenu() throws URISyntaxException {
        scene1();
    }

    public static void main(String[] args) throws URISyntaxException {
        new LaunchMenu();
    }



    public void scene1(){
        assert isFxApplicationThread();

        Text sceneTitle = new Text("Veuillez entrer votre pseudonyme");
        sceneTitle.setFont(Font.font(32));
        sceneTitle.setLayoutX(62);
        sceneTitle.setLayoutY(112);

        Label username = new Label("Pseudo :");
        username.setFont(Font.font(28));
        username.setTextFill(Color.GREEN);
        username.setLayoutX(115);
        username.setLayoutY(188);

        TextField entryUsername = new TextField();
        entryUsername.setFont(Font.font(16));
        entryUsername.setLayoutX(251);
        entryUsername.setLayoutY(189);

        Button playButton = new Button("Valider");
        playButton.setFont(Font.font(15));
        playButton.setLayoutX(267);
        playButton.setLayoutY(269);

        AnchorPane panel = new AnchorPane(playButton, entryUsername, username, sceneTitle);
        panel.setPrefHeight(400);
        panel.setPrefWidth(600);

        Stage main = new Stage();
        main.setScene(new Scene(panel));
        main.setTitle("Login");
        main.show();

        playButton.setOnAction(e -> {
            if(!entryUsername.getText().isEmpty()){
                main.close();
                setOwnName(entryUsername.getText());
                scene2(entryUsername.getText());
            }
        });
    }
    private void scene2(String username){
        assert isFxApplicationThread();

        Text sceneTitle = new Text();
        if(username.equals("Link") || username.equals("link")){
            sceneTitle.setText("It is dangerous to go outside, take this with you !");
            musicPlayer.set(ZELDA_EASTER_EGG_MUSIC);
            musicPlayer.get().setVolume(50);
            musicPlayer.get().play();
        }else{
            sceneTitle.setText(String.format("Bienvenue %s", username));
        }
        sceneTitle.setFont(Font.font("bahnschrift", FontWeight.BOLD, 28));
        sceneTitle.setLayoutX(91);
        sceneTitle.setLayoutY(130);

        Button buttonCreate = new Button("Créer une partie");
        buttonCreate.setFont(Font.font("Arial Unicode MS", 28));
        buttonCreate.setLayoutX(71);
        buttonCreate.setLayoutY(216);

        Button buttonJoin = new Button("Rejoindre une partie");
        buttonJoin.setFont(Font.font("Arial Unicode MS", 28));
        buttonJoin.setLayoutX(71);
        buttonJoin.setLayoutY(313);

        AnchorPane panel = new AnchorPane(sceneTitle, buttonCreate, buttonJoin);
        panel.setPrefHeight(400);
        panel.setPrefWidth(600);

        Stage mainStage2 = new Stage();
        mainStage2.setScene(new Scene(panel));
        mainStage2.setTitle("Selection menu");
        mainStage2.show();

        buttonCreate.setOnAction(e -> {
            mainStage2.close();
            scene21();
        });
        buttonJoin.setOnAction(e ->{
            mainStage2.close();
            scene22();
        });
    }
    private void scene21(){
        assert isFxApplicationThread();
        Text textTitle = new Text("Veuillez entrer votre port");
        textTitle.setFont(Font.font(32));
        textTitle.setLayoutX(124);
        textTitle.setLayoutY(60);

        Label portLabel = new Label("Port :");
        portLabel.setFont(Font.font(22));
        portLabel.setLayoutX(166);
        portLabel.setLayoutY(200);

        TextField entryPort = new TextField();
        entryPort.setFont(Font.font(14));
        entryPort.setLayoutX(243);
        entryPort.setLayoutY(201);

        Button validationButton = new Button("Valider");
        validationButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        validationButton.setLayoutX(438);
        validationButton.setLayoutY(204);

        Button startServer = new Button("Lancer le server");
        startServer.setLayoutX(251);
        startServer.setLayoutY(328);

        AnchorPane panel = new AnchorPane(textTitle, portLabel, entryPort, validationButton, startServer);
        panel.setPrefHeight(400);
        panel.setPrefWidth(600);

        Stage mainStage21 = new Stage();
        mainStage21.setScene(new Scene(panel));
        mainStage21.setTitle("Party creation");
        mainStage21.show();

        validationButton.setOnAction(e -> {
            if(!entryPort.getText().isEmpty() && isInteger(entryPort.getText())){
                try {
                    startServer.setOnAction(event -> {
                        try {
                            Platform.setImplicitExit(false);
                            Platform.runLater(()->System.out.println("Inside Platform.runLater()"));
                            mainStage21.close();
                            serverMain(Integer.parseInt(entryPort.getText()));
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    });
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            } else {
                Text falseEntry = new Text("Please enter a correct port");
                falseEntry.setFill(Color.RED);
                falseEntry.setLayoutX(261);
                falseEntry.setLayoutY(245);

                panel.getChildren().add(falseEntry);
            }
        });
    }
    private boolean isInteger(String txt) {
        try {
            Integer.parseInt(txt);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }
    private void scene22(){
        assert isFxApplicationThread();

        Text textTitle = new Text("Entrer votre IP/Port :");
        textTitle.setFont(Font.font(32));
        textTitle.setLayoutX(156);
        textTitle.setLayoutY(100);

        TextField ip = new TextField();
        ip.setLayoutX(180);
        ip.setLayoutY(171);

        Label ipLabel = new Label("IP :");
        ipLabel.setFont(Font.font(19));
        ipLabel.setLayoutX(129);
        ipLabel.setLayoutY(170);

        TextField port = new TextField();
        port.setLayoutX(180);
        port.setLayoutY(234);

        Label portLabel = new Label("Port :");
        portLabel.setLayoutX(122);
        portLabel.setLayoutY(234);
        portLabel.setFont(Font.font(19));

        Button validationButton = new Button("Rejoindre la partie");
        validationButton.setLayoutX(387);
        validationButton.setLayoutY(287);
        validationButton.setFont(Font.font(14));

        Label wrongIP = new Label();
        wrongIP.setTextFill(Color.RED);
        wrongIP.setLayoutX(283);
        wrongIP.setLayoutY(204);

        Label wrongPort = new Label();
        wrongPort.setTextFill(Color.RED);
        wrongPort.setLayoutX(283);
        wrongPort.setLayoutY(270);

        AnchorPane panel = new AnchorPane(textTitle,
                ip,
                ipLabel,
                port,
                portLabel,
                validationButton,
                wrongIP,
                wrongPort);
        panel.setPrefHeight(400);
        panel.setPrefWidth(600);

        Stage mainStage22 = new Stage();
        mainStage22.setTitle("Waiting for server response...");
        mainStage22.setScene(new Scene(panel));
        mainStage22.show();

        validationButton.setOnAction(e -> {
            try {
                if(isInteger(ip.getText())){
                    wrongIP.setText("wrong Ip");
                }else if(!isInteger(port.getText())){
                    wrongPort.setText("wrong port");
                }else if(!isInteger(port.getText()) && isInteger(ip.getText())){
                    wrongIP.setText("wrong Ip");
                    wrongPort.setText("wrong port");
                }else {
                    Platform.setImplicitExit(false);
                    Platform.runLater(()->System.out.println("Inside Platform.runLater()"));
                    mainStage22.close();
                    List<String> parameter = new ArrayList<>();
                    parameter.add(ip.getText());
                    parameter.add(port.getText());
                    clientMain(parameter);
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }
    private void serverMain(int port) throws Exception {
        assert isFxApplicationThread();

        ServerSocket servSocket = new ServerSocket(port);
        Socket socket = servSocket.accept();


        Map<PlayerId, Player> players = Map.of(PlayerId.PLAYER_1, new GraphicalPlayerAdapter(),
                PlayerId.PLAYER_2, new RemotePlayerProxy(socket));
        System.out.println("server started");
        new Thread(() -> Game.play(players, SortedBag.of(ChMap.tickets()),
                new Random())).start();
    }
    private void clientMain(List<String> param) throws Exception {
        assert isFxApplicationThread();

        String hostName = param.size() > 0 ? param.get(0) : "localhost";
        int port = param.size() > 1 ? Integer.parseInt(param.get(1)) : 5108;


        RemotePlayerClient distantClient = new RemotePlayerClient(new GraphicalPlayerAdapter(),
                hostName,
                port);

        System.out.println(distantClient);
        new Thread(distantClient::run).start();
    }
    private static void setOwnName(String name) {
        ownName = name;
        System.out.println(ownName);
    }
    public static String getOwnName() {
        return ownName;
    }
}

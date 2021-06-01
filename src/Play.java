import ch.epfl.tchu.gui.LaunchMenu;
import com.sun.jdi.connect.LaunchingConnector;
import javafx.application.Application;
import javafx.stage.Stage;

public class Play extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        LaunchMenu game = new LaunchMenu();
    }

}

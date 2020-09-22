package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.API.AppServer;

public class Main extends Application {

    public static String mainToken = "";

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("FXMLs/signInForm.fxml"));
        primaryStage.setTitle("Karkargram");
        Scene scene = new Scene(root, 400, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        AppServer server = new AppServer(8080);
        launch(args);
    }
}

package sample;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField loginText;

    @FXML
    private TextField passwordText;

    @FXML
    private Button signInBtn;

    @FXML
    private Button goToRegBtn;

    @FXML
    void initialize() {
        signInBtn.setOnAction(actionEvent -> {
            Stage stage = (Stage) signInBtn.getScene().getWindow();
            try {
                stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("FXMLs/sample.fxml")), 1050, 720));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

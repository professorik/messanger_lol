package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import kotlin.Unit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LoginController extends MainController {

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
        signInBtn.setOnAction(actionEvent -> signIn());
        goToRegBtn.setOnAction(actionEvent -> changeScene(goToRegBtn, "signUpForm", 400, 650));
    }

    private void signIn() {
        AppSocket socket = new AppSocket();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "login");
            jsonObject.put("username", loginText.getText());
            jsonObject.put("password", passwordText.getText());
            JSONObject object = (JSONObject) new JSONParser().parse(jsonObject.toString());
            socket.request(object).addListener(response -> {
                if ((Boolean) response.get("success")) {
                    changeScene(signInBtn, "sample", 1050, 720);
                } else {
                    showAlert(Alert.AlertType.ERROR, signInBtn.getScene().getWindow(), "Sign In Error", response.get("error").toString());
                }
                System.out.println(response);
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}

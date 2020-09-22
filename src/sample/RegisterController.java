package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import kotlin.Unit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import sample.API.AppSocket;

public class RegisterController extends MainController{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField loginText;

    @FXML
    private TextField passwordText;

    @FXML
    private TextField passwordText1;

    @FXML
    private Button signUpBtn;

    @FXML
    private Button goToLoginBtn;

    @FXML
    void initialize() {
        goToLoginBtn.setOnAction(actionEvent -> changeScene(goToLoginBtn, "signInForm", 400, 650));
        signUpBtn.setOnAction(actionEvent -> signUp());
    }

    private void signUp(){
        if (passwordText.getText().equals(passwordText1.getText())) {
            AppSocket socket = new AppSocket();
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", "register");
                jsonObject.put("username", loginText.getText());
                jsonObject.put("password", passwordText.getText());
                socket.request(jsonObject).addListener(response -> {
                    if ((Boolean) response.get("success")) {
                        changeScene(signUpBtn, "signInForm", 400, 650);
                    } else {
                        showAlert(Alert.AlertType.ERROR, signUpBtn.getScene().getWindow(), "Sign In Error", response.get("error").toString());
                    }
                    System.out.println(response);
                    return Unit.INSTANCE;
                });
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, signUpBtn.getScene().getWindow(), "Sign Up Error", "The passwords aren't equal!");
        }
    }
}

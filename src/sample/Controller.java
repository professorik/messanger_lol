package sample;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private Button menuBtn;

    @FXML
    private TextField searchField;

    @FXML
    private VBox chatsVbox;

    @FXML
    private ScrollPane chatContent;

    @FXML
    void initialize() {
        Label name = new Label("Сообщесвто");
        Label time = new Label("13:23");
        String url = "sample/icons/menuIcon.png";
        Image image = new Image(url, true);
        ImageView imageView = new ImageView(image);
        Label msg = new Label("Чтобы стать прогером надо всего лишь...");
        ChatViewItem chatViewItem;
        //chatsVbox.setStyle("-fx-background-color: red");
        for (int i = 0; i < 15; i++) {
            chatViewItem = new ChatViewItem("Чат ".concat(String.valueOf(i)), chatsVbox.getWidth());
            chatsVbox.getChildren().add(chatViewItem);
        }
    }
}

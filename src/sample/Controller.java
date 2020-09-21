package sample;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.VBox;

public class Controller {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private SplitPane splitPane;

    @FXML
    private Button menuBtn;

    @FXML
    private TextField searchField;

    @FXML
    private VBox chatsVbox;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    void initialize() {
        Label name = new Label("Сообщесвто");
        Label time = new Label("13:23");
        String url = "sample/icons/menuIcon.png";
        Image image = new Image(url, true);
        ImageView imageView = new ImageView(image);
        Label msg = new Label("Чтобы стать прогером надо всего лишь...");
        ChatViewItem[] chatViewItem = new ChatViewItem[30];
        //chatsVbox.setStyle("-fx-background-color: red");
        for (int i = 0; i < 15; i++) {
            chatViewItem[i] = new ChatViewItem("Чат ".concat(String.valueOf(i)), chatsVbox.getWidth());
            chatsVbox.getChildren().add(chatViewItem[i]);
        }
        splitPane.getDividers().get(0).positionProperty().addListener((observableValue, number, t1) -> {
            for (int i = 0; i < 15; i++) {
                chatViewItem[i].setPrefWidth(splitPane.getScene().getWidth()*splitPane.getDividerPositions()[0]-10);
            }
        });
    }
}

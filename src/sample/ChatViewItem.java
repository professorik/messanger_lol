package sample;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ChatViewItem extends AnchorPane {

    @FXML
    private Label name;

    @FXML
    private ImageView avatar;

    @FXML
    private Label time;

    @FXML
    private Label lastMsg;

    public ChatViewItem() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLs/chatItem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.setOnMouseClicked(mouseEvent -> {
            System.out.println("kar");
        });
    }

    public ChatViewItem(String name, String message, String timestamp, Object avatar, double width) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLs/chatItem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.setPrefWidth(width);
        this.name.setText(name);
        this.lastMsg.setText(message);
        this.time.setText(timestamp);
        if (avatar != null){
            this.avatar.setImage(ImageProcessor.decodeBase64ToFile(avatar.toString()));
        }
        this.setOnMouseClicked(mouseEvent -> this.setStyle("-fx-background-color: dodgerblue"));
        this.setOnMouseEntered(mouseEvent ->  this.setStyle("-fx-background-color: slategray"));
        this.setOnMouseExited(mouseEvent -> this.setStyle("-fx-background-color: #233144"));
    }
}


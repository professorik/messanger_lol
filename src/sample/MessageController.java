package sample;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;

public class MessageController extends AnchorPane{

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextArea messageValue;

    @FXML
    private AnchorPane messagesRoot;

    @FXML
    private Label messageTimestamp;

    public MessageController(String msgValue, String time) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("FXMLs/messageItem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.messageTimestamp.setText(time);
        this.messageValue.setText(msgValue);
        this.setPrefHeight(Controller.computeTextHeight(this.messageValue.getFont(), this.messageValue.getText(), this.messageValue.getPrefWidth())+20);
    }

    public void setPrefSizeRoot(double w){
        this.messagesRoot.setPrefWidth(w);
        this.messageValue.setPrefWidth(w);
        this.setPrefHeight(Controller.computeTextHeight(this.messageValue.getFont(), this.messageValue.getText(), w-45)*1.4+10);
    }

}

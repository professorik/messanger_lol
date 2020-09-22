package sample;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import kotlin.Unit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import sample.API.AppSocket;

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

    private ArrayList<ChatViewItem> chatViewItems;

    @FXML
    void initialize() {
        chatViewItems = new ArrayList<>();
        //chatsVbox.setStyle("-fx-background-color: red");
        initChatsList(Main.mainToken);
        controlChatsList();
    }

    private void initChatsList(String token) {
        AppSocket socket = new AppSocket();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "getChats");
            jsonObject.put("token", token);
            socket.request(jsonObject).addListener(response -> {
                System.out.println("Request: " + jsonObject);
                System.out.println("Response: " + response);
                if ((Boolean) response.get("success")) {
                    ArrayList<String> chats = (ArrayList<String>) response.get("chats");
                    JSONObject lastMsgResp;
                    for (String chatName : chats) {
                        lastMsgResp = new JSONObject();
                        lastMsgResp.put("type", "getLastMessagesFrom");
                        lastMsgResp.put("username", chatName);
                        lastMsgResp.put("token", Main.mainToken);
                        lastMsgResp.put("offset", 0);
                        socket.request(lastMsgResp).addListener(responseMsg -> {
                            addItemToChatsList(chatName, ((ArrayList<JSONObject>) responseMsg.get("messages")).get(0));
                            return Unit.INSTANCE;
                        });
                    }
                }
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    private void addItemToChatsList(String chatName, JSONObject lastMessage) {
        Platform.runLater(() -> {
            Timestamp timestamp = new Timestamp(Long.valueOf(lastMessage.get("timestamp").toString()));
            Date date = new Date(timestamp.getTime()*1000);
            SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
            sdf.setTimeZone(Calendar.getInstance().getTimeZone());
            String formattedDate = sdf.format(date);
            chatViewItems.add(new ChatViewItem(chatName, lastMessage.get("value").toString(), formattedDate, 1050 * splitPane.getDividerPositions()[0] - 10));
            chatsVbox.getChildren().add(chatViewItems.get(chatViewItems.size() - 1));
        });
    }

    private void controlChatsList() {
        splitPane.getDividers().get(0).positionProperty().addListener((observableValue, number, t1) -> {
            for (int i = 0; i < chatViewItems.size(); i++) {
                chatViewItems.get(i).setPrefWidth(splitPane.getScene().getWidth() * splitPane.getDividerPositions()[0] - 10);
            }
        });
    }
}

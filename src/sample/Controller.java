package sample;

import java.net.URL;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import kotlin.Unit;
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
    private VBox messagesVbox;

    @FXML
    private ScrollPane chatScrollPane;

    @FXML
    private TextArea sendMsgText;

    @FXML
    private Button sendBtn;

    @FXML
    private Label nicknameForChat;

    @FXML
    private Label userStatus;

    @FXML
    private AnchorPane messagesAnchor;

    @FXML
    private AnchorPane userDetailAnchor;

    private ArrayList<ChatViewItem> chatViewItems;
    private ArrayList<MessageController> messageItems;

    @FXML
    void initialize() {
        chatViewItems = new ArrayList<>();
        messageItems = new ArrayList<>();
        //chatsVbox.setStyle("-fx-background-color: red");
        initChatsList(Main.mainToken);
        controlNodesHeight();
        //TODO: in SceneBuilder set visibility
        userDetailAnchor.setVisible(false);
        messagesAnchor.setVisible(false);
    }

    private void initChat(String name) {
        userDetailAnchor.setVisible(true);
        messagesAnchor.setVisible(true);
        nicknameForChat.setText(name);
        messagesVbox.getChildren().clear();
        messageItems = new ArrayList<>();
        AppSocket socket = new AppSocket();
        try {
            JSONObject lastMsgResp = new JSONObject();
            lastMsgResp.put("type", "getLastMessagesFrom");
            lastMsgResp.put("username", name);
            lastMsgResp.put("token", Main.mainToken);
            lastMsgResp.put("offset", 0);
            socket.request(lastMsgResp).addListener(responseMsg -> {
                for (JSONObject object: (ArrayList<JSONObject>) responseMsg.get("messages")){
                    Platform.runLater(()->{
                        System.out.println(object);
                        String formattedDate = convertTimestamp(object.get("timestamp").toString());
                        messageItems.add(new MessageController(object.get("value").toString(), formattedDate));
                        messagesVbox.getChildren().add(messageItems.get(messageItems.size()-1));
                    });
                }
                return Unit.INSTANCE;
            });
        } catch (Exception e) {
            System.err.println(e);
        }
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
                            addItemToChatsList(chatName, ((ArrayList<JSONObject>) responseMsg.get("messages")).get(0), socket);
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

    private void addItemToChatsList(String chatName, JSONObject lastMessage, AppSocket socket) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", "findUser");
        jsonObject.put("token", Main.mainToken);
        jsonObject.put("username", chatName);
        socket.request(jsonObject).addListener(response -> {
            System.out.println(response);
            Platform.runLater(() -> {
                String formattedDate = convertTimestamp(lastMessage.get("timestamp").toString());
                chatViewItems.add(new ChatViewItem(chatName, lastMessage.get("value").toString(), formattedDate, response.get("profilePicture"), 1050 * splitPane.getDividerPositions()[0] - 10));
                chatViewItems.get(chatViewItems.size()-1).setOnMouseClicked(mouseEvent -> {
                    initChat(chatName);
                });
                chatsVbox.getChildren().add(chatViewItems.get(chatViewItems.size() - 1));
            });
            return Unit.INSTANCE;
        });
    }

    private void controlNodesHeight() {
        splitPane.getDividers().get(0).positionProperty().addListener((observableValue, number, t1) -> {
            this.sendMsgText.setPrefHeight(Math.min(200, 40 + computeTextHeight(sendMsgText.getFont(), sendMsgText.getText(), splitPane.getScene().getWidth() * (1 - splitPane.getDividerPositions()[0]) - 120)));
            for (int i = 0; i < chatViewItems.size(); i++) {
                chatViewItems.get(i).setPrefWidth(splitPane.getScene().getWidth() * splitPane.getDividerPositions()[0] - 10);
            }
            for (int i = 0; i < messageItems.size(); i++) {
                System.out.println(messageItems.get(0).getPrefWidth());
                messageItems.get(i).setPrefSizeRoot(splitPane.getScene().getWidth() * (1 - splitPane.getDividerPositions()[0]) * 0.7);
                System.out.println(messageItems.get(0).getPrefWidth());
            }
        });
        sendMsgText.textProperty().addListener((observable, oldValue, newValue) -> {
            this.sendMsgText.setPrefHeight(Math.min(200, 40 + computeTextHeight(sendMsgText.getFont(), sendMsgText.getText(), splitPane.getScene().getWidth() * (1 - splitPane.getDividerPositions()[0]) - 120)));
        });
    }

    private String convertTimestamp(String timestampStr){
        Timestamp timestamp = new Timestamp(Long.valueOf(timestampStr));
        Date date = new Date(timestamp.getTime()*1000);
        SimpleDateFormat sdf = new SimpleDateFormat("kk:mm");
        sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        return sdf.format(date);
    }

    protected static double computeTextHeight(Font font, String text, double wrappingWidth) {
        Text helper = new Text();
        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth((int)wrappingWidth);
        return helper.getLayoutBounds().getHeight();
    }
}

package sample.API;

import kotlin.Unit;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws IOException {
        AppServer server = new AppServer(8080, 8081);

        AppSocket socket = new AppSocket();

        socket.addListener(data -> {
            if (data.containsKey("type") && data.get("type").equals("message")) {
                System.out.print("Message: ");
                System.out.println(data);
            }
            return Unit.INSTANCE;
        });

        Scanner scanner = new Scanner(System.in);

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            AppSocket newSocket = new AppSocket();
            JSONObject register = new JSONObject();
            register.put("type", "register");
            register.put("username", "karkar1");
            register.put("password", "karkar");
            newSocket.request(register).addListener(__ -> {
                JSONObject login = new JSONObject();
                login.put("type", "login");
                login.put("username", "karkar1");
                login.put("password", "karkar");
                newSocket.request(login).addListener(response -> {
                    String token = (String) response.get("token");
                    JSONObject message = new JSONObject();
                    message.put("type", "sendMessage");
                    message.put("token", token);
                    message.put("value", "ty mudak1");
                    message.put("username", "nikita202");
                    newSocket.request(message).addListener(messageResponse -> {
                        System.out.println(messageResponse);
                        return Unit.INSTANCE;
                    });
                    return Unit.INSTANCE;
                });
                return Unit.INSTANCE;
            });
        }).start();

        while (true) {
            String input = scanner.nextLine();
            try {
                JSONObject object = (JSONObject) new JSONParser().parse(input);
                socket.request(object).addListener(response -> {
                    System.out.println("Response to " + input);
                    System.out.println(response);
                    return Unit.INSTANCE;
                });
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }
}

//{"type":"register","username":"nikita202","password":"karkar"}
//{"type":"login","username":"nikita202","password":"karkar"}
//{"type":"setCurrentToken","token":"XeZTRncIlsvKFd67lUFu"}
//{"type":"getLastMessagesFrom","username":"karkar"}

package soselab.david.msdobot.RabbitMQ.Consumer;

import org.json.JSONObject;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Service.JDAConnect;

import java.nio.charset.StandardCharsets;

/**
 * handle each message from rabbitmq server
 */
@Service
public class RabbitMessageHandler {

    private final JDAConnect jdaService;

    public RabbitMessageHandler(JDAConnect jdaConnect){
        this.jdaService = jdaConnect;
    }

    public void handleMessage(String msg){
        System.out.println(msg);
    }

    /**
     * parse received raw message to Type String
     * replace '\n' with newline character
     * @param message raw message received from rabbitmq
     */
    public void handleJenkinsMessage(byte[] message){
        String rawMsg = new String(message, StandardCharsets.UTF_8);
        // replace '\n' characters with newline character to make sure we can split raw message in to multiple lines
        String saltedRawMsg = rawMsg.replace("\\n", "\n");
        String[] tokenList = saltedRawMsg.split("\n"); // the string array contains each line of raw message
        System.out.println("[DEBUG][message_received][raw] " + rawMsg);
        String output = "[Jenkins]> " + saltedRawMsg;

        // <-- maybe add some filter here
        // send message to discord
        jdaService.send("test_channel", output);
    }

    public void handleEurekaMessage(byte[] message){
        String msg = new String(message);
        JSONObject obj  = new JSONObject(msg);
//        System.out.println(obj.toString());
        System.out.println("[DEBUG][raw][handleEurekaMsg]" + obj);

//        System.out.println(obj.get("status"));
        String status = (String) obj.get("status");
        String result = "";
        switch(status){
            case "Failed":
                result = "[" + status + "] Service : " + obj.get("appName") + " is dead. Please check it.";
                break;
            case "Server Start":
                result = "[" + status + "] Eureka Server just start working !";
                break;
            case "Server Registry Start":
                result = "[" + status + "] Eureka Registry Server just start working !";
                break;
        }

        jdaService.send("test_channel", result);
    }
}

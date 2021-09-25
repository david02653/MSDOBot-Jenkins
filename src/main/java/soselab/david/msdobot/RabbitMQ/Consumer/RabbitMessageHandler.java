package soselab.david.msdobot.RabbitMQ.Consumer;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Service.JDAConnect;

import java.nio.charset.StandardCharsets;

/**
 * handle each message from rabbitmq server
 */
@Service
public class RabbitMessageHandler {

    private final JDAConnect jdaService;
    private final String rabbitmqChannel;

    public RabbitMessageHandler(JDAConnect jdaConnect, Environment env){
        this.jdaService = jdaConnect;
        this.rabbitmqChannel = env.getProperty("discord.channel.rabbitmq");
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
        jdaService.send(rabbitmqChannel, output);
    }
}

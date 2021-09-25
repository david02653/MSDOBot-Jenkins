package soselab.david.msdobot.Controller.DiscordEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import soselab.david.msdobot.Entity.AdditionalQuestion.ChannelAdditionalQuizList;
import soselab.david.msdobot.Entity.AdditionalQuestion.Question;
import soselab.david.msdobot.Entity.Rasa.IntentSet;
import soselab.david.msdobot.Exception.MessageSearchException;
import soselab.david.msdobot.Exception.RequestFailException;
import soselab.david.msdobot.Service.AdditionalQAService;
import soselab.david.msdobot.Service.IntentHandleService;
import soselab.david.msdobot.Service.JDAMessageHandler;
import soselab.david.msdobot.Service.RasaService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * this class will listen to Discord onMessage received event
 */
@Service
public class DiscordMessageEvent extends ListenerAdapter {

    /* required spring handled service */
    private final RasaService rasa;
    private final IntentHandleService intentHandler;
    private final JDAMessageHandler jdaMsgHandler;
    private final AdditionalQAService aqaService;

    private HashMap<String, ChannelAdditionalQuizList> additionQAList;

    /**
     * constructor for spring, instance are injected by spring
     * @param additionalQAService instance of AdditionalQAService class
     * @param rasa instance of RasaService class
     * @param intentHandle inject by spring, handle intent analyze and define what to do
     * @param jdaMsgHandler inject by spring, handle jda message-related service
     */
    @Autowired
    public DiscordMessageEvent(AdditionalQAService additionalQAService, RasaService rasa, IntentHandleService intentHandle, JDAMessageHandler jdaMsgHandler){
        this.rasa = rasa;
        this.intentHandler = intentHandle;
        this.jdaMsgHandler = jdaMsgHandler;
        this.aqaService = additionalQAService;

        // load additional question list
        this.additionQAList = additionalQAService.getMap();
    }

    /**
     * listen MessageReceivedEvent
     * handle what to do with incoming message here
     * notice that you can only send message with size smaller than 2000
     * use api to store massive response message, send api url instead of raw message
     *
     * note:
     * if message start with no decorator: normal message
     *   -> send to rasa intent analyze -> check what to do
     * if message start with decorator '!': additional question list from yaml setting file
     *   -> check additional question list
     *     -> response with assigned method (result in file, result from api, etc), check the question file for details
     * @param event message received event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event){
//        System.out.println("print author");
//        System.out.println(event.getAuthor());
//        event.getAuthor().openPrivateChannel();

        if(event.isFromType(ChannelType.PRIVATE)){
            if(!event.getAuthor().isBot()) {
                System.out.printf("[private message] %s: %s\n", event.getAuthor().getName(), event.getMessage().getContentDisplay());
                event.getChannel().sendMessage("ok ok").queue();
            }
        }else{
            // [server name][server id][channel name] username: message-content
            System.out.printf("[%s][%s][%s] %s: %s\n", event.getGuild().getName(), event.getTextChannel().getName(), event.getGuild().getId(), Objects.requireNonNull(event.getMember()).getEffectiveName(), event.getMessage().getContentDisplay());

            /* respond when speak is not a bot */
            if(!event.getAuthor().isBot()){
                /* message received from discord, remove all space from message head */
                String msgReceived = event.getMessage().getContentDisplay().strip();

                /* check if message have multi-request */
                try{
                    if(checkMultiMessage(msgReceived)){
                        /* contains multiple message */
                        System.out.println("[DEBUG][DiscordMessageEvent]: Multiple input message detected.");
                        ArrayList<String> msgList = getMultiMessage(msgReceived); // divide input into single messages
                        System.out.print("[DEBUG][detected message]");
                        System.out.println(msgList);
                        for(String msg: msgList){
                            // handle each message
                            System.out.println("[DEBUG][DiscordMessageEvent][multiple message handle]: " + msg);
                            jdaMsgHandler.sendMessage(event.getTextChannel(), normalMessageHandle(event.getChannel().getName(), msg));
                        }
                    }else{
                        // single message
                        jdaMsgHandler.sendMessage(event.getTextChannel(), normalMessageHandle(event.getChannel().getName(), msgReceived));
                    }
                }catch (JsonProcessingException je){
                    jdaMsgHandler.sendMessage(event.getTextChannel(), "Something goes wrong in api request, might by some json parsing error, check api related service");
                    System.out.println("[DEBUG][DiscordMessageEvent] maybe some json related error happened.");
                    je.printStackTrace();
                }catch (RequestFailException re) {
                    jdaMsgHandler.sendMessage(event.getTextChannel(), "Something goes wrong when firing api request");
                    System.out.println("[DEBUG][DiscordMessageEvent] maybe some api request error happened.");
                    re.printStackTrace();
                }
            }
        }
    }

    /**
     * if received message contains '|', determine as multi-request
     * expect message like this: 'msg1 | msg2 | msg3'
     * @param msg received message
     * @return if there are plural incoming message
     */
    private boolean checkMultiMessage(String msg){
        return msg.contains("|");
    }

    /**
     * split message with '|', return as string arrayList
     * @param msg received message
     * @return result arraylist
     */
    private ArrayList<String> getMultiMessage(String msg){
        String[] msgList = msg.split("\\|");
        ArrayList<String> result = new ArrayList<>();
        for(String token: msgList)
            result.add(token.strip());
        return result;
    }

    /**
     * suppose ONE message received, check what to do
     * 1. additional question check
     * 1-1. fixed question
     * 1-2. regex question
     * 2. rasa question check
     * @param channelName which channel received message
     * @param msg incoming message
     */
    private MessageEmbed normalMessageHandle(String channelName, String msg) throws JsonProcessingException, RequestFailException {
        MessageEmbed result;
        /* check additional question */
        /* if nothing found, send message to rasa */
        Question AQASearchResult;
        try{
            AQASearchResult = aqaService.checkMatched(channelName, msg);
        }catch (MessageSearchException e){
            AQASearchResult = null;
            System.out.println("[DEBUG][normalMessageHandle][" + msg + "]: nothing found from additional QA list ! Redirect message to Rasa to check any intent fits this user input.");
        }
        if(AQASearchResult == null){
            // no result found from additional question, check rasa
//            result = rasaMessageHandle(msg);
            result = rasaAnalyze(msg);
        }else{
            // found matched question, return answer from additional question
            System.out.println("[DEBUG][normalMessageHandle][" + msg + "]: matched additional question found, try to reply with additional question list.");
            System.out.println(AQASearchResult);
            result = handleAdditionalQARequest(AQASearchResult, msg);
        }
        return result;
    }

    /**
     * default message input, send to Rasa to analyze intent
     * do stuffs with detected intent and send back to discord
     *
     * normal message handle (rasa analyze)
     * @param input user input
     * @return handle result
     */
    private String rasaMessageHandle(String input) throws JsonProcessingException, RequestFailException {
        IntentSet detectedIntent = rasa.analyzeIntent(input);
        // send intent to intentHandler to check next move
        // send message back to frontend
        String correspondMsg = intentHandler.checkIntent(detectedIntent);
        return correspondMsg;
    }
    private MessageEmbed rasaAnalyze(String input) throws RequestFailException {
        IntentSet detectedIntent = rasa.analyzeIntent(input);
        List<MessageEmbed> resp = intentHandler.checkJenkinsIntent(detectedIntent);
        return resp.get(0);
    }

//    /**
//     * handle requests for additional question
//     *
//     *  21/08/11 update
//     *  change to new workflow, SUSPEND this method
//     *
//     * @param event incoming onMessage event
//     * @param input raw message of user input
//     */
//    private void handleAdditionalQARequest(@NotNull MessageReceivedEvent event, String input) throws Exception {
//        String msg = parse(input);
//        String currentChannelName = event.getTextChannel().getName().toLowerCase();
//        TextChannel currentChannel = event.getTextChannel();
//        if(additionQAList.get(currentChannelName) != null){
//            String result = "";
//            /* do stuff if current channel have questions in list */
//            Question question = additionQAList.get(currentChannelName).get(msg.toLowerCase());
//            if(question != null){
//                String quizResource = question.getResource().toLowerCase();
//                switch (quizResource){
//                    case "rest":
//                        /* handle addition question with resource from rest */
//                        result = AdditionalQAService.restRequest(question.getSource(), question.getMethod());
//                        break;
//                    case "rasa":
//                        /* handle addition question with resource from rasa*/
//                        result = intentHandler.checkIntent(rasa.analyzeIntent(msg.toLowerCase()));
//                        break;
//                    default:
//                        /* extract answer from setting file */
//                        result = question.getAnswer();
//                        break;
//                }
//                if(result != null)
//                    jdaMsgHandler.sendMessage(currentChannel, result);
//                else
//                    System.out.println("[DEBUG][addition_QA] null result !");
//            }else {
////                event.getTextChannel().sendMessage("[Warning] No Additional question found or question list is not available.").queue();
//                jdaMsgHandler.sendMessage(event.getTextChannel(), "[Warning] No Additional question found or question list is not available.");
//            }
//        }else {
////            event.getTextChannel().sendMessage("[DEBUG] something goes wrong with setting file.").queue();
//            jdaMsgHandler.sendMessage(event.getTextChannel(), "[DEBUG] something goes wrong with setting file.");
//        }
//    }

    /**
     * check answer from given Question instance
     * expect user's question input is already confirmed in given Question instance
     * @param target target Question instance
     * @param inputMsg user input
     * @return answer found from Question instance
     */
    private MessageEmbed handleAdditionalQARequest(Question target, String inputMsg){
        String resource = target.getResource().toLowerCase();
        String result = "";
        EmbedBuilder builder = new EmbedBuilder();
        switch (resource){
            case "rest":
                result = AdditionalQAService.restRequest(target.getSource(), target.getMethod());
                break;
            case "file":
                result = target.getAnswer();
                break;
        }
        builder.setDescription(result);
        return builder.build();
    }

    /**
     * remove white space from beginning and convert to lower case
     * @param input input string
     * @return result
     */
    private static String parse(String input){
        String raw = input.substring(1); // remove command head
//        String[] token = raw.split(" ");
//        StringBuilder builder = new StringBuilder();
//        Iterator<String> stringIterator = Arrays.stream(token).iterator();
//        while(stringIterator.hasNext()){
//            builder.append(stringIterator.next());
//            if(stringIterator.hasNext())
//                builder.append(" ");
//        }
        return raw.trim().toLowerCase();
    }
}

package soselab.david.msdobot.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import soselab.david.msdobot.Entity.AdditionalQuestion.ChannelAdditionalQuizList;
import soselab.david.msdobot.Entity.AdditionalQuestion.Question;
import soselab.david.msdobot.Exception.MessageSearchException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class AdditionalQAService {

//    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//    private static AdditionalQuizList quizList;
//    private static ArrayList<AdditionalQuizList> lists = null;
    private final String additionalSettingPath;
//    private final String longMsgHandleApi;
    private static List<ChannelAdditionalQuizList> lists;

    /**
     * read settings from application.properties
     * @param env application context, injected by spring
     */
    @Autowired
    public AdditionalQAService(Environment env){
        additionalSettingPath = env.getProperty("additional.setting.path");
//        longMsgHandleApi = env.getProperty("long.message.handle.url");
        if(loadFile())
            System.out.println("[AdditionalQA][init] setting file load successfully.");
        else
            System.out.println("[DEBUG][AdditionalQA][init] failed to load file.");
    }

    public List<ChannelAdditionalQuizList> getAdditionalQuizList(){
        return lists;
    }

    /**
     * get current question list in HashMap
     * @return current question list
     */
    public HashMap<String, ChannelAdditionalQuizList> getMap(){
        return parse();
    }

    /**
     * read additional question list from yaml file
     * set file path in application.properties
     * @return if yaml file is loaded correctly
     */
    public boolean loadFile(){
//        mapper = new ObjectMapper(new YAMLFactory());
//        mapper.findAndRegisterModules();
        /* parse multiple yaml documents from single file */
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper();
        try {
//            YAMLParser yamlParser = yamlFactory.createParser(new File("./src/main/resources/static/QuizList.yaml"));
            YAMLParser yamlParser = yamlFactory.createParser(new File(additionalSettingPath));
            lists = mapper.readValues(yamlParser, ChannelAdditionalQuizList.class).readAll();
            //System.out.println(lists);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * parse loaded question list in to HashMap
     * return hashmap will look like this: hashmap(channel name, hashmap(question, question object))
     * 'channel name' and 'question' are String
     * return type will be HashMap<String, HashMap<String, Question>>
     *
     * 21/08/11 update
     * change question and pattern type from String to ArrayList
     * from now on return hashmap will be like this: hashmap(channel name, channel question list object)
     * @return loaded question list
     */
    public HashMap<String, ChannelAdditionalQuizList> parse(){
        HashMap<String, ChannelAdditionalQuizList> result = new HashMap<>();
        lists.forEach(channel -> {
//            HashMap<String, Question> map = new HashMap<>();
//            channel.getList().forEach(question -> {
//                map.put(question.getQuestion().toLowerCase(), question);
//            });
//            result.put(channel.getChannel().toLowerCase(), map);
            result.put(channel.getChannel().toLowerCase(), channel);
        });
        return result;
    }

    /**
     * fire request via restTemplate
     * @param source api url
     * @param method get or post
     * @return request result
     */
    public static String restRequest(String source, String method){
        String raw = method.toLowerCase();
        String type = raw.split(":")[0];
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", methodType(method));
        System.out.println(methodType(method));
        HttpEntity<?> entity = new HttpEntity<>(headers);
        switch(type){
            case "get":
                // request get method
                return template.exchange(source, HttpMethod.GET, entity, String.class).getBody();
            case "post":
                // request post method
                break;
            default:
                return "";
        }
        return null;
    }

    /**
     * return string of MediaType for each method
     * @param method method type, xml or json
     * @return string of MediaType
     */
    private static String methodType(String method){
        String[] token = method.split(":");
        switch(token[token.length - 1]){
            case "xml":
                return MediaType.APPLICATION_XML_VALUE;
            case "json":
                return MediaType.APPLICATION_JSON_VALUE;
        }
        return MediaType.ALL_VALUE;
    }

    /**
     * find question list by channel name
     * return null if no channel name matched
     * @param channelName target channel name
     * @return question list, null if nothing matched
     */
    public ChannelAdditionalQuizList getTargetList(String channelName) throws MessageSearchException {
        for(ChannelAdditionalQuizList list: lists){
            if(list.getChannel().equals(channelName))
                return list;
        }
        throw new MessageSearchException();
    }

    /**
     * check if input message matches any fixed question or regex pattern
     * use channel name to get channel question list
     * call function to check if any question or pattern matches
     * return matched Question instance if matches
     * return null if nothing matches
     * @param channelName channel name
     * @param msg input message
     * @return matched Question instance, return null if nothing matched
     * @throws MessageSearchException if no channel question list found with given channel name
     */
    public Question checkMatched(String channelName, String msg) throws MessageSearchException {
        /* get target channel */
        ChannelAdditionalQuizList targetList = getTargetList(channelName);
        if(targetList == null)
            throw new MessageSearchException();
        /* check if any regex format or fixed format question matched */
        for(Question list: targetList.getList()){
            if(checkExistingQuestion(list.getQuestion(), msg))
                return list;
            if(checkExistingRegex(list.getPattern(), msg))
                return list;
        }
        return null;
    }

    /**
     * check if input question is already existed in fixed question list
     * input message is already processed by strip()
     * @param questionList fixed question list
     * @param input input message
     * @return if matched any existed question
     */
    private boolean checkExistingQuestion(ArrayList<String> questionList, String input){
        String exampleLow;
        for(String example: questionList){
            exampleLow = example.toLowerCase();
            if(exampleLow.equals(input))
                return true;
        }
        return false;
    }

    /**
     * check if input question matches any pattern in pattern list
     * input message is already processed by strip()
     * @param patternList question pattern list
     * @param input input message
     * @return if matches any pattern
     */
    private boolean checkExistingRegex(ArrayList<String> patternList, String input){
        for(String pattern: patternList){
            if(Pattern.matches(pattern, input))
                return true;
        }
        return false;
    }
}

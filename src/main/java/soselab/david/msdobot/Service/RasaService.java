package soselab.david.msdobot.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import soselab.david.msdobot.Entity.Rasa.Intent;
import soselab.david.msdobot.Entity.Rasa.IntentSet;

/**
 * define functions who needs to interact with rasa endpoint
 */
@Service
public class RasaService {

    private final String RASA_ENDPOINT;

    @Autowired
    public RasaService(Environment env){
        this.RASA_ENDPOINT = env.getProperty("env.setting.rasa.url");
    }

    // save stages if multi-turns of conversation is required, unused for now
    private int stage;

    /**
     * rasa api
     * send message to rasa to analyze intent and service name
     * should receive something like this:
     * [{"recipient_id":"test","text":"{'intent': 'action_build_fail', 'service': 'none'}"}]
     * note that return message from rasa might not be a legal json string, fix received message if necessary
     * @param data message
     * @return rasa reaction
     */
    public IntentSet analyzeIntent(String data){

        // implement function stage_rasa(...)
        // POST method to rasa endpoint, return with json type data {intent, data}

        RestTemplate template = new RestTemplate();
        String path = RASA_ENDPOINT + "/webhooks/rest/webhook";

        // set request content
        JsonObject content = new JsonObject();
        content.addProperty("message", data);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(content.toString(), headers);
        ResponseEntity<String> response = template.exchange(path, HttpMethod.POST, entity, String.class);

        System.out.println(response);
//        System.out.println("response body : " + response.getBody());
        /* jsonObject version string pre-handle */
//        String raw = changeFormat(response.getBody());
//        System.out.println("target json : " + raw);

        /* gson string pre-handle */
        String raw = noSlash(response.getBody());
        System.out.println("raw : " + raw);

        try{
            /* Gson version, no backslash and no '"' surround object required */
            Gson gson = new Gson();
            Intent analyseResult = gson.fromJson(raw, Intent.class);
            System.out.println("gson analyzed: " + analyseResult);
            /* available version, backslash and no '"' surround object required */
//            JSONObject object = new JSONObject(raw);
//            String inner = object.getString("text");
//            System.out.println(inner);
//            JSONObject innerObject = new JSONObject(inner);
//            System.out.println(innerObject);
//            if(innerObject.has("service")) System.out.println(innerObject.getString("service"));
//            else System.out.println("service not exist");
//            System.out.println(innerObject.getString("intent"));

            return analyseResult.getText();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public String changeFormat(String raw){
        String[] token = raw.split(",");
        String data = "{";
        for(int i=1; i<token.length; i++) {
            data += token[i];
            if(i != token.length-1) data += ",";
        }
        data = data.replace("'", "\"");
        StringBuilder builder = new StringBuilder(data);
        builder.deleteCharAt(builder.length()-1);
//        builder.deleteCharAt(0);

        String temp = builder.toString();
        StringBuilder result = new StringBuilder("{");
        String[] second = temp.split("");
        for(int i=1; i<second.length; i++){
//            System.out.println("current=[" + second[i] + "], i=" + i + ", result=[" + result + "]");
            int open = StringUtils.countOccurrencesOf(temp.substring(0, i), "{");
            int close = StringUtils.countOccurrencesOf(temp.substring(0, i), "}");
            if(second[i].equals("\"")){
                if(open - close == 1){
                    if(second[i+1].equals("{") || second[i-1].equals("}")) {
                        result.append("");
                    }
                    else{
                        result.append("\\\"");
                    }
//                    result += second[i];
                }else{
                    if(second[i-1].equals("\\")) {
                        result.append(second[i]);
                    }else{
                        result.append("\\\"");
                    }
                }
                continue;
            }
            result.append(second[i]);
        }

        return result.toString();
    }

    /**
     * make received message from rasa a legal json string format
     * @param raw raw message
     * @return processed message
     */
    public String noSlash(String raw){
        String[] token = raw.split("");
        StringBuilder result = new StringBuilder();
        for(String t: token){
            if(t.equals("\\")) continue;
            if(t.equals("'")){
                result.append("\"");
                continue;
            }
            result.append(t);
        }
        result.deleteCharAt(result.length()-1);
        result.deleteCharAt(0);

        String temp = result.toString();
        StringBuilder output = new StringBuilder("{");
        String[] second = temp.split("");
        for(int i=1; i<second.length; i++){
//            System.out.println("current=[" + second[i] + "], i=" + i + ", result=[" + result + "]");
            int open = StringUtils.countOccurrencesOf(temp.substring(0, i), "{");
            int close = StringUtils.countOccurrencesOf(temp.substring(0, i), "}");
            if(second[i].equals("\"")){
                if(open - close == 1){
                    if(second[i+1].equals("{") || second[i-1].equals("}")) {
                        continue;
                    }
                }
            }
            output.append(second[i]);
        }
        return output.toString();
    }

    /**
     * use api to reload rasa service
     */
    public void reloadRasa(){
        // todo: rasa reload
    }


}

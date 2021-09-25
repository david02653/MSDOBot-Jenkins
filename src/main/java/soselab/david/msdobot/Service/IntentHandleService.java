package soselab.david.msdobot.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import soselab.david.msdobot.Entity.Eureka.Application;
import soselab.david.msdobot.Entity.Eureka.EurekaResponse;
import soselab.david.msdobot.Entity.Eureka.Instance;
import soselab.david.msdobot.Entity.Jenkins.*;
import soselab.david.msdobot.Entity.Rasa.IntentSet;
import soselab.david.msdobot.Exception.RequestFailException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class should be used to handle incoming messages in discord bot
 * send message via jdaMessage handler
 * check intent by using rasa service
 * find question by using additional question service
 *
 * note: need to pass JDA instance via parameter, in case needs to use jda message service, etc
 */
@Service
public class IntentHandleService {

    /* endpoint path from properties */
    private final String zuulEndpoint;
    private final String rasaEndpoint;
    private final String jenkinsEndpoint;
    private final String eurekaEndpoint;
    private final String jenkinsUser;
    private final String jenkinsPw;
    private final String jenkinsToken;

    /* required spring managed service */
//    private final RasaService rasaService;
//    private final AdditionalQAService additionalQAService;
    private final LongMessageService longMessageService;

    /* required service */
    private JDAMessageHandler jdaMessageHandler;

    @Autowired
    public IntentHandleService(Environment env, LongMessageService longMessageService){
        this.zuulEndpoint = env.getProperty("env.setting.zuul.url");
        this.rasaEndpoint = env.getProperty("env.setting.rasa.v2.url");
        this.jenkinsEndpoint = env.getProperty("env.setting.jenkins.url");
        this.eurekaEndpoint = env.getProperty("env.setting.eureka.url");
        this.jenkinsUser = env.getProperty("env.setting.jenkins.user");
        this.jenkinsPw = env.getProperty("env.setting.jenkins.pw");
        this.jenkinsToken = env.getProperty("env.setting.jenkins.token");

//        this.rasaService = rasa;
//        this.additionalQAService = aqa;
        this.longMessageService = longMessageService;
    }


    /**
     * check if intent object contains service_name
     * otherwise, do stuffs with object intent
     *
     * note: implements stage_check_intent in MSABot
     * @param set target intent set object
     * @return correspond result
     */
    public String checkIntent(IntentSet set) throws RequestFailException, JsonProcessingException {
        String intent = set.getIntent();
        String service = set.getJobName();

        /* bot help and intent with no service dependency */
        switch (intent){
            case "bot_help":
                return botHelp();
            case "action_service_env":
                return requestServiceEnv();
            case "action_service_health":
                return requestHealthData();
            default:
                break;
        }
        /* no service name found */
        if(service == null || service.equals("") || service.equals("none"))
            return "service name required but no service name found.";
        /* check intent with service dependency */
        switch (intent){
            case "action_detail_api": // missing definition in MSABot, check it out
                break;
            case "action_connect_error":
                return connectError(service);
            case "action_build_fail":
//                return requestLastFailedBuildStatus(service);
                return requestLastTestCaseDetail(service);
            case "action_service_info":
                return requestServiceInfo(service);
            case "action_service_using_info":
                return requestServiceUsingInfo(service);
            case "action_service_api_list":
                return requestApiList(service);
            default:
                break;
        }
        return "We have no idea what are you talking about, please consider ask again in another way.";
    }

    /**
     * handle intent about jenkins
     * should return MessageEmbed object
     * workflow:
     * 1. check incoming intent
     * 2. if service name required, check if service name received, return error message if something goes wrong with service name
     * 3. return result (EmbedMessage)
     * @param set incoming intent
     */
    public List<MessageEmbed> checkJenkinsIntent(IntentSet set) throws RequestFailException {
        String intent = set.getIntent();
        String service = set.getJobName();
        switch(intent){
            case "greet":
                return greeting();
            case "help":
                return jenkinsBotHelp();
        }
        /* no service name required */
        switch(intent){
            case "ask_job_view_list":
                return getJenkinsViewsMsg();
            case "ask_job_list":
                return getJenkinsViewDetailMsg("All");
            case "ask_plugin_info":
                return getJenkinsPluginMsg();
            case "ask_system_all_build":
                return getJenkinsBuildRssMsg("all");
            case "ask_system_failed_build":
                return getJenkinsBuildRssMsg("failed");
            case "ask_system_latest_build":
                return getJenkinsBuildRssMsg("latest");
            case "ask_jenkins_log":
                return getJenkinsLogMsg("all");
            case "ask_jenkins_severe_log":
                return getJenkinsLogMsg("severe");
            case "ask_jenkins_warning_log":
                return getJenkinsLogMsg("warning");
            // get env info
            // get credential
        }
        /* object name required, check if service name exist */
        if(service == null || service.equals("None")){
            // target name missing, return error message
            System.out.println("[DEBUG][intent analyze][missing target service]: " + intent);
            return missingJenkinsObjectName(set);
        }
        switch (intent){
            case "ask_view_detail":
                return getJenkinsViewDetailMsg(service);
            case "ask_job_health_report":
                return getHealthReportMsg(service);
            case "ask_build_result":
                return getJobLastBuildResultMsg(service);
            case "ask_job_test_report":
                return getJenkinsTestReportMsg(service);
            case "ask_last_build_report":
                return getJenkinsJobOverview(service);
            // get job git update
        }
        return noMatchedIntent(intent);
    }

    private List<MessageEmbed> noMatchedIntent(String intent){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("No intent found matched with [" + intent + "].");
        builder.setTitle("Oops");
        return Collections.singletonList(builder.build());
    }

    //- greet
    //- help
    //- ask_job_view_list
    //- ask_view_detail
    //- ask_job_list
    //- ask_job_test_report
    //- ask_job_health_report
    //- ask_build_result
    //- ask_last_build_report
    //- ask_job_git_update
    //- ask_system_latest_build
    //- ask_system_failed_build
    //- ask_system_all_build
    //- ask_jenkins_log
    //- ask_jenkins_server_log
    //- ask_jenkins_warning_log
    //- ask_plugin_info
    //- ask_env_info
    //- ask_credential_info

    public List<MessageEmbed> greeting(){
        EmbedBuilder builder = new EmbedBuilder();
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setDescription("Hi ! How can i help you ?");
        return Collections.singletonList(builder.build());
    }

    /**
     * create user guide message
     * build an embed message and return
     * note: implements action_bot_help in MSABot
     * @return help message
     */
    public String botHelp(){
        return "1. Search the service health data.\n" +
                "2. Search the service's information.\n" +
                "3. Search the service's overview.\n" +
                "4. Search the service's api list.\n" +
                "5. Search the env setting.\n" +
                "6. Search the build data on Jenkins.\n" +
                "~~7. Search the connection status on Eureka.~~ \n" +
                "~~8. Get the dependency graph from VMAMV.~~ ";
    }

    public List<MessageEmbed> jenkinsBotHelp(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
//        embedBuilder.setAuthor("MSDOBot Jenkins");
        embedBuilder.setColor(Color.WHITE);
        embedBuilder.setTitle("How to use :thinking:");
        String helpMsg =
                "1. Get current view list on Jenkins.\n" +
                "2. Get detail info about single view.\n" +
                "3. Get current job list on Jenkins.\n" +
                "4. Get health report about single job.\n" +
                "5. Get build result about single job.\n" +
                "6. Get test report about single job.\n" +
                "7. Get info about recent build on Jenkins.\n" +
                "8. Get info about failed build on Jenkins.\n" +
                "9. Get info about latest build on Jenkins.\n" +
                "10. Get Jenkins system log.\n" +
                "11. Get Jenkins system log (severe).\n" +
                "12. Get Jenkins system log (warning).\n" +
                "13. Get info about current plugin on Jenkins.";
        embedBuilder.setDescription(helpMsg);
        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.setFooter("bot help");
        return Collections.singletonList(embedBuilder.build());
    }

    /**
     * request eureka endpoint to get app data (xml)
     * parse xml to extract target information
     * note: implements action_service_health in MSABot, Eureka service
     * @throws JsonProcessingException error parsing eureka xml response
     */
    public String requestHealthData() throws JsonProcessingException, RequestFailException {
        String url = eurekaEndpoint + "/eureka/apps";
        String rawData = "";
        ResponseEntity<String> response = fireRestExchange(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
        rawData = response.getBody();
        if(rawData == null || response.getStatusCodeValue() != 200){
            /* api request failed somehow, do something */
            System.out.println("[DEBUG][healthData] eureka api request failed or return with null body.");
            throw new RequestFailException();
        }
        XmlMapper mapper = new XmlMapper();
        EurekaResponse result = mapper.readValue(replaceReServed(rawData), EurekaResponse.class);
        HashMap<String, Instance> map = eurekaMapSerialize(result);

        /* build result string in to arrayList */
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for(Map.Entry<String, Instance> entry: map.entrySet()){
            String name = entry.getKey();
            Instance instance = entry.getValue();
            builder.append(++count);
            builder.append(".[");
            builder.append(name);
            builder.append("]: ");
            builder.append(instance.getStatus());
            builder.append("\n");
        }
        return builder.toString();
    }

    /**
     * request service api list from zuul endpoint
     * use zuul and swagger
     * note: implements action_service_api_list in MSABot
     * check zuul api first to see the return format details
     * you cannot use this function if the api dead anyway
     * @param service target service name
     * @return embed message
     */
    public String requestApiList(String service){
        String url = zuulEndpoint + "/" + service + "/v2/api-docs/";
        System.out.println("[DEBUG][requestApiList][url]: " + url + "");
        ResponseEntity<String> response = null;
        try {
            response = fireRestExchange(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        } catch (RequestFailException e) {
            return "Something goes wrong with zuul api request.";
        }
        /* use Gson, parse response json string into JsonObject */
        Gson gson = new Gson();
        JsonObject zuulResp = gson.fromJson(response.getBody(), JsonObject.class);
        /* extract target information */
        String title = zuulResp.getAsJsonObject("info").get("title").getAsString();
        String contactName = zuulResp.getAsJsonObject("info").getAsJsonObject("contact").get("name").getAsString();
        JsonObject tag = zuulResp.getAsJsonArray("tags").get(0).getAsJsonObject();
        String tagName = tag.get("name").getAsString();
        String tagDescription = tag.get("description").getAsString();
        JsonObject pathInfo = zuulResp.getAsJsonObject("paths");
        /* build result */
        StringBuilder builder = new StringBuilder();
        builder.append(title).append(" : ").append(tagName).append("\n");
        builder.append("(").append(tagDescription).append(")\n");
        builder.append("If this service went some problems, please call ").append(contactName).append("\n");
        builder.append("Some api intro : \n");
        for(Map.Entry<String, JsonElement> entry: pathInfo.entrySet()){
            String pathToken = entry.getKey();
            String apiSummary = entry.getValue().getAsJsonObject().getAsJsonObject("get").get("summary").getAsString();
            builder.append(zuulEndpoint).append("/").append(service).append(pathToken).append(" : ")
                    .append(apiSummary).append("\n");
        }
        builder.append("See detail information on Swagger. ")
                .append(zuulEndpoint).append("/").append(service).append("/swagger-ui.html").append("\n");
        builder.append("(The data was collected from your Zuul server and Swagger api.)");
        return builder.toString();
    }

    /**
     * request system environment setting from eureka endpoint
     * note: implements action_service_env in MSABot, Eureka service
     * check eureka api first to see the return format details
     * you cannot use this function if the api dead anyway
     * @return result information
     */
    public String requestServiceEnv(){
        String url = eurekaEndpoint + "/env/";
        System.out.println("[DEBUG][requestServiceEnv][url]: " + url + "");
        ResponseEntity<String> response = null;
        try {
            response = fireRestExchange(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        } catch (RequestFailException e) {
            return "Something goes wrong with eureka api request.";
        }
        /* use Gson, parse response json string into JsonObject */
        Gson gson = new Gson();
        JsonObject eurekaResp = gson.fromJson(response.getBody(), JsonObject.class);
        /* extract target information */
        String targetPropertyName = "applicationConfig: [classpath:/application.properties]";
        JsonObject targetSection = eurekaResp.getAsJsonObject(targetPropertyName);
        /* build result string */
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<String, JsonElement> entry: targetSection.entrySet()){
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue().getAsString();
            builder.append("The env ").append(propertyName).append(" is : ").append(propertyValue).append("\n");
        }
        return builder.toString();
    }

    /**
     * request service detail information from zuul endpoint
     * this api will need a service name in lower case
     * note: implements action_service_info in MSABot, Zuul and Actuator service
     * check zuul api first to see the return format details
     * you cannot use this function if the api dead anyway
     * @param service target service name
     * @return result information
     */
    public String requestServiceInfo(String service){
        String url = zuulEndpoint + "/" + service.toLowerCase() + "/info/";
        System.out.println("[DEBUG][requestServiceInfo][url]: " + url + "");
        ResponseEntity<String> response = null;
        try {
            response = fireRestExchange(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        } catch (RequestFailException e) {
            return "Something goes wrong with Zuul api request.";
        }
        /* use Gson, parse response json string into JsonObject */
        Gson gson = new Gson();
        JsonObject resp = gson.fromJson(response.getBody(), JsonObject.class);
        /* extract target information */
        String version = resp.get("version").getAsString();
        String group = resp.getAsJsonObject("build").get("group").getAsString();
        JsonObject gitInfo = resp.getAsJsonObject("git");
        String remoteUrl = gitInfo.getAsJsonObject("remote").getAsJsonObject("origin").get("url").getAsString();
        String branch = gitInfo.get("branch").getAsString();
        String user = gitInfo.getAsJsonObject("commit").getAsJsonObject("user").get("name").getAsString();
        String userMail = gitInfo.getAsJsonObject("commit").getAsJsonObject("user").get("email").getAsString();
        String commitCount = gitInfo.getAsJsonObject("total").getAsJsonObject("commit").get("count").getAsString();
        /* build result string */
        String builder = "Information found on service " + service + ".\n" +
                "Version: " + version + "\n" +
                "Group: " + group + "\n" +
                "Git URL: " + remoteUrl + "\n" +
                "Git branch: " + branch + "\n" +
                "Git user: " + user + "\n" +
                "(If this service has some problems contact him/her by : " + userMail + "\n" +
                "Total commit count: " + commitCount + "\n" +
                "See detail information on Swagger! : " + zuulEndpoint + "/" + service + "/swagger-ui.html" + "\n" +
                "(The data was collected from your Zuul server and Actuator api.)";
        return builder;
    }

    /**
     * request for service using_info
     * note: implements action_service_using_info in MSABot, Zuul Swagger and Actuator api
     * check zuul api first to see the return format details
     * you cannot use this function if the api dead anyway
     * @param service target service name
     * @return result message
     */
    public String requestServiceUsingInfo(String service){
        String serviceUrl = zuulEndpoint + "/" + service + "/v2/api-docs/";
        String traceUrl = zuulEndpoint + "/" + service + "/trace";
        System.out.println("[DEBUG][requestServiceUsingInfo][serviceUrl]: " + serviceUrl + "");
        System.out.println("[DEBUG][requestServiceUsingInfo][traceUrl]: " + traceUrl + "");
        ResponseEntity<String> serviceInfoResp = null;
        ResponseEntity<String> traceResp = null;
        try{
            serviceInfoResp = fireRestExchange(serviceUrl, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
            traceResp = fireRestExchange(traceUrl, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        }catch (RequestFailException e){
            return "Something goes wrong with Zuul api request.";
        }
        Gson gson = new Gson();
        JsonObject serviceInfo = gson.fromJson(serviceInfoResp.getBody(), JsonObject.class);
        JsonArray traceInfo = gson.fromJson(traceResp.getBody(), JsonArray.class);
        /* extract information from json object */
        // loop serviceInfo to get all api list
        ArrayList<String> apiList = new ArrayList<>();
        HashMap<String, String> traceList = new HashMap<>();
        HashMap<String, Integer> resultList = new HashMap<>();
        JsonObject serviceApi = serviceInfo.getAsJsonObject("paths");
        for(Map.Entry<String, JsonElement> entry: serviceApi.entrySet())
            apiList.add(entry.getKey());
        for(JsonElement ele: traceInfo){
            traceList.put(ele.getAsJsonObject().getAsJsonObject("info").get("path").getAsString(), ele.getAsJsonObject().getAsJsonObject("info").getAsJsonObject("headers").getAsJsonObject("response").get("status").getAsString());
        }
        System.out.println(apiList);
        // compare with traceInfo to get what we are after
        int totalCount = 0;
        for(Map.Entry<String, String> ele: traceList.entrySet()){
            System.out.println("[trace][" + ele.getKey() + "][" + ele.getValue() + "]");
            String pathName = ele.getKey();
            String httpStatus = ele.getValue();
            if(apiList.contains(pathName)){
                if(!resultList.containsKey(httpStatus))
                    resultList.put(httpStatus, 1);
                else
                    resultList.put(httpStatus, resultList.get(httpStatus) + 1);
                totalCount++;
            }
        }
        if(resultList.size() == 0){
            return "Seems like this service has no usage record. The total using amount is : 0";
        }
        // create chart parameter
        StringBuilder labelName = new StringBuilder();
        StringBuilder labelValue = new StringBuilder();
        for(Map.Entry<String, Integer> ele: resultList.entrySet()){
            labelName.append(ele.getKey()).append(",");
            labelValue.append(ele.getValue()).append(",");
        }
        labelName = new StringBuilder(labelName.substring(0, labelName.length() - 1));
        labelValue = new StringBuilder(labelValue.substring(0, labelValue.length() - 1));
        /* build result message */
        String builder = "The total using amount is : " + totalCount + "\n" +
                "See the pie chart I prepared for you! \n" +
                "https://quickchart.io/chart?c={type:'pie',data:{labels:[" + labelName + "],datasets:[{data:[" + labelValue + "]}]}}";
        return builder;
    }

    /**
     * send the service's connection error data back
     * note: implements action_connect_error in MSABot, Eureka api
     * @param service target service name
     * @return result message
     */
    public String connectError(String service){
        return "Here is service \"" + service + "\"'s action_connect_error.";
    }

    /**
     * fire request to jenkins api to access job related information
     * this method gathers information about last failed build
     * check jenkins api first to see the return format details
     * you cannot use this function if the api dead anyway
     *
     * suspend and use requestLastTestCaseDetail(String service) instead, for now
     * @param service target service name
     * @return build information
     */
    public String requestLastFailedBuildStatus(String service){
        // api format -> http(s)://<user>:<pw>@<server-host>/job/<service-name>/api/json
        String url = jenkinsEndpoint + "/job/" + service + "/api/json";
        System.out.println("[DEBUG][requestLastFailedBuildStatus][url]: " + url + "");
        ResponseEntity<String> response = null;
        try {
            response = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        } catch (RequestFailException e) {
            return "Something goes wrong with jenkins api request.";
        }
        Gson gson = new Gson();
        JsonObject jenkinsObj = gson.fromJson(response.getBody(), JsonObject.class);
        StringBuilder builder = new StringBuilder();
        if(jenkinsObj.get("lastFailedBuild") == null)
            builder.append("There is no failed build record.");
        else{
            String buildNumber = jenkinsObj.getAsJsonObject("lastFailedBuild").get("number").getAsString();
            String serviceDisplayName = jenkinsObj.get("fullDisplayName").getAsString();
            builder.append("Service[").append(serviceDisplayName).append("]'s last build is build").append(buildNumber).append("\n");
            builder.append("Check it on your Jenkins server").append(jenkinsEndpoint).append("/job/").append(serviceDisplayName).append("/").append(buildNumber).append("/");
        }
        return builder.toString();
    }

    /**
     * fire request to jenkins api to access specific job information, including build number and test case details
     * this method gathers information about test case details
     * check jenkins api first to see the return format details
     * you cannot use this function if the api dead anyway
     * @param service target service name
     * @return test case details
     */
    public String requestLastTestCaseDetail(String service){
        // build status api format -> http(s)://<user>:<pw>@<server-host>/job/<service-name>/api/json
        // build detail -> http(s)://<user>:<pw>@<server-host>/job/<service-name>/<build-number>/testReport/api/json
        try{
//            String[] hostToken = jenkinsEndpoint.split("//");
//            String statusUrl = hostToken[0] + "//" + jenkinsUser + ":" + token + "@" + hostToken[1] + "/job/" + service + "/api/json";
            String statusUrl = jenkinsEndpoint + "/job/" + service + "/api/json";
            System.out.println("[DEBUG][requestLastTestCaseDetail][statusUrl]: " + statusUrl + "");
            ResponseEntity<String> statusResp = fireBasicAuthJenkinsRequest(statusUrl, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
            Gson gson = new Gson();
            JsonObject statusObj = gson.fromJson(statusResp.getBody(), JsonObject.class);
            String buildNumber = statusObj.getAsJsonObject("lastBuild").get("number").getAsString();
//            String url = hostToken[0] + "//" + jenkinsUser + ":" + jenkinsPw + "@" + hostToken[1] + "/job/" + service + "/" + buildNumber + "/testReport/api/json";
            String url = jenkinsEndpoint + "/job/" + service + "/" + buildNumber + "/testReport/api/json";
            System.out.println("[DEBUG][requestLastTestCaseDetail][Url]: " + url + "");
            ResponseEntity<String> detailResp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
            JsonObject detailObj = gson.fromJson(detailResp.getBody(), JsonObject.class);
            StringBuilder builder = new StringBuilder();
            JsonArray suites = detailObj.get("suites").getAsJsonArray();
            int totalDuration = detailObj.get("duration").getAsInt();
            int totalFailedCount = detailObj.get("failCount").getAsInt();
            int totalPassedCount = detailObj.get("passCount").getAsInt();
            int totalSkipCount = detailObj.get("skipCount").getAsInt();
            JenkinsTestReport testReport = JenkinsTestReport.createJenkinsTestReport(totalDuration, totalFailedCount, totalPassedCount, totalSkipCount);
            testReport.setTestSuites(getTestSuiteDetails(suites));
//            testReport.setReportDetailUrl(additionalQAService.insertMessage(testReport.getTestSuites().toString()));
            testReport.setReportDetailUrl(longMessageService.getUrl(longMessageService.addMessage(gson.toJson(testReport.getTestSuites()))));
//            return testReport.toString();
            return gson.toJson(testReport);
        }catch (RequestFailException e){
            return "Something goes wrong wit jenkins api request.";
        }
    }

    /**
     * do same thing as requestLastTestCaseDetail, return JenkinsTestReport object instead
     * @param service target service name
     * @return test case object
     */
    public JenkinsTestReport getLastTestCaseDetail(String service) throws RequestFailException {
        // request for last build number
        String statusUrl = jenkinsEndpoint + "/job/" + service + "/api/json?depth=2&tree=lastBuild[number]";
        System.out.println("[DEBUG][requestLastTestCaseDetail][statusUrl]: " + statusUrl + "");
        ResponseEntity<String> statusResp = fireBasicAuthJenkinsRequest(statusUrl, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        Gson gson = new Gson();
        JsonObject statusObj = gson.fromJson(statusResp.getBody(), JsonObject.class);
        String buildNumber = statusObj.getAsJsonObject("lastBuild").get("number").getAsString();
        String url = jenkinsEndpoint + "/job/" + service + "/" + buildNumber + "/testReport/api/json";
        System.out.println("[DEBUG][requestLastTestCaseDetail][Url]: " + url + "");
        // request for last build test report
        ResponseEntity<String> detailResp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        JsonObject detailObj = gson.fromJson(detailResp.getBody(), JsonObject.class);
        StringBuilder builder = new StringBuilder();
        JsonArray suites = detailObj.get("suites").getAsJsonArray();
        int totalDuration = detailObj.get("duration").getAsInt();
        int totalFailedCount = detailObj.get("failCount").getAsInt();
        int totalPassedCount = detailObj.get("passCount").getAsInt();
        int totalSkipCount = detailObj.get("skipCount").getAsInt();
        // create result instance
        JenkinsTestReport testReport = JenkinsTestReport.createJenkinsTestReport(totalDuration, totalFailedCount, totalPassedCount, totalSkipCount);
        testReport.setTestSuites(getTestSuiteDetails(suites));
//            testReport.setReportDetailUrl(additionalQAService.insertMessage(testReport.getTestSuites().toString()));
        testReport.setReportDetailUrl(longMessageService.getUrl(longMessageService.addMessage(gson.toJson(testReport.getTestSuites()))));
        return testReport;
    }

    /**
     * extract information from test suites
     * including suite name and suite duration
     * call another method to access detail test case information
     * @param testSuites JsonArray of test Suites
     * @return test suites information
     */
    private ArrayList<JenkinsTestSuite> getTestSuiteDetails(JsonArray testSuites){
        ArrayList<JenkinsTestSuite> suiteReports = new ArrayList<>();
        for(JsonElement element: testSuites){
            JsonObject suite = element.getAsJsonObject();
            int duration = suite.get("duration").getAsInt();
            String suiteName = suite.get("name").getAsString();
            JenkinsTestSuite temp = JenkinsTestSuite.createNewTestSuite(suiteName, duration);
            temp.setTestCases(getTestCaseDetails(suite.get("cases").getAsJsonArray()));
            suiteReports.add(temp);
        }
        return suiteReports;
    }

    /**
     * extract detail information from test report json array
     * this method will extract test name, test duration, error detail , error stack trace anb test status
     * note that error detail and error stack trace might be null
     * @param testCases
     * @return
     */
    private ArrayList<JenkinsTestCase> getTestCaseDetails(JsonArray testCases){
        JenkinsTestCase testCaseTemplate;
        ArrayList<JenkinsTestCase> report = new ArrayList<>();
        for(JsonElement element: testCases){
            JsonObject testCase = element.getAsJsonObject();
            int duration = testCase.get("duration").getAsInt();
            String testName = testCase.get("name").getAsString();
            String details;
            String stackTrace;
            if(testCase.get("errorDetails").isJsonNull())
                details = null;
            else
                details = testCase.get("errorDetails").getAsString();
            if(testCase.get("errorStackTrace").isJsonNull())
                stackTrace = null;
            else
                stackTrace = testCase.get("errorStackTrace").getAsString();
            String status = testCase.get("status").getAsString();

            if(details == null)
                details = "non";
            // cut stack trace message
            if(stackTrace == null)
                stackTrace = "non";
            else if(stackTrace.contains("Exception"))
                stackTrace = stackTrace.split("Exception")[0] + "Exception";
            testCaseTemplate = JenkinsTestCase.createNewTestCase(testName, duration, details, stackTrace, status);
            report.add(testCaseTemplate);
        }
        return report;
    }

    /**
     * fire http request using RestTemplate
     * this method use RestTemplate.exchange()
     * @param httpMethod method type: get, post, etc.
     * @param headerList required header list
     * @return responseBody from exchange
     */
    public static ResponseEntity<String> fireRestExchange(String url, HttpMethod httpMethod, HashMap<String, String> headerList) throws RequestFailException {
        RestTemplate template = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headerList.forEach(headers::set);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = null;
        try{
            result  = template.exchange(url, httpMethod, entity, String.class);
        }catch (Exception e){
            e.printStackTrace();
            throw new RequestFailException();
        }
//        return template.exchange(url, httpMethod, entity, String.class);
        return result;
    }

    private ResponseEntity<String> fireBasicAuthJenkinsRequest(String url, HttpMethod httpMethod, HashMap<String, String> headerList) throws RequestFailException{
        RestTemplate template = new RestTemplate();
        RestTemplateBuilder templateBuilder = new RestTemplateBuilder();
        template = templateBuilder.basicAuthentication(jenkinsUser, jenkinsToken).build();
        HttpHeaders headers = new HttpHeaders();
        headerList.forEach(headers::set);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        ResponseEntity<String> result = null;
        try{
            result  = template.exchange(url, httpMethod, entity, String.class);
        }catch (Exception e){
            e.printStackTrace();
            throw new RequestFailException();
        }
//        return template.exchange(url, httpMethod, entity, String.class);
        return result;
    }

    /**
     * replace reserved words in the source string
     * @param source string format xml
     * @return cleaned xml string
     */
    private String replaceReServed(String source){
        return source.replace("class=", "source=");
    }

    /**
     * serialize eureka bean to hashmap
     * @param raw eureka bean
     * @return hashmap
     */
    private HashMap<String, Instance> eurekaMapSerialize(EurekaResponse raw){
        ArrayList<Application> list = raw.getAppList();
        HashMap<String, Instance> map = new HashMap<>();
        for (Application app : list) {
            map.put(app.getName(), app.getInstance());
        }
        return map;
    }

    /**
     * extract jenkins package name from full package name
     * expect incoming string format looks like "hudson.model.FreeStyleProject"
     * use regex to extract the third part of string
     * @return jenkins job style, empty string if nothing matches found
     */
    private String extractJenkinsPackageName(String jenkinsClassName){
        Pattern jenkinsJobStyle = Pattern.compile("^hudson[.]model[.](.*?)$");
        Matcher matcher = jenkinsJobStyle.matcher(jenkinsClassName);
        if(matcher.find())
            return matcher.group(1);
        return "";
    }

    /**
     * get all jobs on jenkins.
     * response json format should look like this:
     * {
     *  "_class": "hudson.model.Hudson",
     *  "jobs": [
     *           {"_class": <job type>, "name": <job name>, "url": <job url>},
     *           {...}
     *          ]
     * }
     * we need job type, job name and job url here
     * @return hashmap of jenkins jobs: map key is job name, map value is job instance
     */
    public HashMap<String, JenkinsJob> getJenkinsJobs() throws RequestFailException {
        // request url
        String query = "/api/json?tree=jobs[name,url]";
        String url = jenkinsEndpoint + query;
        // fire request to jenkins endpoint
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        // parse and extract result
        HashMap<String, JenkinsJob> result = new HashMap<>();
        try{
            Gson gson = new Gson();
            JsonObject responseJson = gson.fromJson(resp.getBody(), JsonObject.class);
            JsonArray jobs = responseJson.get("jobs").getAsJsonArray();
            for(JsonElement job: jobs){
                JsonObject jobObject = job.getAsJsonObject();
                String style = extractJenkinsPackageName(jobObject.get("_class").getAsString());
                String name = jobObject.get("name").getAsString();
                String jobUrl = jobObject.get("url").getAsString();
                result.put(name, new JenkinsJob(style, name, jobUrl));
            }
        }catch (JSONException je){
            je.printStackTrace();
            System.out.println("[DEBUG][getJenkinsJobs] something goes wrong while parsing json, maybe object received null value.");
            return result;
        }
        return result;
    }

    /**
     * get all jenkins view list
     * only return with view name and view link
     * @return
     * @throws RequestFailException
     */
    public List<MessageEmbed> getJenkinsViewsMsg() throws RequestFailException {
        HashMap<String, JenkinsView> viewList = getAllJenkinsViews();
        EmbedBuilder builder = new EmbedBuilder();
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setTitle("Jenkins view list");
        for(Map.Entry<String, JenkinsView> entry: viewList.entrySet()){
            if(builder.getFields().size() >= 25){
                builder.setFooter("too many views ! consider check jenkins for complete information.");
                break;
            }
            String viewName = entry.getKey();
//            JenkinsView view = entry.getValue();
            builder.addField(new MessageEmbed.Field(viewName, generateViewLink(viewName), false));
        }
        return Collections.singletonList(builder.build());
    }

    /**
     * get detail of certain jenkins view
     * return with view name, job list, job url
     * @param viewName
     * @return
     * @throws RequestFailException
     */
    public List<MessageEmbed> getJenkinsViewDetailMsg(String viewName) throws RequestFailException {
        HashMap<String, JenkinsView> viewList = getAllJenkinsViews();
        EmbedBuilder builder = new EmbedBuilder();
        JenkinsView targetView;
        if(viewName.equals("All"))
            targetView = viewList.get("all");
        else
            targetView = viewList.get(viewName);
        System.out.println(targetView);
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setTitle("[Jenkins View] " + viewName);
        for(Map.Entry<String, JenkinsJob> view: targetView.getJobList().entrySet()){
            if(builder.getFields().size() >= 25){
                builder.setFooter("too many jobs ! check [this link]("+generateViewLink(viewName)+") for complete information.");
                break;
            }
            String jobName = view.getKey();
            String jobType = view.getValue().getType();
            String jobUrl = view.getValue().getUrl();
            builder.addField(new MessageEmbed.Field(jobName, "["+jobType+"] "+jobUrl, false));
        }
        return Collections.singletonList(builder.build());
    }

    private String generateViewLink(String viewName){
        return jenkinsEndpoint + "/view/" + viewName;
    }

    /**
     * get all views on jenkins.
     * response json format should look like this:
     * {
     *     "_class": "hudson.model.Hudson",
     *     "views": [
     *         {
     *             "_class": <jenkins view type>,
     *             "jobs": [
     *                 {
     *                     "_class": <jenkins job type>,
     *                     "name": <job name>,
     *                     "url": <job url>
     *                 },
     *                 {...}
     *             ],
     *             "name": <view name>
     *         },
     *         {...}
     *     ]
     * }
     * we need jenkins job type/name/url and view name here
     * @return hashmap of jenkins views
     */
    public HashMap<String, JenkinsView> getAllJenkinsViews() throws RequestFailException {
        // request url
        String query = "/api/json?tree=views[name,jobs[name,url]]";
        String url = jenkinsEndpoint + query;
        // fire request to jenkins endpoint
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        // parse and extract result
        HashMap<String, JenkinsView> result = new HashMap<>();
        try{
            Gson gson = new Gson();
            JsonObject respJson = gson.fromJson(resp.getBody(), JsonObject.class);
            JsonArray views = respJson.get("views").getAsJsonArray();
            for(JsonElement view: views){
                JsonObject viewObj = view.getAsJsonObject();
                String viewType = viewObj.get("_class").getAsString();
//                if(extractJenkinsPackageName(viewType).equals("AllView"))
//                    continue; // ignore all job view
                String viewName = viewObj.get("name").getAsString();
                HashMap<String, JenkinsJob> jobMap = new HashMap<>();
                // extract job information
                for(JsonElement job: viewObj.get("jobs").getAsJsonArray()){
                    String jobName = job.getAsJsonObject().get("name").getAsString();
                    String jobStyle = extractJenkinsPackageName(job.getAsJsonObject().get("_class").getAsString());
                    String jobUrl = job.getAsJsonObject().get("url").getAsString();
                    jobMap.put(jobName, new JenkinsJob(jobStyle, jobName, jobUrl));
                }
                result.put(viewName, new JenkinsView(viewName, jobMap));
            }
        }catch (JSONException je){
            je.printStackTrace();
            System.out.println("[DEBUG][getJenkinsJobs] something goes wrong while parsing json, maybe object received null value.");
            return result;
        }
        return result;
    }

    /**
     * get jobs on jenkins by view name
     * @param targetViewName target view name
     * @return
     */
    public JenkinsView getJenkinsJobsByView(String targetViewName) throws RequestFailException {
        // request url
        String query = "/api/json?tree=views[name,jobs[name,url]]";
        String url = jenkinsEndpoint + query;
        // fire request to jenkins endpoint
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        // parse and extract result
        JenkinsView resultView = new JenkinsView();
        HashMap<String, JenkinsJob> jobList = new HashMap<>();
        try{
            Gson gson = new Gson();
            JsonObject respJson = gson.fromJson(resp.getBody(), JsonObject.class);
            JsonArray views = respJson.get("views").getAsJsonArray();
            for(JsonElement view: views){
                String viewName = view.getAsJsonObject().get("name").getAsString();
                if(!viewName.equals(targetViewName)) continue;
                resultView.setName(viewName);
                for(JsonElement job: view.getAsJsonObject().get("jobs").getAsJsonArray()){
                    String jobName = job.getAsJsonObject().get("name").getAsString();
                    String jobStyle = extractJenkinsPackageName(job.getAsJsonObject().get("_class").getAsString());
                    String jobUrl = job.getAsJsonObject().get("url").getAsString();
                    jobList.put(jobName, new JenkinsJob(jobStyle, jobName, jobUrl));
                }
                resultView.setJobList(jobList);
                break;
            }
        }catch (JSONException je){
            je.printStackTrace();
            System.out.println("[DEBUG][getJenkinsJobs] something goes wrong while parsing json, maybe object received null value.");
            return resultView;
        }
        return resultView;
    }

    /**
     * create embed message about missing target name (job name or view name)
     * @return embed message
     */
    private List<MessageEmbed> missingJenkinsObjectName(IntentSet intent){
        EmbedBuilder builder = new EmbedBuilder();
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setTitle("Something goes wrong");
//        builder.setDescription("Seems like you are willing to query about some view or job but target name is missing.");
        builder.setDescription("Maybe service name is missing.\n" +
                "[" + intent.getIntent() + "] queried. Entity [" + intent.getJobName() + "] found.");
        builder.setTimestamp(Instant.now());
        builder.setFooter("Bot Hint");
        return Collections.singletonList(builder.build());
    }

    public List<MessageEmbed> getHealthReportMsg(String targetName) throws RequestFailException {
        String query = "/job/" + targetName + "/api/json?depth=2&tree=healthReport[*]";
        String url = jenkinsEndpoint + query;
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        Gson gson = new Gson();
        JsonObject response = gson.fromJson(resp.getBody(), JsonObject.class);
        JsonArray healthReport = response.getAsJsonArray("healthReport");
        JsonObject buildStability = healthReport.get(0).getAsJsonObject();
        JsonObject testResult = healthReport.get(1).getAsJsonObject();
        EmbedBuilder builder = new EmbedBuilder();
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setTitle("[Jenkins Health Report] " + targetName);
        builder.addField(buildStability.get("description").getAsString().split(":")[0], buildStability.get("description").getAsString().split(":")[1].strip(), false);
        builder.addField(testResult.get("description").getAsString().split(":")[0], testResult.get("description").getAsString().split(":")[1].strip(), false);
        return Collections.singletonList(builder.build());
    }

    public List<MessageEmbed> getJenkinsJobOverview(String jobName) throws RequestFailException {
        String query = "/job/" + jobName + "/api/json?depth=2&tree=description,url,healthReport[*],keepDependencies,lastBuild[*[*[*[*]]]],scm[userRemoteConfigs[*],branches[*]]";
        String url = jenkinsEndpoint + query;
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        EmbedBuilder builder = new EmbedBuilder();
        Gson gson = new Gson();
        JsonObject jsonResp = gson.fromJson(resp.getBody(), JsonObject.class); // json object of complete response from endpoint
        JsonArray healthReport = jsonResp.get("healthReport").getAsJsonArray(); // health report: test result, build stability
        String testResult = healthReport.get(0).getAsJsonObject().get("description").getAsString();
        String buildStability = healthReport.get(1).getAsJsonObject().get("description").getAsString();
        JsonObject scm = jsonResp.get("scm").getAsJsonObject();
        String branch = scm.get("branches").getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
        String remoteUrl = scm.get("userRemoteConfigs").getAsJsonArray().get(0).getAsJsonObject().get("url").getAsString();
        JsonObject lastBuildInfo = jsonResp.get("lastBuild").getAsJsonObject();
        String displayName = lastBuildInfo.get("displayName").getAsString();
        int duration = lastBuildInfo.get("duration").getAsInt();
        String buildResult = lastBuildInfo.get("result").getAsString();
        String jobUrl = lastBuildInfo.get("url").getAsString();
        JsonArray gitCommit = lastBuildInfo.get("changeSet").getAsJsonObject().get("items").getAsJsonArray();
        /* embed message content build */
        builder.setTitle("[Build Report] :" + jobName);
        builder.setDescription("This job is build from branch '" + branch.split("/")[1] + "' on " + remoteUrl + "\n" +
                               "Check " + jobUrl + " for complete detail.");
        builder.addField("Build Number", displayName, true);
        builder.addField("Build Result", buildResult, true);
        builder.addField("Duration", Integer.toString(duration), true);
        builder.addField("[Health Report] Test Result", testResult.split(":")[1].strip(), false);
        builder.addField("[Health Report] Build Stability", buildStability.split(":")[1].strip(), false);
        builder.addField("Git Info", gitUpdateCount(gitCommit), false);
        return Collections.singletonList(builder.build());
    }

    /**
     * count how many commit pushed since the last build, of example: this build is #3 then count all commit between #2 and #3
     * count how many file are affected in those commit
     * return summary info
     * @param gitUpdateInfo git update json array
     * @return git update simple summary info
     */
    private String gitUpdateCount(JsonArray gitUpdateInfo){
        int commitNumber = gitUpdateInfo.size();
        Set<String> affectedFile = new HashSet<>();
        for(JsonElement item: gitUpdateInfo){
            JsonArray affectedPaths = item.getAsJsonObject().get("affectedPaths").getAsJsonArray();
            for(JsonElement paths: affectedPaths){
                affectedFile.add(paths.getAsString());
            }
        }
        return commitNumber + " commits pushed since last build, " + affectedFile.size() + " file are affected.";
    }

    public List<MessageEmbed> getJobLastBuildResultMsg(String jobName) throws RequestFailException {
        String query = "/job/" + jobName + "/api/json?depth=2&tree=lastBuild[result]";
        String url = jenkinsEndpoint + query;
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        Gson gson = new Gson();
        JsonObject result = gson.fromJson(resp.getBody(), JsonObject.class);
        EmbedBuilder builder = new EmbedBuilder();
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setTitle("[Jenkins Last Build Result] " + jobName);
        builder.setDescription(result.get("lastBuild").getAsJsonObject().get("result").getAsString());
        return Collections.singletonList(builder.build());
    }

    public List<MessageEmbed> getJenkinsPluginMsg() throws RequestFailException {
        String query = "/pluginManager/api/json?depth=2&tree=plugins[shortName,longName,version,active,hasUpdate,url]";
        String url = jenkinsEndpoint + query;
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        Gson gson = new Gson();
        JsonObject response = gson.fromJson(resp.getBody(), JsonObject.class);
        JsonArray pluginList = response.get("plugins").getAsJsonArray();
        EmbedBuilder builder = new EmbedBuilder();
//        builder.setAuthor("MSDOBot Jenkins");
        builder.setTitle("Plugin Information");
        // create detail info using long message service
        builder.setDescription(longMessageService.getUrl(longMessageService.addMessage(pluginList.toString())));
        for(JsonElement element: pluginList){
            if(builder.getFields().size() >=25){
                builder.setFooter("too many plugins! check url for more detail information.");
            }
            JsonObject plugin = element.getAsJsonObject();
            builder.addField(plugin.get("shortName").getAsString(), createJenkinsPluginDescription(plugin.get("version").getAsString(), plugin.get("hasUpdate").getAsBoolean(), plugin.get("active").getAsBoolean()), false);
        }
        return Collections.singletonList(builder.build());
    }

    private String createJenkinsPluginDescription(String version, Boolean hasUpdate, Boolean active){
        String updateStatus;
        if(hasUpdate)
            updateStatus = "has available update";
        else
            updateStatus = "no available update";
        if(active)
            return "[active] version: " + version + ", " + updateStatus;
        else
            return "[inactive] version: " + version + ", " + updateStatus;
    }

    public List<MessageEmbed> getJenkinsLogMsg(String type){
        String query = "/log/rss";
        String title = "";
        switch (type){
            case "severe":
                query = query.concat("?level=SEVERE");
                title = "Jenkins Log (SEVERE)";
                break;
            case "warning":
                query = query.concat("?level=WARNING");
                title = "Jenkins Log (WARNING)";
                break;
            default:
                title = "Jenkins Log (All)";
        }
        String url = jenkinsEndpoint + query;
        EmbedBuilder builder = new EmbedBuilder();
        try{
            ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
            Document doc = JenkinsXMLParser.loadXML(resp.getBody());
            JenkinsLog resultLog = JenkinsXMLParser.parseJenkinsLog(doc, longMessageService);
            // parse jenkins log into discord embed message
//            builder.setAuthor("MSDOBot Jenkins");
            builder.setTitle(title);
            builder.setDescription(url);
            ArrayList<JenkinsLogEntry> entryList = resultLog.getEntries();
            if(entryList == null){
                // no log found
                builder.addField("No Log Found !", "log is current empty.", false);
                System.out.println("[DEBUG][Jenkins Log Msg] no available log record found.");
                return Collections.singletonList(builder.build());
            }
            for(JenkinsLogEntry log: entryList){
                if(builder.getFields().size() >= 25) {
                    builder.setFooter("too many logs, check url for complete information.");
                    break;
                }
                builder.addField(log.getPublishedTime(), log.getContent(), false);
            }
        } catch (RequestFailException e) {
            System.out.println("[DEBUG][JenkinsLog] url request goes wrong.");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("[DEBUG][JenkinsLog] xml parser creating goes wrong.");
            e.printStackTrace();
        } catch (IOException | SAXException e) {
            System.out.println("[DEBUG][JenkinsLog] xml parsing goes wrong.");
            e.printStackTrace();
        }
        return Collections.singletonList(builder.build());
    }
//    public MessageEmbed getJenkinsSevereLogMsg(){
//        String query = "/log/rss?level=SEVERE";
//        String url = jenkinsEndpoint + query;
//        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
//        Document doc = JenkinsXMLParse.loadXML(resp.getBody());
//        return null;
//    }
//    public MessageEmbed getJenkinsWarningLogMsg(){
//        String query = "/log/rss?level=WARNING";
//        String url = jenkinsEndpoint + query;
//        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
//        Document doc = JenkinsXMLParse.loadXML(resp.getBody());
//        return null;
//    }

    public List<MessageEmbed> getJenkinsBuildRssMsg(String type){
        String query = "";
        String buildType = "";
        switch(type){
            case "failed":
                query = "/view/all/rssFailed";
                buildType = "Failed Build";
                break;
            case "latest":
                query = "/view/all/rssLatest";
                buildType = "Latest Build";
                break;
            default:
                query = "/view/all/rssAll";
                buildType = "All Recent Build";
        }
        String url = jenkinsEndpoint + query;
        EmbedBuilder builder = new EmbedBuilder();
        try{
            ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
            Document doc = JenkinsXMLParser.loadXML(resp.getBody());
//            builder.setAuthor("MSDOBot Jenkins");
            builder.setTitle(buildType);
            builder.setDescription(url);
            ArrayList<JenkinsBuildFeed> buildList = JenkinsXMLParser.parseJenkinsBuildAtomFeed(doc);
            for(JenkinsBuildFeed build: buildList){
                if(builder.getFields().size() >= 25){
                    builder.setFooter("too many builds, check url for complete information");
                    break;
                }
                builder.addField(build.getJobName() + " #" + build.getBuildNumber(),
                        "status: " + build.getSummary() + "\n" + "published time: " + build.getPublishedTime(),
                        false);
            }
        } catch (RequestFailException e) {
            System.out.println("[DEBUG][BuildRss] url request goes wrong.");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            System.out.println("[DEBUG][BuildRss] xml parser creating goes wrong.");
            e.printStackTrace();
        } catch (IOException | SAXException e) {
            System.out.println("[DEBUG][BuildRss] xml parsing goes wrong.");
            e.printStackTrace();
        }
        return Collections.singletonList(builder.build());
    }
//    public MessageEmbed getJenkinsRecentFailedBuildMsg(){
//        String query = "/view/all/rssFailed";
//        String url = jenkinsEndpoint + query;
//        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
//        Document doc = JenkinsXMLParser.loadXML(resp.getBody());
//        return null;
//    }
//    public MessageEmbed getJenkinsLatestBuildMsg(){
//        String query = "/view/all/rssLatest";
//        String url = jenkinsEndpoint + query;
//        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_XML_VALUE)));
//        Document doc = JenkinsXMLParser.loadXML(resp.getBody());
//        return null;
//    }

    /**
     * turn jenkinsTestReport into discord message
     * will contain multiple embed message if multiple suite found
     * note that only up to 10 messageEmbed can be placed in a message
     * only up to 25 field can be placed in a messageEmbed
     * @param service
     * @return
     */
    public List<MessageEmbed> getJenkinsTestReportMsg(String service){
//        List<MessageEmbed> embedList = new ArrayList<>();
        EmbedBuilder builder = new EmbedBuilder();
        try {
            JenkinsTestReport report = getLastTestCaseDetail(service);
//            builder.setAuthor("MSDOBot Jenkins");
            builder.setTitle("[Jenkins Test Report] " + service);
            builder.setDescription("Duration: " + report.getTotalDuration() + "\n" +
                                   "PassCount: " + report.getPassedCount() + "\n" +
                                   "FailedCount: " + report.getFailedCount() + "\n" +
                                   "SkipCount: " + report.getSkipCount() + "\n" +
                                   "Detail: " + report.getReportDetailUrl());
            for(JenkinsTestSuite suite: report.getTestSuites()){
                String suiteName = suite.getSuiteName();
                if(builder.getFields().size() >= 25){
                    builder.setFooter("too many test case, check url for more complete information.");
                    break;
                }
                for(JenkinsTestCase testCase: suite.getTestCases()){
                    if(builder.getFields().size() >= 25){
                        // set footer and return current builder result
                        builder.setFooter("too many test case, check url for more complete information.");
                        break;
                    }
                    builder.addField("[" + suiteName + "] " + testCase.getName(),
                                     "duration: " + testCase.getDuration() + "\n" +
                                           "error details: " + testCase.getErrorDetails() + "\n" +
                                           "error stack trace: " + testCase.getErrorStackTrace() + "\n" +
                                           "status: " + testCase.getStatus(),
                                     false);
                }
            }
        } catch (RequestFailException e) {
            System.out.println("[DEBUG][JenkinsTestReportMsg] something goes wrong when requesting url.");
            e.printStackTrace();
        }
        return Collections.singletonList(builder.build());
    }
}

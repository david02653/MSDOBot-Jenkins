package soselab.david.msdobot.Service;

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
    private final String jenkinsEndpoint;
    private final String jenkinsUser;
    private final String jenkinsToken;

    /* required spring managed service */
    private final LongMessageService longMessageService;

    /* required service */
    private JDAMessageHandler jdaMessageHandler;

    @Autowired
    public IntentHandleService(Environment env, LongMessageService longMessageService){
        this.jenkinsEndpoint = env.getProperty("env.setting.jenkins.url");
        this.jenkinsUser = env.getProperty("env.setting.jenkins.user");
        this.jenkinsToken = env.getProperty("env.setting.jenkins.token");
        this.longMessageService = longMessageService;
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
            case "ask_view_test_report_overview":
                return jenkinsTestReportOverviewMsg(service);
            // get job git update
        }
        return noMatchedIntent(intent);
    }

    /**
     * send response message if intent detected have no implement method
     * @param intent detected intent
     * @return response message
     */
    private List<MessageEmbed> noMatchedIntent(String intent){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("No intent found matched with [" + intent + "].");
        builder.setTitle("Oops");
        return Collections.singletonList(builder.build());
    }

    // intent implement list:
    //- ask_job_git_update
    //- ask_env_info
    //- ask_credential_info

    /**
     * say hi to user
     * @return message
     */
    public List<MessageEmbed> greeting(){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription("Hi ! How can i help you ?");
        return Collections.singletonList(builder.build());
    }

    /**
     * send help message
     * @return help message
     */
    public List<MessageEmbed> jenkinsBotHelp(){
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.WHITE);
        embedBuilder.setTitle("How to use :thinking:");
        String helpMsg =
                "1. Get current view list on Jenkins.\n" +
                "2. Get detail info about single view.\n" +
                "3. Get current job list on Jenkins.\n" +
                "4. Get health report about single job.\n" +
                "5. Get build result about single job.\n" +
                "6. Get test report about single job.\n" +
                "7. Get last build report about single job.\n" +
                "8. Get info about recent build on Jenkins.\n" +
                "9. Get info about failed build on Jenkins.\n" +
                "10. Get info about latest build on Jenkins.\n" +
                "11. Get Jenkins system log.\n" +
                "12. Get Jenkins system log (severe).\n" +
                "13. Get Jenkins system log (warning).\n" +
                "14. Get info about current plugin on Jenkins.";
        embedBuilder.setDescription(helpMsg);
        embedBuilder.setTimestamp(Instant.now());
        embedBuilder.setFooter("bot help");
        return Collections.singletonList(embedBuilder.build());
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
        return result;
    }

    /**
     * fire GET request to basic auth required endpoint
     * in this case, request jenkins api
     * @param url request api url
     * @param httpMethod method type
     * @param headerList request header
     * @return response
     * @throws RequestFailException throw error if something goes wrong
     */
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
        return result;
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
     * @return message
     * @throws RequestFailException if something goes wrong while requesting data from jenkins
     */
    public List<MessageEmbed> getJenkinsViewsMsg() throws RequestFailException {
        HashMap<String, JenkinsView> viewList = getAllJenkinsViews();
        EmbedBuilder builder = new EmbedBuilder();
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
     * @param viewName view name
     * @return message
     * @throws RequestFailException if something goes wrong while requesting data from jenkins
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
        builder.setTitle("[Jenkins View] " + viewName);
        if(targetView == null){
            builder.setDescription("Nothing found in this view. Maybe view name typo ?");
            return Collections.singletonList(builder.build());
        }
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

    /**
     * return jenkins view url
     * @param viewName view name
     * @return jenkins view url
     */
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
     * create embed message about missing target name (job name or view name)
     * @return embed message
     */
    private List<MessageEmbed> missingJenkinsObjectName(IntentSet intent){
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Something goes wrong");
//        builder.setDescription("Seems like you are willing to query about some view or job but target name is missing.");
        builder.setDescription("Maybe service name is missing.\n" +
                "[" + intent.getIntent() + "] queried. Entity [" + intent.getJobName() + "] found.");
        builder.setTimestamp(Instant.now());
        builder.setFooter("Bot Hint");
        return Collections.singletonList(builder.build());
    }

    /**
     * get health report by job name
     * @param targetName job name
     * @return message of health report
     * @throws RequestFailException throw if something goes wrong while requesting data from jenkins
     */
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
        builder.setTitle("[Jenkins Health Report] " + targetName);
        builder.addField(buildStability.get("description").getAsString().split(":")[0], buildStability.get("description").getAsString().split(":")[1].strip(), false);
        builder.addField(testResult.get("description").getAsString().split(":")[0], testResult.get("description").getAsString().split(":")[1].strip(), false);
        return Collections.singletonList(builder.build());
    }

    /**
     * build message of jenkins job build report, use last build report as data source
     * should contain job description, job url, job health report, job git change, job last build info
     * @param jobName job name
     * @return message
     * @throws RequestFailException throw if something goes wrong while requesting jenkins data
     */
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
        builder.setTitle("[Build Report] " + jobName);
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

    /**
     * get last build result
     * should get something like: SUCCESS, UNSTABLE, etc
     * @param jobName job name
     * @return message of build result
     * @throws RequestFailException throw if something goes wrong while requesting data from jenkins
     */
    public List<MessageEmbed> getJobLastBuildResultMsg(String jobName) throws RequestFailException {
        String query = "/job/" + jobName + "/api/json?depth=2&tree=lastBuild[result]";
        String url = jenkinsEndpoint + query;
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        Gson gson = new Gson();
        JsonObject result = gson.fromJson(resp.getBody(), JsonObject.class);
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("[Jenkins Last Build Result] " + jobName);
        builder.setDescription(result.get("lastBuild").getAsJsonObject().get("result").getAsString());
        return Collections.singletonList(builder.build());
    }

    /**
     * get information about plugin on current jenkins server
     * @return message of jenkins plugin
     * @throws RequestFailException throw if something goes wrong while requesting data from jenkins
     */
    public List<MessageEmbed> getJenkinsPluginMsg() throws RequestFailException {
        String query = "/pluginManager/api/json?depth=2&tree=plugins[shortName,longName,version,active,hasUpdate,url]";
        String url = jenkinsEndpoint + query;
        ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
        Gson gson = new Gson();
        JsonObject response = gson.fromJson(resp.getBody(), JsonObject.class);
        JsonArray pluginList = response.get("plugins").getAsJsonArray();
        EmbedBuilder builder = new EmbedBuilder();
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

    /**
     * generate summary description about jenkins plugin
     * @param version plugin version
     * @param hasUpdate plugin update info
     * @param active plugin activation info
     * @return plugin message
     */
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

    /**
     * get message about jenkins system log
     * @param type log type (all, severe, warning)
     * @return message about jenkins log
     */
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

    /**
     * get message about jenkins build information
     * @param type build type (latest, failed, all)
     * @return message about jenkins build information
     */
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

    /**
     * turn jenkinsTestReport into discord message
     * will contain multiple embed message if multiple suite found
     * note that only up to 10 messageEmbed can be placed in a message
     * only up to 25 field can be placed in a messageEmbed
     * @param service
     * @return
     */
    public List<MessageEmbed> getJenkinsTestReportMsg(String service){
        EmbedBuilder builder = new EmbedBuilder();
        try {
            JenkinsTestReport report = getLastTestCaseDetail(service);
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

    /**
     * get all test report by view name
     * @param viewName target view name
     * @return all jenkins report summary message
     */
    public List<MessageEmbed> jenkinsTestReportOverviewMsg(String viewName){
        String query = "/api/json?depth=3&tree=views[name,url,jobs[name,lastBuild[id,duration,result,actions[*]{3,}]]]";
        String url = jenkinsEndpoint + query;
        Gson gson = new Gson();
        EmbedBuilder builder = new EmbedBuilder();
        /* make sure view name 'all' always works */
        if(viewName.equals("ALL") || viewName.equals("All"))
            viewName = "all";
        builder.setTitle("[General view testReport] " + viewName);
        try{
            ResponseEntity<String> resp = fireBasicAuthJenkinsRequest(url, HttpMethod.GET, new HashMap<>(Collections.singletonMap("Accept", MediaType.APPLICATION_JSON_VALUE)));
            JsonObject json = gson.fromJson(resp.getBody(), JsonObject.class);
            JsonArray views = json.get("views").getAsJsonArray();
            int totalCountSum =0, skipCountSum = 0, failCountSum = 0, passCountSum = 0;
            for(JsonElement ele: views){
                JsonObject view = ele.getAsJsonObject();
                if(!view.get("name").getAsString().equals(viewName)) continue; // check view name
                JsonArray jobList = view.get("jobs").getAsJsonArray(); // job list of target view
                for(JsonElement job: jobList){
                    JsonElement testObj = job.getAsJsonObject().get("lastBuild").getAsJsonObject().get("actions").getAsJsonArray().get(0);
                    if(testObj == null) continue; // in case no test report found
                    JsonObject testReport = testObj.getAsJsonObject();
                    int total = testReport.get("totalCount").getAsInt();
                    int skip = testReport.get("skipCount").getAsInt();
                    int fail = testReport.get("failCount").getAsInt();
                    int pass = total - skip - fail;
                    totalCountSum += total;
                    skipCountSum += skip;
                    failCountSum += fail;
                    if(builder.getFields().size() < 25){
                        String jobName = job.getAsJsonObject().get("name").getAsString();
                        builder.addField("[Job] " + jobName,
                                         "[TotalCount]: " + total + "\n" +
                                               "[PassCount]: " + pass + "\n" +
                                               "[FailCount]: " + fail + "\n" +
                                               "[SkipCount]: " + skip,
                                         false);
                    }else{
                        builder.setFooter("Too many jobs, check each job for details.");
                    }
                }
            }
            passCountSum = totalCountSum - skipCountSum - failCountSum;
            builder.setDescription("**TotalCount:** " + totalCountSum + "\n" +
                                   "**PassCount:** " + passCountSum + "\n" +
                                   "**FailCount:** " + failCountSum + "\n" +
                                   "**SkipCount:** " + skipCountSum);
        } catch (RequestFailException e){
            e.printStackTrace();
            System.out.println("[DEBUG][JenkinsTestReportOverview] something goes wrong when requesting url.");
        }
        return Collections.singletonList(builder.build());
    }

    private float getTestCasePassRate(int totalCount, int skipCount, int failCount){
        int passCount = totalCount - skipCount - failCount;
        return (float) passCount / totalCount;
    }
}

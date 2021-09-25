package soselab.david.msdobot.Service;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import soselab.david.msdobot.Entity.Jenkins.JenkinsBuildFeed;
import soselab.david.msdobot.Entity.Jenkins.JenkinsLog;
import soselab.david.msdobot.Entity.Jenkins.JenkinsLogEntry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * parse xml(atom feed) format jenkins information
 */
public class JenkinsXMLParser {

    /**
     * parse xml string to Document object
     * @param xml raw xml string
     * @return xml document
     * @throws ParserConfigurationException if something goes wrong while creating new document builder
     * @throws IOException if something goes wrong with turning xml string into byte input stream
     * @throws SAXException if xml parsing goes wrong
     */
    public static Document loadXML(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
        return builder.parse(is);
    }

    /**
     * TESTING VERSION FOR TEST CLASS
     * try to extract information from parsed jenkins log, include general/severe/warning logs
     * @param doc document of parsed jenkins log
     */
    public static void parseJenkinsLog(Document doc){
        doc.getDocumentElement().normalize(); // normalize xml structure
        Element titleElement = (Element) doc.getElementsByTagName("title").item(0);
        String title = titleElement.getTextContent();
        NodeList entryList = doc.getElementsByTagName("entry");
        JenkinsLogEntry logEntry = null;
        for(int count = 0; count < entryList.getLength(); count++){
            Node entry = entryList.item(count);
            if(entry.getNodeType() == Node.ELEMENT_NODE){
                logEntry = new JenkinsLogEntry();
                Element ety = (Element) entry;
                String entryTitle = ety.getElementsByTagName("title").item(0).getTextContent();
                String entryId = ety.getElementsByTagName("id").item(0).getTextContent();
                String publishedTime = ety.getElementsByTagName("published").item(0).getTextContent();
                String content = ety.getElementsByTagName("content").item(0).getTextContent();
                logEntry.setTitle(entryTitle);
                logEntry.setId(entryId);
                logEntry.setPublishedTime(publishedTime);
                logEntry.setContent(removeUselessInfoFromJenkinsLogEntryContent(content));
//                logEntry.setContentDetail(content);
            }
        }
        System.out.println(logEntry);
    }

    /**
     * extract information from parsed jenkins log document
     * if raw entry content is too massive, use long service to store data and set url as detail
     * @param doc parsed jenkins log
     * @param longMessageService used to store content detail
     * @return jenkins log object
     */
    public static JenkinsLog parseJenkinsLog(Document doc, LongMessageService longMessageService){
        JenkinsLog resultLog = new JenkinsLog();
        doc.getDocumentElement().normalize(); // normalize xml structure
        Element titleElement = (Element) doc.getElementsByTagName("title").item(0);
        String title = titleElement.getTextContent();
        resultLog.setTitle(title);
        NodeList entryList = doc.getElementsByTagName("entry");
//        JenkinsLogEntry logEntry = null;
        ArrayList<JenkinsLogEntry> resultEntryList = new ArrayList<>();
        for(int count = 0; count < entryList.getLength(); count++){
            Node entry = entryList.item(count);
            if(entry.getNodeType() == Node.ELEMENT_NODE){
                JenkinsLogEntry logEntry = new JenkinsLogEntry();
                Element ety = (Element) entry;
                String entryTitle = ety.getElementsByTagName("title").item(0).getTextContent();
                String entryId = ety.getElementsByTagName("id").item(0).getTextContent();
                String publishedTime = ety.getElementsByTagName("published").item(0).getTextContent();
                String content = ety.getElementsByTagName("content").item(0).getTextContent();
                logEntry.setTitle(entryTitle);
                logEntry.setId(entryId);
                logEntry.setPublishedTime(publishedTime);
                logEntry.setContent(removeUselessInfoFromJenkinsLogEntryContent(content));
                if(content.split("\n").length >= 3 || content.length() > 2000)
                    logEntry.setContentDetail(longMessageService.getUrl(longMessageService.addMessage(content)));
//                    logEntry.setContentDetail(">>> TEMPORARY REMOVED <<<");
                else
                    logEntry.setContentDetail(content);
                resultEntryList.add(logEntry);
            }
        }
//        System.out.println(logEntry);
//        System.out.println(resultEntryList);
        resultLog.setEntries(resultEntryList);
        return resultLog;
    }

    /**
     * remove useless information (maybe)
     * basically remove all java error trace stack and empty line
     * @param entryContent raw entry content
     * @return processed content
     */
    private static String removeUselessInfoFromJenkinsLogEntryContent(String entryContent){
        String[] contentList = entryContent.split("\n");
        StringBuilder builder = new StringBuilder();
        for(String line: contentList){
            if(!Pattern.matches("^at [a-zA-Z0-9.:()$]+\\)$", line.strip()) && line.strip().length() > 1){
                if(builder.length() > 0)
                    builder.append("; ");
                builder.append(line);
            }
        }
        return builder.toString();
    }

    /**
     * parse jenkins build rss report (all/failed/latest)
     * extract jobName, buildNumber and publishedTime from each entry
     * return arrayList of jenkins build object
     * @param doc parsed jenkins build rss document
     * @return arrayList of jenkins build object
     */
    public static ArrayList<JenkinsBuildFeed> parseJenkinsBuildAtomFeed(Document doc){
        ArrayList<JenkinsBuildFeed> resultBuildList = new ArrayList<>();
        doc.getDocumentElement().normalize(); // normalize xml structure
        NodeList entryList = doc.getElementsByTagName("entry");
        for(int count = 0; count < entryList.getLength(); count++){
            Node entryNode = entryList.item(count);
            if(entryNode.getNodeType() == Node.ELEMENT_NODE){
                JenkinsBuildFeed build = new JenkinsBuildFeed();
                Element entry = (Element) entryNode;
                String entryTitle = entry.getElementsByTagName("title").item(0).getTextContent();
                // extract job name, build number and summary from entry information
                String jobName = getJenkinsBuildJobName(entryTitle);
                String buildNumber = getJenkinsBuildBuildNumber(entryTitle);
                String jobSummary = getJenkinsBuildSummary(entryTitle);
                String publishedTime = entry.getElementsByTagName("published").item(0).getTextContent();
                build.setJobName(jobName);
                build.setBuildNumber(buildNumber);
                build.setSummary(jobSummary);
                build.setPublishedTime(publishedTime);
                resultBuildList.add(build);
            }
        }
        System.out.println(resultBuildList);
        return resultBuildList;
    }

    /**
     * extract job name from jenkins build entry title using regex
     * @param raw raw title
     * @return job name, return 'init-job-name' if nothing captured
     */
    private static String getJenkinsBuildJobName(String raw){
        Pattern jobNamePattern = Pattern.compile("^(.*?) #[0-9]+ \\([a-zA-Z ]+\\)$");
        Matcher matcher = jobNamePattern.matcher(raw);
        if(matcher.find())
            return matcher.group(1);
        return "init-job-name";
    }

    /**
     * extract build number from jenkins build entry title using regex
     * @param raw raw title
     * @return build number, return 'init-build-number' if nothing captured
     */
    private static String getJenkinsBuildBuildNumber(String raw){
        Pattern buildNumberPattern = Pattern.compile("^[a-zA-Z0-9-_ ]+ #(\\d+?) \\([a-zA-Z ]+\\)$");
        Matcher matcher = buildNumberPattern.matcher(raw);
        if(matcher.find())
            return matcher.group(1);
        return "init-build-number";
    }

    /**
     * extract build summary description from jenkins build entry title using regex
     * @param raw raw title
     * @return build summary description, return 'init-build-summary' if nothing captured
     */
    private static String getJenkinsBuildSummary(String raw){
        Pattern jobSummaryPattern = Pattern.compile("^[a-zA-Z0-9-_ ]+ #[0-9]+ \\((.*?)\\)$");
        Matcher matcher = jobSummaryPattern.matcher(raw);
        if(matcher.find())
            return matcher.group(1);
        return "init-build-summary";
    }
}

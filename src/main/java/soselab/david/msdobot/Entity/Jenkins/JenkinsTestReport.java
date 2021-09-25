package soselab.david.msdobot.Entity.Jenkins;

import com.google.gson.Gson;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.util.ArrayList;
import java.util.List;

public class JenkinsTestReport {

    private ArrayList<JenkinsTestSuite> testSuites;
    private int totalDuration;
    private int failedCount;
    private int passedCount;
    private int skipCount;
    private String reportDetailUrl;

    public JenkinsTestReport(){
        this.testSuites = new ArrayList<>();
    }

    public static JenkinsTestReport createJenkinsTestReport(int totalDuration, int failedCount, int passedCount, int skipCount){
        JenkinsTestReport newReport = new JenkinsTestReport();
        newReport.setTotalDuration(totalDuration);
        newReport.setFailedCount(failedCount);
        newReport.setPassedCount(passedCount);
        newReport.setSkipCount(skipCount);
        return newReport;
    }

    public void setTestSuites(ArrayList<JenkinsTestSuite> testSuites) {
        this.testSuites = testSuites;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public void setSkipCount(int skipCount) {
        this.skipCount = skipCount;
    }

    public void setReportDetailUrl(String reportDetailUrl) {
        this.reportDetailUrl = reportDetailUrl;
    }

    public ArrayList<JenkinsTestSuite> getTestSuites() {
        return testSuites;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public int getSkipCount() {
        return skipCount;
    }

    public String getReportDetailUrl() {
        return reportDetailUrl;
    }

    /**
     * json string format output
     * @return object json string
     */
    public String asJsonString(){
        Gson gson = new Gson();
        String json = gson.toJson(this);
        return json;
    }

    @Override
    public String toString() {
//        return "JenkinsTestReport{" +
//                "totalDuration=" + totalDuration +
//                ", failedCount=" + failedCount +
//                ", passedCount=" + passedCount +
//                ", skipCount=" + skipCount +
//                ", reportDetailUrl='" + reportDetailUrl +
//                "'}";
        return "[Jenkins Test Report] :" + "\n" +
                "Total Duration = " + totalDuration + "\n" +
                "Failed count = " + failedCount + "\n" +
                "Passed count = " + passedCount + "\n" +
                "Skipped count = " + skipCount + "\n" +
                "Check this url for report details: " + reportDetailUrl;
    }

    public List<MessageEmbed> getAsDiscordEmbedMessage(){
        EmbedBuilder builder = new EmbedBuilder();
        return null;
    }
}

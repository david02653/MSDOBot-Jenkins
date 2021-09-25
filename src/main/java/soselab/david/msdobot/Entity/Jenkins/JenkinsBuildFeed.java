package soselab.david.msdobot.Entity.Jenkins;

import com.google.gson.Gson;

public class JenkinsBuildFeed {
    String jobName;
    String buildNumber;
    String summary;
    String publishedTime;

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setPublishedTime(String publishedTime) {
        this.publishedTime = publishedTime;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPublishedTime() {
        return publishedTime;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getJobName() {
        return jobName;
    }

    public String getSummary() {
        return summary;
    }

    @Override
    public String toString() {
//        return "JenkinsBuildFeed{" +
//                "jobName='" + jobName + '\'' +
//                ", buildNumber='" + buildNumber + '\'' +
//                ", summary='" + summary + '\'' +
//                ", publishedTime='" + publishedTime + '\'' +
//                '}';
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

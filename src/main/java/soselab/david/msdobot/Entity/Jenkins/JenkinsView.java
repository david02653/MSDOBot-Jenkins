package soselab.david.msdobot.Entity.Jenkins;

import com.google.gson.Gson;
import com.iwebpp.crypto.TweetNaclFast;

import java.util.HashMap;

public class JenkinsView {

    private String name;
    private HashMap<String, JenkinsJob> jobList;

    public JenkinsView(){}
    public JenkinsView(String viewName, HashMap<String, JenkinsJob> jobList){
        this.name = viewName;
        this.jobList = jobList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setJobList(HashMap<String, JenkinsJob> jobList) {
        this.jobList = jobList;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, JenkinsJob> getJobList() {
        return jobList;
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

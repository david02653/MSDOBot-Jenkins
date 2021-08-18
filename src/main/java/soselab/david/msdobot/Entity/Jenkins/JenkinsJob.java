package soselab.david.msdobot.Entity.Jenkins;

import com.google.gson.Gson;

public class JenkinsJob {

    private String type; // job type, for example: freeStyleProject
    private String name;
    private String url;

    public JenkinsJob(){}
    public JenkinsJob(String type, String name, String url){
        this.type = type;
        this.name = name;
        this.url = url;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

package soselab.david.msdobot.Entity.Jenkins;

import com.google.gson.Gson;

import java.util.ArrayList;

public class JenkinsLog {
    String title;
    ArrayList<JenkinsLogEntry> entries;

    public void setEntries(ArrayList<JenkinsLogEntry> entries) {
        this.entries = entries;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public ArrayList<JenkinsLogEntry> getEntries() {
        return entries;
    }

    @Override
    public String toString(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

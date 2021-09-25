package soselab.david.msdobot.Entity.Jenkins;

import com.google.gson.Gson;

public class JenkinsLogEntry {
    String title;
    String id;
    String publishedTime;
    String content;
    String contentDetail;

    public JenkinsLogEntry(){
        this.title = "none";
    }

    public void setTitle(String title) {
        if(title.length() < 1) return;
        this.title = title;
    }

    public void setPublishedTime(String publishedTime) {
        this.publishedTime = publishedTime;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentDetail(String contentDetail) {
        this.contentDetail = contentDetail;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getPublishedTime() {
        return publishedTime;
    }

    public String getContent() {
        return content;
    }

    public String getContentDetail() {
        return contentDetail;
    }

    @Override
    public String toString() {
//        return "JenkinsLogEntry{" +
//                "title='" + title + '\'' +
//                ", publishedTime='" + publishedTime + '\'' +
//                ", content='" + content + '\'' +
//                ", contentDetail='" + contentDetail + '\'' +
//                '}';
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}

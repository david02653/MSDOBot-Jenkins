package soselab.david.msdobot.Entity.AdditionalQuestion;

import java.util.ArrayList;

public class Question {
    private String creator;
    private String resource;
    private String source = null;
    private String method = null;
    private ArrayList<String> pattern;
    private ArrayList<String> question;
    private String answer;

    public Question(){}

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setQuestion(ArrayList<String> question) {
        this.question = question;
    }

    public void setPattern(ArrayList<String> pattern) {
        this.pattern = pattern;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getCreator() {
        return creator;
    }

    public ArrayList<String> getQuestion() {
        return question;
    }

    public ArrayList<String> getPattern() {
        return pattern;
    }

    public String getResource() {
        return resource;
    }

    public String getSource() {
        return source;
    }

    public String getMethod() {
        return method;
    }

    public String getAnswer() {
        return answer;
    }

    @Override
    public String toString() {
        return "QuestionList{" +
                "creator='" + creator + '\'' +
                ", resource='" + resource + '\'' +
                ", source='" + source + '\'' +
                ", method='" + method + '\'' +
                ", pattern='" + pattern + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}

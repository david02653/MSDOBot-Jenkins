package soselab.david.msdobot.Entity.AdditionalQuestion;

import java.util.ArrayList;

public class ChannelAdditionalQuizList {
    private String channel;
    private ArrayList<Question> list;

    public ChannelAdditionalQuizList(){}

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setList(ArrayList<Question> list) {
        this.list = list;
    }

    public String getChannel() {
        return channel;
    }

    public ArrayList<Question> getList() {
        return list;
    }

    @Override
    public String toString() {
        return "ChannelAdditionalQuizList{" +
                "channel='" + channel + '\'' +
                ", list=" + list +
                '}';
    }
}

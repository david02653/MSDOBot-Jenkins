package soselab.david.msdobot.Entity.LongMessage;

public class DiscordMessage {

    private String id;
    private String message;

    public DiscordMessage(){}
    public DiscordMessage(String msg){
        this.message = msg;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "DiscordMessage{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}

package soselab.david.msdobot.Entity.Rasa;

public class Intent {
    public String recipient_id;
    public IntentSet text;

    public void setText(IntentSet text) {
        this.text = text;
    }

    public void setRecipient_id(String recipient_id) {
        this.recipient_id = recipient_id;
    }

    public IntentSet getText() {
        return text;
    }

    public String getRecipient_id() {
        return recipient_id;
    }

    @Override
    public String toString() {
        return "Intent{" +
                "recipient_id='" + recipient_id + '\'' +
                ", text=" + text +
                '}';
    }

}



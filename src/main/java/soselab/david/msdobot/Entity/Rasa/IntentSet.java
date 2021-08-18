package soselab.david.msdobot.Entity.Rasa;

public class IntentSet {
    public String intent;
    public String service;

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getIntent() {
        return intent;
    }

    public String getService() {
        return service;
    }

    @Override
    public String toString() {
        return "IntentSet{" +
                "intent='" + intent + '\'' +
                ", service='" + service + '\'' +
                '}';
    }
}

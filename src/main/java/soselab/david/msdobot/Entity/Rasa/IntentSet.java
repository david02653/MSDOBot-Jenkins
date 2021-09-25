package soselab.david.msdobot.Entity.Rasa;

public class IntentSet {
    public String intent;
    public String jobName;

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getIntent() {
        return intent;
    }

    public String getJobName() {
        return jobName;
    }

    @Override
    public String toString() {
        return "IntentSet{" +
                "intent='" + intent + '\'' +
                ", service='" + jobName + '\'' +
                '}';
    }
}

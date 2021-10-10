package soselab.david.msdobot.Entity.Rasa;

public class IntentSet {
    public String intent;
    public String jobName;
    public boolean lostName;

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setLostName(boolean lostName) {
        this.lostName = lostName;
    }

    public String getIntent() {
        return intent;
    }

    public String getJobName() {
        return jobName;
    }

    public boolean hasLostName() {
        return lostName;
    }

    @Override
    public String toString() {
        return "IntentSet{" +
                "intent='" + intent + '\'' +
                ", jobName='" + jobName + '\'' +
                ", lostName='" + lostName + '\'' +
                '}';
    }
}

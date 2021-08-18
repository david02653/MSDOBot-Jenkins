package soselab.david.msdobot.Entity.Jenkins;

/**
 * this java bean contains information extracted from jenkins JUnit test report
 * only several data field are required here, check out jenkins api for more details
 * name: test case name
 * duration: how long does this test case execution takes
 * errorDetails: why do this test case failed
 * errorStackTrace: error stack log of this test case
 * status: success of failed
 */
public class JenkinsTestCase{
    private String name;
    private int duration;
    private String errorDetails;
    private String errorStackTrace;
    private String status;

    public JenkinsTestCase(String name, int duration, String details, String trace, String status){
        setName(name);
        setDuration(duration);
        setErrorDetails(details);
        setErrorStackTrace(trace);
        setStatus(status);
    }

    /**
     * try to use factory mode to create new JenkinsTestCase object
     * @param name test case name
     * @param duration how many time does this test case takes
     * @param details failed reason
     * @param trace failed log
     * @param status success of failed
     * @return new jenkins test case object
     */
    public static JenkinsTestCase createNewTestCase(String name, int duration, String details, String trace, String status){
        return new JenkinsTestCase(name, duration, details, trace, status);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public void setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "{" + "\n" +
                "  test case name=" + name + "\n," +
                "  duration=" + duration + "\n," +
                "  errorDetails=" + errorDetails + "\n," +
                "  errorStackTrace=" + errorStackTrace + "\n," +
                "  status=" + status + "\n" +
                "}";
    }
}

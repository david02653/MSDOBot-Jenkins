package soselab.david.msdobot.Entity.Jenkins;

import java.util.ArrayList;

public class JenkinsTestSuite {

    private ArrayList<JenkinsTestCase> testCases;
    private int duration;
    private String suiteName;

    public JenkinsTestSuite(){}

    public static JenkinsTestSuite createNewTestSuite(String name, int duration){
        JenkinsTestSuite suite = new JenkinsTestSuite();
        suite.setSuiteName(name);
        suite.setDuration(duration);
        return suite;
    }

    public void setTestCases(ArrayList<JenkinsTestCase> testCases) {
        this.testCases = testCases;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public ArrayList<JenkinsTestCase> getTestCases() {
        return testCases;
    }

    public int getDuration() {
        return duration;
    }

    public String getSuiteName() {
        return suiteName;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{")
                .append("  Test Suite name: ").append(suiteName).append("\n,")
                .append("  Test Suite duration: ").append(duration);
        for(JenkinsTestCase testCase: testCases){
            builder.append("\n,  ");
            builder.append(testCase.toString());
        }
        builder.append("\n}");
//        return "{" +
//                "testCases=" + testCases +
//                ", duration=" + duration +
//                ", suiteName='" + suiteName + '\'' +
//                "}";
        return builder.toString();
    }
}

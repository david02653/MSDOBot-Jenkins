package soselab.david.msdobot.Entity.Eureka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.ArrayList;

public class EurekaResponse {
    @JsonProperty("versions__delta")
    private String versionsDelta;
    @JsonProperty("apps__hashcode")
    private String appsHashCode;
    @JsonProperty("application")
    @JacksonXmlElementWrapper(useWrapping = false)
    private ArrayList<Application> appList;

    public void setVersionsDelta(String versionsDelta) {
        this.versionsDelta = versionsDelta;
    }

    public void setAppsHashCode(String appsHashCode) {
        this.appsHashCode = appsHashCode;
    }

    public void setAppList(ArrayList<Application> appList) {
        this.appList = appList;
    }

    public String getVersionsDelta() {
        return versionsDelta;
    }

    public String getAppsHashCode() {
        return appsHashCode;
    }

    public ArrayList<Application> getAppList() {
        return appList;
    }

    @Override
    public String toString() {
        return "EurekaResponse{" +
                "versionsDelta='" + versionsDelta + '\'' +
                ", appsHashCode='" + appsHashCode + '\'' +
                ", appList=" + appList +
                '}';
    }
}
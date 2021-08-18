package soselab.david.msdobot.Entity.Eureka;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

public class Instance{
    private String instanceId;
    private String hostName;
    private String app;
    private String ipAddr;
    private String status;
    @JsonProperty("overriddenstatus")
    private String overriddenStatus;
    private Port port;
    private Port securePort;
    private int countryId;
    private DataCenterInfo dataCenterInfo;
    private LeaseInfo leaseInfo;
    @JsonProperty("metadata")
    private MetaData metaData;
    private String homePageUrl;
    private String statusPageUrl;
    private String healthCheckUrl;
    private String vipAddress;
    private String secureVipAddress;
    private boolean isCoordinatingDiscoveryServer;
    private String lastUpdatedTimestamp;
    private String lastDirtyTimestamp;
    private String actionType;

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setOverriddenStatus(String overriddenStatus) {
        this.overriddenStatus = overriddenStatus;
    }

    public void setPort(Port port) {
        this.port = port;
    }

    public void setSecurePort(Port securePort) {
        this.securePort = securePort;
    }

    public void setCountryId(int countryId) {
        this.countryId = countryId;
    }

    public void setDataCenterInfo(DataCenterInfo dataCenterInfo) {
        this.dataCenterInfo = dataCenterInfo;
    }

    public void setLeaseInfo(LeaseInfo leaseInfo) {
        this.leaseInfo = leaseInfo;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public void setHomePageUrl(String homePageUrl) {
        this.homePageUrl = homePageUrl;
    }

    public void setStatusPageUrl(String statusPageUrl) {
        this.statusPageUrl = statusPageUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public void setVipAddress(String vipAddress) {
        this.vipAddress = vipAddress;
    }

    public void setSecureVipAddress(String secureVipAddress) {
        this.secureVipAddress = secureVipAddress;
    }

    public void setIsCoordinatingDiscoveryServer(boolean coordinatingDiscoveryServer) {
        isCoordinatingDiscoveryServer = coordinatingDiscoveryServer;
    }

    public void setLastUpdatedTimestamp(String lastUpdatedTimestamp) {
        this.lastUpdatedTimestamp = lastUpdatedTimestamp;
    }

    public void setLastDirtyTimestamp(String lastDirtyTimestamp) {
        this.lastDirtyTimestamp = lastDirtyTimestamp;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getHostName() {
        return hostName;
    }

    public String getApp() {
        return app;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public String getStatus() {
        return status;
    }

    public String getOverriddenStatus() {
        return overriddenStatus;
    }

    public Port getPort() {
        return port;
    }

    public Port getSecurePort() {
        return securePort;
    }

    public int getCountryId() {
        return countryId;
    }

    public DataCenterInfo getDataCenterInfo() {
        return dataCenterInfo;
    }

    public LeaseInfo getLeaseInfo() {
        return leaseInfo;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public String getHomePageUrl() {
        return homePageUrl;
    }

    public String getStatusPageUrl() {
        return statusPageUrl;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public String getVipAddress() {
        return vipAddress;
    }

    public String getSecureVipAddress() {
        return secureVipAddress;
    }

    public boolean getIsCoordinatingDiscoveryServer() {
        return isCoordinatingDiscoveryServer;
    }

    public String getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    public String getLastDirtyTimestamp() {
        return lastDirtyTimestamp;
    }

    public String getActionType() {
        return actionType;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "instanceId='" + instanceId + '\'' +
                ", hostName='" + hostName + '\'' +
                ", app='" + app + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", status='" + status + '\'' +
                ", overriddenStatus='" + overriddenStatus + '\'' +
                ", port='" + port + '\'' +
                ", securePort='" + securePort + '\'' +
                ", countryId=" + countryId +
                ", dataCenterInfo=" + dataCenterInfo +
                ", leaseInfo=" + leaseInfo +
                ", metaData=" + metaData +
                ", homePageUrl='" + homePageUrl + '\'' +
                ", statusPageUrl='" + statusPageUrl + '\'' +
                ", healthCheckUrl='" + healthCheckUrl + '\'' +
                ", vipAddress='" + vipAddress + '\'' +
                ", secureVipAddress='" + secureVipAddress + '\'' +
                ", isCoordinatingDiscoveryServer=" + isCoordinatingDiscoveryServer +
                ", lastUpdatedTimestamp='" + lastUpdatedTimestamp + '\'' +
                ", lastDirtyTimestamp='" + lastDirtyTimestamp + '\'' +
                ", actionType='" + actionType + '\'' +
                '}';
    }
}

class Port{
    @JacksonXmlProperty(isAttribute = true)
    private String enabled;
    @JacksonXmlText(value = true)
    private Integer port;

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getEnabled() {
        return enabled;
    }

    public Integer getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "Port{" +
                "enabled='" + enabled + '\'' +
                ", port=" + port +
                '}';
    }
}

class DataCenterInfo{
    @JacksonXmlProperty(isAttribute = true)
    private String source;
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "DataCenterInfo{" +
                "source='" + source + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

class LeaseInfo{
    private int renewalIntervalInSecs;
    private int durationInSecs;
    private String registrationTimestamp;
    private String lastRenewalTimestamp;
    private String evictionTimestamp;
    private String serviceUpTimestamp;

    public void setRenewalIntervalInSecs(int renewalIntervalInSecs) {
        this.renewalIntervalInSecs = renewalIntervalInSecs;
    }

    public void setDurationInSecs(int durationInSecs) {
        this.durationInSecs = durationInSecs;
    }

    public void setRegistrationTimestamp(String registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public void setLastRenewalTimestamp(String lastRenewalTimestamp) {
        this.lastRenewalTimestamp = lastRenewalTimestamp;
    }

    public void setEvictionTimestamp(String evictionTimestamp) {
        this.evictionTimestamp = evictionTimestamp;
    }

    public void setServiceUpTimestamp(String serviceUpTimestamp) {
        this.serviceUpTimestamp = serviceUpTimestamp;
    }

    public int getRenewalIntervalInSecs() {
        return renewalIntervalInSecs;
    }

    public int getDurationInSecs() {
        return durationInSecs;
    }

    public String getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    public String getLastRenewalTimestamp() {
        return lastRenewalTimestamp;
    }

    public String getEvictionTimestamp() {
        return evictionTimestamp;
    }

    public String getServiceUpTimestamp() {
        return serviceUpTimestamp;
    }

    @Override
    public String toString() {
        return "LeaseInfo{" +
                "renewalIntervalInSecs=" + renewalIntervalInSecs +
                ", durationInSecs=" + durationInSecs +
                ", registrationTimestamp='" + registrationTimestamp + '\'' +
                ", lastRenewalTimestamp='" + lastRenewalTimestamp + '\'' +
                ", evictionTimestamp='" + evictionTimestamp + '\'' +
                ", serviceUpTimestamp='" + serviceUpTimestamp + '\'' +
                '}';
    }
}

class MetaData{
    @JsonProperty("management.port")
    private int managementPort;

    public void setManagementPort(int managementPort) {
        this.managementPort = managementPort;
    }

    public int getManagementPort() {
        return managementPort;
    }

    @Override
    public String toString() {
        return "MetaData{" +
                "managementPort=" + managementPort +
                '}';
    }
}

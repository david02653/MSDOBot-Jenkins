package soselab.david.msdobot.Entity.Eureka;

public class Application{
    private String name;
    private Instance instance;

    public void setName(String name) {
        this.name = name;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public String getName() {
        return name;
    }

    public Instance getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", instanceList=" + instance +
                '}';
    }
}

/*
<applications>
  <versions__delta>1</versions__delta>
  <apps__hashcode>UP_6_</apps__hashcode>
  <application>
    <name>CINEMACATALOG</name>
    <instance>
      <instanceId>3cf16c3aa1dc:cinemacatalog:9014</instanceId>
      <hostName>140.121.197.130</hostName>
      <app>CINEMACATALOG</app>
      <ipAddr>140.121.197.130</ipAddr>
      <status>UP</status>
      <overriddenstatus>UNKNOWN</overriddenstatus>
      <port enabled="true">9014</port>
      <securePort enabled="false">443</securePort>
      <countryId>1</countryId>
      <dataCenterInfo class="com.netflix.appinfo.InstanceInfo$DefaultDataCenterInfo">
        <name>MyOwn</name>
      </dataCenterInfo>
      <leaseInfo>
        <renewalIntervalInSecs>1</renewalIntervalInSecs>
        <durationInSecs>2</durationInSecs>
        <registrationTimestamp>1606073608201</registrationTimestamp>
        <lastRenewalTimestamp>1606075092179</lastRenewalTimestamp>
        <evictionTimestamp>0</evictionTimestamp>
        <serviceUpTimestamp>1606073608201</serviceUpTimestamp>
      </leaseInfo>
      <metadata>
        <management.port>9014</management.port>
      </metadata>
      <homePageUrl>http://140.121.197.130:9014/</homePageUrl>
      <statusPageUrl>http://140.121.197.130:9014/info</statusPageUrl>
      <healthCheckUrl>http://140.121.197.130:9014/health</healthCheckUrl>
      <vipAddress>cinemacatalog</vipAddress>
      <secureVipAddress>cinemacatalog</secureVipAddress>
      <isCoordinatingDiscoveryServer>false</isCoordinatingDiscoveryServer>
      <lastUpdatedTimestamp>1606073608201</lastUpdatedTimestamp>
      <lastDirtyTimestamp>1606073608132</lastDirtyTimestamp>
      <actionType>ADDED</actionType>
    </instance>
  </application>
  <application></application>
  <application></application>
  ...
</applications>
 */
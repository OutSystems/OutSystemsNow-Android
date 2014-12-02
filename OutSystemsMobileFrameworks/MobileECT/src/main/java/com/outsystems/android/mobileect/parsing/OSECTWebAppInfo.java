package com.outsystems.android.mobileect.parsing;

import java.util.List;

/**
 * Created by lrs on 24-11-2014.
 */
public class OSECTWebAppInfo {



    private boolean ECTAvailable;
    private String EnvironmentUID;
    private String EspaceUID;
    private String ApplicationUID;
    private String ScreenUID;
    private String ScreenName;
    private String UserId;
    private String UserAgentHeader;
    private List<OSECTApiVersion> SupportedApiVersions;

    public OSECTWebAppInfo(){

    }
    public boolean isECTAvailable() {
        return ECTAvailable;
    }

    public void setECTAvailable(boolean ECTAvailable) {
        this.ECTAvailable = ECTAvailable;
    }

    public String getEnvironmentUID() {
        return EnvironmentUID;
    }

    public void setEnvironmentUID(String environmentUID) {
        EnvironmentUID = environmentUID;
    }

    public String getEspaceUID() {
        return EspaceUID;
    }

    public void setEspaceUID(String espaceUID) {
        EspaceUID = espaceUID;
    }

    public String getApplicationUID() {
        return ApplicationUID;
    }

    public void setApplicationUID(String applicationUID) {
        ApplicationUID = applicationUID;
    }

    public String getScreenUID() {
        return ScreenUID;
    }

    public void setScreenUID(String screenUID) {
        ScreenUID = screenUID;
    }

    public String getScreenName() {
        return ScreenName;
    }

    public void setScreenName(String screenName) {
        ScreenName = screenName;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getUserAgentHeader() {
        return UserAgentHeader;
    }

    public void setUserAgentHeader(String userAgentHeader) {
        UserAgentHeader = userAgentHeader;
    }


    public List<OSECTApiVersion> getSupportedApiVersions() {
        return SupportedApiVersions;
    }

    public void setSupportedApiVersions(List<OSECTApiVersion> supportedApiVersions) {
        SupportedApiVersions = supportedApiVersions;
    }
}

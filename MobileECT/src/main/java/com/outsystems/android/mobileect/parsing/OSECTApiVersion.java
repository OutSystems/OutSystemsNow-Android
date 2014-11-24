package com.outsystems.android.mobileect.parsing;

/**
 * Created by lrs on 24-11-2014.
 */
public class OSECTApiVersion {

    private String ApiVersion;
    private String URL;
    private boolean IsCurrentVersion;

    public OSECTApiVersion(){

    }

    public String getApiVersion() {
        return ApiVersion;
    }

    public void setApiVersion(String apiVersion) {
        ApiVersion = apiVersion;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public boolean isCurrentVersion() {
        return IsCurrentVersion;
    }

    public void setCurrentVersion(boolean isCurrentVersion) {
        IsCurrentVersion = isCurrentVersion;
    }
}

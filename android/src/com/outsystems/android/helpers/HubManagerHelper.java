package com.outsystems.android.helpers;

public class HubManagerHelper {

    private static HubManagerHelper _instance;

    private String applicationHosted = null;

 // The application Server - default value .aspx
    private boolean isJSFApplicationServer = false;
    
    private HubManagerHelper() {

    }

    public static HubManagerHelper getInstance() {
        if (_instance == null) {
            _instance = new HubManagerHelper();
        }
        return _instance;
    }

    public String getApplicationHosted() {
        return applicationHosted;
    }

    public void setApplicationHosted(String applicationHosted) {
        this.applicationHosted = applicationHosted;
    }

    public boolean isJSFApplicationServer() {
        return isJSFApplicationServer;
    }

    public void setJSFApplicationServer(boolean isJSFApplicationServer) {
        this.isJSFApplicationServer = isJSFApplicationServer;
    }
}

/*
 * Outsystems Project
 * 
 * Copyright (C) 2014 Outsystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android.helpers;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class HubManagerHelper {

    private static HubManagerHelper _instance;

    private String applicationHosted = null;

    // The application Server - default value .aspx
    private boolean isJSFApplicationServer = false;

    private String deviceId = "";

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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}

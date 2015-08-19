package com.outsystems.android.model;

/**
 * Created by lrs on 8/19/2015.
 */
public class AppSettings {
    private boolean SkipNativeLogin;
    private boolean SkipApplicationList;
    private boolean HideNavigationBar;
    private String DefaultHostname;
    private String DefaultApplicationURL;
    private String BackgroundColor;
    private String ForegroundColor;
    private String TintColor;

    public AppSettings() {
    }

    public AppSettings(boolean skipNativeLogin, boolean skipApplicationList, boolean hideNavigationBar, String defaultHostname, String defaultApplicationURL, String backgroundColor, String foregroundColor, String tintColor) {
        SkipNativeLogin = skipNativeLogin;
        SkipApplicationList = skipApplicationList;
        HideNavigationBar = hideNavigationBar;
        DefaultHostname = defaultHostname;
        DefaultApplicationURL = defaultApplicationURL;
        BackgroundColor = backgroundColor;
        ForegroundColor = foregroundColor;
        TintColor = tintColor;
    }

    public boolean skipNativeLogin() {
        return SkipNativeLogin;
    }

    public void setSkipNativeLogin(boolean skipNativeLogin) {
        SkipNativeLogin = skipNativeLogin;
    }

    public boolean skipApplicationList() {
        return SkipApplicationList;
    }

    public void setSkipApplicationList(boolean skipApplicationList) {
        SkipApplicationList = skipApplicationList;
    }

    public boolean hideNavigationBar() {
        return HideNavigationBar;
    }

    public void setHideNavigationBar(boolean hideNavigationBar) {
        HideNavigationBar = hideNavigationBar;
    }

    public String getDefaultHostname() {
        return DefaultHostname;
    }

    public void setDefaultHostname(String defaultHostname) {
        DefaultHostname = defaultHostname;
    }

    public String getDefaultApplicationURL() {
        return DefaultApplicationURL;
    }

    public void setDefaultApplicationURL(String defaultApplicationURL) {
        DefaultApplicationURL = defaultApplicationURL;
    }

    public String getBackgroundColor() {
        return BackgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        BackgroundColor = backgroundColor;
    }

    public String getForegroundColor() {
        return ForegroundColor;
    }

    public void setForegroundColor(String foregroundColor) {
        ForegroundColor = foregroundColor;
    }

    public String getTintColor() {
        return TintColor;
    }

    public void setTintColor(String tintColor) {
        TintColor = tintColor;
    }
}

package com.outsystems.android.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;

import com.outsystems.android.ApplicationOutsystems;
import com.outsystems.android.ApplicationsActivity;
import com.outsystems.android.R;
import com.outsystems.android.WebApplicationActivity;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.model.Application;
import com.outsystems.android.model.HubApplicationModel;
import com.outsystems.android.model.Login;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lrs on 24-12-2014.
 */
public class OfflineSupport {


    private static OfflineSupport _instance;

    private boolean validCredentials;
    private Context applicationContext;

    private HubApplicationModel previousSession;

    private boolean newSession;

    private boolean offlineSession;


    public OfflineSupport(Context context) {
        this.validCredentials = false;
        this.newSession = false;
        this.applicationContext = context;
        this.offlineSession = true;
    }

    public static OfflineSupport getInstance(Context context) {
        if (_instance == null) {
            _instance = new OfflineSupport(context);
        }

        _instance.applicationContext = context;

        return _instance;
    }


    public boolean hasValidCredentials() {

        DatabaseHandler database = new DatabaseHandler(applicationContext);
        HubApplicationModel lastHub = database.getLastLoginHubApplicationModel();
        this.validCredentials = false;

        if (lastHub != null) {
            List<Application> applications = database.getLoginApplications(lastHub.getHost(), lastHub.getUserName());
            this.validCredentials = applications != null && applications.size() > 0;
        }

        database.close();

        return validCredentials;
    }

    public void redirectToApplicationList(Activity currentActivity) {

        DatabaseHandler database = new DatabaseHandler(applicationContext);
        HubApplicationModel lastHub = database.getLastLoginHubApplicationModel();
        List<Application> applications = database.getLoginApplications(lastHub.getHost(), lastHub.getUserName());
        database.close();

        if (applications != null && applications.size() == 1) {
            openWebApplicationActivity(currentActivity,applications.get(0));
        } else {
            openApplicationsActivity(currentActivity,applications);
        }
    }


    /**
     * Open applications activity.
     *
     */
    private void openApplicationsActivity(Activity currentActivity,
                                          List<Application> applications) {
        Intent intent = new Intent(this.applicationContext, ApplicationsActivity.class);
        ArrayList arrayList = ( ArrayList )applications;
        intent.putParcelableArrayListExtra(ApplicationsActivity.KEY_CONTENT_APPLICATIONS,
                (ArrayList<? extends Parcelable>) arrayList);
        intent.putExtra(ApplicationsActivity.KEY_TITLE_ACTION_BAR,
                currentActivity.getResources().getString(R.string.label_logout));
        currentActivity.startActivity(intent);
    }

    /**
     * Open web application activity.
     */
    private void openWebApplicationActivity(Activity currentActivity,
                                            Application application) {
        Intent intent = new Intent(this.applicationContext, WebApplicationActivity.class);
        if (application != null) {
            intent.putExtra(WebApplicationActivity.KEY_APPLICATION, application);
            intent.putExtra(WebApplicationActivity.KEY_SINGLE_APPLICATION, true);
        }
        currentActivity.startActivity(intent);
    }

    /**
     * Clear browser cache if needed
     */
    public void clearCacheIfNeeded(WebView webView){
        EventLogger.logMessage(getClass(), "clearCacheIfNeeded: "+this.newSession);
        if(this.newSession){
            /*
             * 23/02/2015 - LRS - Feature disabled: iOS doesn't have support for a similar feature.
             *                    We need to ensure the same behavior in all platforms.
            webView.clearCache(true);
            */
            this.previousSession = null;
        }

        this.newSession = false;
    }


    /**
     *  Prepare data
     */
    public void prepareForLogin() {
        this.newSession = false;

        DatabaseHandler database = new DatabaseHandler(applicationContext);
        this.previousSession = database.getLastLoginHubApplicationModel();
        database.close();
    }

    /**
     * Check if the current session has a new user to clear the browser cache
     */
    public void checkCurrentSession(String hub, String username){
        this.newSession = true;
        this.offlineSession = false;

        if(previousSession != null){
            boolean sameEnvironment = hub.equalsIgnoreCase(previousSession.getHost());
            boolean sameUser = username.equalsIgnoreCase(previousSession.getUserName());

            this.newSession = !(sameEnvironment && sameUser);
            EventLogger.logMessage(getClass(), "checkCurrentSession: "+this.newSession);

        }

    }

    public void retryWebViewAction(Activity currentActivity, ApplicationOutsystems app, WebView webView, String failingUrl){

        if (app.isNetworkAvailable()) {
            webView.setNetworkAvailable(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        } else {
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        }


        EventLogger.logMessage(getClass(), "retryWebViewAction: offlineSession:"+this.offlineSession);
        if(this.offlineSession){
            this.loginAndReloadWebView(currentActivity, webView,failingUrl);
        }
        else {
            if(webView.getUrl() != null)
                webView.reload();
            else
                webView.loadUrl(failingUrl);
        }
    }

    private void loginAndReloadWebView(final Activity currentActivity, final WebView webView, final String failingUrl) {


        final DisplayMetrics displaymetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);


        DatabaseHandler database = new DatabaseHandler(applicationContext);
        HubApplicationModel lastHub = database.getLastLoginHubApplicationModel();
        database.close();

        if(lastHub == null)
            return;

        final String userName = lastHub.getUserName();
        final String password = lastHub.getPassword();

        WebServicesClient.getInstance().loginPlattform(applicationContext, userName, password,
                HubManagerHelper.getInstance().getDeviceId(), (int) (displaymetrics.widthPixels / displaymetrics.density), (int) (displaymetrics.heightPixels / displaymetrics.density), new WSRequestHandler() {
                    @Override
                    public void requestFinish(Object result, boolean error, int statusCode) {
                        boolean loginSucceeded = false;
                        if (!error) {
                            Login login = (Login) result;

                            if (login != null && login.isSuccess()) {

                                DatabaseHandler database = new DatabaseHandler(applicationContext);
                                database.updateHubApplicationCredentials(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName, password);
                                database.addLoginApplications(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName, login.getApplications());

                                database.close();

                                offlineSession = false;
                                loginSucceeded = true;

                                // Synchronize WebView cookies with Login Request cookies
                                CookieSyncManager.createInstance(applicationContext);
                                // android.webkit.CookieManager.getInstance().removeAllCookie();

                                List<String> cookies = WebServicesClient.getInstance().getLoginCookies();
                                if (cookies != null && !cookies.isEmpty()){
                                    for(String cookieString : cookies){
                                        android.webkit.CookieManager.getInstance().setCookie(HubManagerHelper.getInstance().getApplicationHosted(), cookieString);
                                        CookieSyncManager.getInstance().sync();
                                    }
                                }

                            }
                        }
                        else {


                            if(statusCode == WebServicesClient.INVALID_SSL) {
                                Resources res = currentActivity.getResources();
                                String message = String.format(res.getString(R.string.invalid_ssl_message), HubManagerHelper.getInstance().getApplicationHosted());

                                new AlertDialog.Builder(currentActivity.getWindow().getContext())
                                        .setTitle(R.string.invalid_ssl_title)
                                        .setMessage(message)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                WebServicesClient.getInstance().addTrustedHostname(HubManagerHelper.getInstance().getApplicationHosted());
                                                webView.loadUrl(failingUrl);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                webView.loadUrl(failingUrl);
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();


                                return;
                            }

                        }

                        EventLogger.logMessage(getClass(), "loginAndReloadWebView: loginSucceeded:"+loginSucceeded);

                        if(webView.getUrl() != null)
                            webView.reload();
                        else
                            webView.loadUrl(failingUrl);
                    }
                });

    }


    public void loginIfOfflineSession(Activity currentActivity) {

        if(!offlineSession)
            return;

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        currentActivity.getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);

        DatabaseHandler database = new DatabaseHandler(applicationContext);
        HubApplicationModel lastHub = database.getLastLoginHubApplicationModel();
        database.close();

        if(lastHub == null)
            return;

        final String userName = lastHub.getUserName();
        final String password = lastHub.getPassword();


        WebServicesClient.getInstance().loginPlattform(applicationContext, userName, password,
                HubManagerHelper.getInstance().getDeviceId(), (int) (displaymetrics.widthPixels / displaymetrics.density), (int) (displaymetrics.heightPixels / displaymetrics.density), new WSRequestHandler() {
                    @Override
                    public void requestFinish(Object result, boolean error, int statusCode) {
                        boolean loginSucceeded = false;
                        if (!error) {
                            Login login = (Login) result;

                            if (login != null && login.isSuccess()) {

                                DatabaseHandler database = new DatabaseHandler(applicationContext);
                                database.updateHubApplicationCredentials(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName, password);
                                database.addLoginApplications(HubManagerHelper.getInstance()
                                        .getApplicationHosted(), userName, login.getApplications());

                                database.close();

                                offlineSession = false;
                                loginSucceeded = true;

                                // Synchronize WebView cookies with Login Request cookies
                                CookieSyncManager.createInstance(applicationContext);
                                // android.webkit.CookieManager.getInstance().removeAllCookie();

                                List<String> cookies = WebServicesClient.getInstance().getLoginCookies();
                                if (cookies != null && !cookies.isEmpty()){
                                    for(String cookieString : cookies){
                                        android.webkit.CookieManager.getInstance().setCookie(HubManagerHelper.getInstance().getApplicationHosted(), cookieString);
                                        CookieSyncManager.getInstance().sync();
                                    }
                                }

                            }
                        }

                        EventLogger.logMessage(getClass(), "retryLogin: loginSucceeded:"+loginSucceeded);
                    }
                });

    }

}

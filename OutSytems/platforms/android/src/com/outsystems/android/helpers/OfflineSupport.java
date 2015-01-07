package com.outsystems.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.outsystems.android.ApplicationOutsystems;
import com.outsystems.android.ApplicationsActivity;
import com.outsystems.android.LoginActivity;
import com.outsystems.android.R;
import com.outsystems.android.WebApplicationActivity;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.model.Application;
import com.outsystems.android.model.HubApplicationModel;
import com.outsystems.android.model.Login;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lrs on 24-12-2014.
 */
public class OfflineSupport {

    private static final String COOKIE_USERS = "Users";

    private static OfflineSupport _instance;

    private boolean validCredentials;
    private Context applicationContext;

    public OfflineSupport(Context context) {
        this.validCredentials = false;
        this.applicationContext = context;
    }

    public static OfflineSupport getInstance(Context context) {
        if (_instance == null) {
            _instance = new OfflineSupport(context);
        }
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

        return validCredentials;
    }

    public void redirectToApplicationList(Activity currentActivity) {

        DatabaseHandler database = new DatabaseHandler(applicationContext);
        HubApplicationModel lastHub = database.getLastLoginHubApplicationModel();
        List<Application> applications = database.getLoginApplications(lastHub.getHost(), lastHub.getUserName());

        if (applications != null && applications.size() == 1) {
            openWebApplicationActivity(currentActivity,applications.get(0));
        } else {
            openApplicationsActivity(currentActivity,applications);
        }
    }

    public boolean isUserLoggedIn(String cookies){
        return cookies != null && cookies.contains(COOKIE_USERS);
    }

    public void redirectToLoginScreen(Activity currentActivity){

        DatabaseHandler database = new DatabaseHandler(this.applicationContext);

        HubApplicationModel lastHub = database.getLastLoginHubApplicationModel();

        if (database.getHubApplication(lastHub.getHost()) == null) {
            database.addHostHubApplication(lastHub.getHost(), lastHub.getName(), lastHub.isJSF());
        }

        HubManagerHelper.getInstance().setApplicationHosted(lastHub.getHost());
        HubManagerHelper.getInstance().setJSFApplicationServer(lastHub.isJSF());

        ApplicationOutsystems app = (ApplicationOutsystems) currentActivity.getApplication();
        app.setDemoApplications(false);

        // Start Login Activity
        Intent intent = new Intent(this.applicationContext, LoginActivity.class);
        intent.putExtra(LoginActivity.KEY_AUTOMATICLY_LOGIN, false);
        if (HubManagerHelper.getInstance() != null) {
            intent.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, lastHub.getName());
        }
        currentActivity.startActivity(intent);

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
}

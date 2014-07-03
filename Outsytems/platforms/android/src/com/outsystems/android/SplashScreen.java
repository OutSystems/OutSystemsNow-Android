/*
 * Outsystems Project
 *
 * Copyright (C) 2014 Outsystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android;

import com.crashlytics.android.Crashlytics;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.HubApplicationModel;

/**
 * Class description.
 *
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 *
 */
public class SplashScreen extends Activity {

    public static int TIME_SPLASH_SCREEN = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_splashscreen);

        // Add delay to show splashscreen
        delaySplashScreen();
    }

    private void delaySplashScreen() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goNextActivity();
            }
        }, TIME_SPLASH_SCREEN);
    }

    protected void goNextActivity() {
        DatabaseHandler database = new DatabaseHandler(getApplicationContext());
        List<HubApplicationModel> hubApplications = database.getAllHubApllications();
        openHubActivity();
        if (hubApplications != null && hubApplications.size() > 0) {
            HubApplicationModel hubApplication = hubApplications.get(0);
            HubManagerHelper.getInstance().setApplicationHosted(hubApplication.getHost());
            HubManagerHelper.getInstance().setJSFApplicationServer(hubApplication.isJSF());

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            if (hubApplication != null) {
                intent.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, hubApplication.getName());
                intent.putExtra(LoginActivity.KEY_AUTOMATICLY_LOGIN, true);
            }
            startActivity(intent);
        }
        finish();
    }

    private void openHubActivity() {
        Intent intent = new Intent(this, HubAppActivity.class);
        startActivity(intent);
    }
}

/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;

import com.arellomobile.android.push.PushManager;
import com.arellomobile.android.push.utils.RegisterBroadcastReceiver;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
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

    /** The time splash screen. */
    public static int TIME_SPLASH_SCREEN = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        // Add delay to show splashscreen
        delaySplashScreen();

        // Register receivers for push notifications
        registerReceivers();



        // Register for push!


    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister receivers on pause
        unregisterReceivers();
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
            if (hubApplication != null) {
                HubManagerHelper.getInstance().setApplicationHosted(hubApplication.getHost());
                HubManagerHelper.getInstance().setJSFApplicationServer(hubApplication.isJSF());
            }
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

    /** Methods to Push Notifications */
    // Registration receiver
    BroadcastReceiver mBroadcastReceiver = new RegisterBroadcastReceiver() {
        @Override
        public void onRegisterActionReceive(Context context, Intent intent) {
            checkMessage(intent);
        }
    };

    // Registration of the receivers
    public void registerReceivers() {
    }

    public void unregisterReceivers() {
        // Unregister receivers on pause
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            EventLogger.logError(getClass(), e);
        }
    }

    private void checkMessage(Intent intent) {
        if (null != intent) {
            if (intent.hasExtra(PushManager.REGISTER_EVENT)) {
                String deviceId = intent.getExtras().getString(PushManager.REGISTER_EVENT);
                HubManagerHelper.getInstance().setDeviceId(deviceId);
            }
            resetIntentValues();
        }
    }

    /**
     * Will check main Activity intent and if it contains any PushWoosh data, will clear it
     */
    private void resetIntentValues() {
        Intent mainAppIntent = getIntent();


        setIntent(mainAppIntent);
    }
}

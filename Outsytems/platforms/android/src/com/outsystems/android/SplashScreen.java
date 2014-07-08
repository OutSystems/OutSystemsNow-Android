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
import android.util.Log;

import com.arellomobile.android.push.PushManager;
import com.arellomobile.android.push.utils.RegisterBroadcastReceiver;
import com.crashlytics.android.Crashlytics;
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

    /** The time splash screen. */
    public static int TIME_SPLASH_SCREEN = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.activity_splashscreen);

        // Add delay to show splashscreen
        delaySplashScreen();

        // Register receivers for push notifications
        registerReceivers();

        // Create and start push manager
        PushManager pushManager = PushManager.getInstance(this);

        // Start push manager, this will count app open for Pushwoosh stats as well
        try {
            pushManager.onStartup(this);
        } catch (Exception e) {
            // push notifications are not available or AndroidManifest.xml is not configured properly
            Log.e("outsystems", e.toString());
        }

        // Register for push!
        pushManager.registerForPushNotifications();

        checkMessage(getIntent());

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
        registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "."
                + PushManager.REGISTER_BROAD_CAST_ACTION));
    }

    public void unregisterReceivers() {
        // Unregister receivers on pause
        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            Log.e("outsystems", e.toString());
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

        if (mainAppIntent.hasExtra(PushManager.PUSH_RECEIVE_EVENT)) {
            mainAppIntent.removeExtra(PushManager.PUSH_RECEIVE_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.REGISTER_EVENT)) {
            mainAppIntent.removeExtra(PushManager.REGISTER_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_EVENT)) {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.REGISTER_ERROR_EVENT)) {
            mainAppIntent.removeExtra(PushManager.REGISTER_ERROR_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT)) {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_ERROR_EVENT);
        }

        setIntent(mainAppIntent);
    }
}

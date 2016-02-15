package com.outsystems.android.core;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import com.outsystems.android.BaseActivity;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewEngine;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.PluginManager;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.util.ArrayList;
import java.util.Locale;

public class CordovaWebViewActivity extends BaseActivity {
    public static String TAG = "CordovaWebViewActivity";

    // The System Webview for our app
    public SystemWebView cordovaWebView;

    // The Cordova Webview for our app
    protected CordovaWebView appView;

    private static int ACTIVITY_STARTING = 0;
    private static int ACTIVITY_RUNNING = 1;
    private static int ACTIVITY_EXITING = 2;

    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    // Flag to keep immersive mode if set to fullscreen
    protected boolean immersiveMode;

    // Read from config.xml:
    protected CordovaPreferences preferences;
    protected String launchUrl;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaWebViewInterface cordovaInterface;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        EventLogger.logInfoMessage(TAG, "Apache Cordova native platform version " + CordovaWebView.CORDOVA_VERSION + " is starting");
        EventLogger.logMessage(TAG, "CordovaActivity.onCreate()");

        // need to activate preferences before super.onCreate to avoid "requestFeature() must be called before adding content" exception
        loadConfig();
       /*
        if(!preferences.getBoolean("ShowTitle", false))
        {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        }*/

        if(preferences.getBoolean("SetFullscreen", false))
        {
            EventLogger.logMessage(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
            preferences.set("Fullscreen", true);
        }
        if(preferences.getBoolean("Fullscreen", false))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                immersiveMode = true;
            }
            else
            {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }

        super.onCreate(savedInstanceState);

        // Hide Action Bar
        if(!preferences.getBoolean("ShowTitle", false)) {
            getActionBar().hide();
        }

        cordovaInterface = makeCordovaInterface();
        if(savedInstanceState != null)
        {
            cordovaInterface.restoreInstanceState(savedInstanceState);
        }
    }

    protected void init() {
        appView = makeWebView();

        if (!appView.isInitialized()) {
            appView.init(cordovaInterface, pluginEntries, preferences);
        }
        cordovaInterface.onCordovaInit(appView.getPluginManager());

        // Wire the hardware volume controls to control media if desired.
        String volumePref = preferences.getString("DefaultVolumeStream", "");
        if ("media".equals(volumePref.toLowerCase(Locale.ENGLISH))) {
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
        }

        appView.getView().requestFocusFromTouch();
    }

    @SuppressWarnings("deprecation")
    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(this);
        preferences = parser.getPreferences();
        preferences.setPreferencesBundle(getIntent().getExtras());
        launchUrl = parser.getLaunchUrl();
        pluginEntries = parser.getPluginEntries();
    }



    /**
     * Construct the default web view object.
     *
     * Override this to customize the webview that is used.
     */
    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    protected CordovaWebViewEngine makeWebViewEngine() {
        return new SystemWebViewEngine(cordovaWebView,preferences);
    }

    protected CordovaWebViewInterface makeCordovaInterface() {
        return new CordovaWebViewInterface(this) {
            @Override
            public Object onMessage(String id, Object data) {
                // Plumb this to CordovaActivity.onMessage for backwards compatibility
                return CordovaWebViewActivity.this.onMessage(id, data);
            }
        };
    }

    /**
     * Load the url into the webview.
     */
    public void loadUrl(String url) {
        if (appView == null) {
            init();
        }

        // If keepRunning
        this.keepRunning = preferences.getBoolean("KeepRunning", true);

        appView.loadUrlIntoView(url, true);
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     */
    @Override
    public void onPause() {
        super.onPause();
        EventLogger.logMessage(TAG, "Paused the activity.");

        if (this.appView != null) {
            // CB-9382 If there is an activity that started for result and main activity is waiting for callback
            // result, we shoudn't stop WebView Javascript timers, as activity for result might be using them
            boolean keepRunning = this.keepRunning || this.cordovaInterface.getActivityResultCallback() != null;
            this.appView.handlePause(keepRunning);
        }
    }

    /**
     * Called when the activity receives a new intent
     **/
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Forward to plugins
        if (this.appView != null)
            this.appView.onNewIntent(intent);
    }

    /**
     * Called when the activity will start interacting with the user.
     */
    @Override
    public void onResume() {
        super.onResume();
        EventLogger.logMessage(TAG, "Resumed the activity.");

        if (this.appView == null) {
            return;
        }
        // Force window to have focus, so application always
        // receive user input. Workaround for some devices (Samsung Galaxy Note 3 at least)
        this.getWindow().getDecorView().requestFocus();

        this.appView.handleResume(this.keepRunning);
    }

    /**
     * Called when the activity is no longer visible to the user.
     */
    @Override
    public void onStop() {
        super.onStop();
        EventLogger.logMessage(TAG, "Stopped the activity.");

        if (this.appView == null) {
            return;
        }
        this.appView.handleStop();
    }

    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    public void onStart() {
        super.onStart();
        EventLogger.logMessage(TAG, "Started the activity.");

        if (this.appView == null) {
            return;
        }
        this.appView.handleStart();
    }

    /**
     * The final call you receive before your activity is destroyed.
     */
    @Override
    public void onDestroy() {
        EventLogger.logMessage(TAG, "CordovaActivity.onDestroy()");
        super.onDestroy();

        if (this.appView != null) {
            appView.handleDestroy();
        }
    }

    /**
     * Called when view focus is changed
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && immersiveMode) {
            final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        // Capture requestCode here so that it is captured in the setActivityResultCallback() case.
        cordovaInterface.setActivityResultRequestCode(requestCode);
        super.startActivityForResult(intent, requestCode, options);
    }

    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        EventLogger.logMessage(TAG, "Incoming Result. Request code = " + requestCode);
        super.onActivityResult(requestCode, resultCode, intent);
        cordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

  
    /*
     * Hook in Cordova for menu plugins
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onCreateOptionsMenu", menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onPrepareOptionsMenu", menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (appView != null) {
            appView.getPluginManager().postMessage("onOptionsItemSelected", item);
        }
        return true;
    }

    /**
     * Called when a message is sent to plugin.
     *
     * @param id            The message id
     * @param data          The message data
     * @return              Object or null
     */
    public Object onMessage(String id, Object data) {
        if ("exit".equals(id)) {
            finish();
        }
        return null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        cordovaInterface.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    /**
     * Called by the system when the device configuration changes while your activity is running.
     *
     * @param newConfig		The new device configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.appView == null) {
            return;
        }
        PluginManager pm = this.appView.getPluginManager();
        if (pm != null) {
            pm.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /* Pressing menu button causes crash in Activity with no ActionBar
          http://stackoverflow.com/questions/19275447/pressing-menu-button-causes-crash-in-activity-with-no-actionbar/19320065#19320065
         */
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
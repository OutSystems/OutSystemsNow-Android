/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.http.cookie.Cookie;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.outsystems.android.core.CordovaLoaderWebClient;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.core.CustomWebView;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.helpers.OfflineSupport;
import com.outsystems.android.mobileect.MobileECTController;
import com.outsystems.android.mobileect.interfaces.OSECTContainerListener;
import com.outsystems.android.model.Application;
import com.outsystems.android.model.MobileECT;
import com.phonegap.plugins.barcodescanner.BarcodeScanner;

/**
 * Class description.
 *
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 *
 */
public class WebApplicationActivity extends BaseActivity implements CordovaInterface, OSECTContainerListener {

    public static String KEY_APPLICATION = "key_application";
    public static String KEY_SINGLE_APPLICATION ="single_application";
    private static String OPEN_URL_EXTERNAL_BROWSER_PREFIX = "external:";

    CordovaWebView cordovaWebView;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    @SuppressWarnings("unused")
    private int activityState = 0; // 0=starting, 1=running (after 1st resume), 2=shutting down

    // Plugin to call when activity result is received
    protected CordovaPlugin activityResultCallback = null;

    private ImageButton buttonForth;
    protected ProgressDialog spinnerDialog = null;
    private ImageView imageView;

    protected boolean activityResultKeepRunning;
    private int flagNumberLoadings = 0;

    private MobileECTController mobileECTController;

    // Offline support
    private View networkErrorView;
    private View webViewGroup;

    private boolean webViewLoadingFailed = false;
    private boolean spinnerEnabled = false;


    public OnClickListener onClickListenerBack = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (cordovaWebView.canGoBack()) {
                LinearLayout viewLoading = (LinearLayout) findViewById(R.id.view_loading);
                if (viewLoading.getVisibility() != View.VISIBLE) {
                    startLoadingAnimation();
                }
                cordovaWebView.goBack();
                enableDisableButtonForth();
            } else {
                finish();
            }
        }
    };

    private OnClickListener onClickListenerForth = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (cordovaWebView.canGoForward()) {
                startLoadingAnimation();
                cordovaWebView.goForward();
                enableDisableButtonForth();
            }
        }
    };

    private OnClickListener onClickListenerApplication = new OnClickListener() {

        @Override
        public void onClick(View v) {
            finish();
        }
    };


    private OnClickListener onClickListenerOpenECT = new OnClickListener() {

        @Override
        public void onClick(View v) {
            mobileECTController.openECTView();
        }
    };


    /*
     * The variables below are used to cache some of the activity properties.
     */

    // Keep app running when pause is received. (default = true)
    // If true, then the JavaScript and native code continue to run in the background
    // when another application (activity) is started.
    protected boolean keepRunning = true;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_application);

        // Hide action bar
        getSupportActionBar().hide();

        cordovaWebView = (CustomWebView) this.findViewById(R.id.mainView);
        imageView = (ImageView) this.findViewById(R.id.image_view);
        Config.init(this);

        Application application = null;
        boolean singleApp = false;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            application = (Application) bundle.get("key_application");

            singleApp  =  bundle.get(KEY_SINGLE_APPLICATION) != null && (Boolean) bundle.get(KEY_SINGLE_APPLICATION);
        }

        // Local Url to load application
        String url = "";
        if (application != null) {
            if (HubManagerHelper.getInstance().getApplicationHosted() == null) {
                ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
                app.registerDefaultHubApplication();
            }
            url = String.format(WebServicesClient.URL_WEB_APPLICATION, HubManagerHelper.getInstance()
                    .getApplicationHosted(), application.getPath());
        }

        cordovaWebView.setWebViewClient(new CordovaCustomWebClient(this, cordovaWebView));
        cordovaWebView.setWebChromeClient(new CordovaChromeClient(this, cordovaWebView));
        cordovaWebView.getSettings().setJavaScriptEnabled(true);
        cordovaWebView.getSettings().setLightTouchEnabled(true);

        // Listener to Download Web File with Native Component - Download Manager
        cordovaWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                    long contentLength) {
                downloadAndOpenFile(WebApplicationActivity.this, url);
            }
        });


        // Synchronize WebView cookies with Login Request cookies
        CookieSyncManager.createInstance(getApplicationContext());
        android.webkit.CookieManager.getInstance().removeAllCookie();

        List<String> cookies = WebServicesClient.getInstance().getLoginCookies();
        if (cookies != null && !cookies.isEmpty()){
            for(String cookieString : cookies){
                android.webkit.CookieManager.getInstance().setCookie(HubManagerHelper.getInstance().getApplicationHosted(), cookieString);
                CookieSyncManager.getInstance().sync();
            }
        }


        // Set in the user agent OutSystemsApp
        String ua = cordovaWebView.getSettings().getUserAgentString();
        String appVersion = getAppVersion();
        String newUA = ua.concat(" OutSystemsApp v." + appVersion);
        cordovaWebView.getSettings().setUserAgentString(newUA);

        // Offline Support
        cordovaWebView.getSettings().setAppCacheMaxSize( 50 * 1024 * 1024 ); // 50MB
        cordovaWebView.getSettings().setAppCachePath( getApplicationContext().getCacheDir().getAbsolutePath() );
        cordovaWebView.getSettings().setAllowFileAccess( true );
        cordovaWebView.getSettings().setAppCacheEnabled( true );
        cordovaWebView.getSettings().setJavaScriptEnabled( true );
        cordovaWebView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE );

        ApplicationOutsystems app = (ApplicationOutsystems)getApplication();

        if ( !app.isNetworkAvailable() ) { // loading offline
            cordovaWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ONLY );
        }

        OfflineSupport.getInstance(getApplicationContext()).clearCacheIfNeeded(cordovaWebView);

        this.networkErrorView = findViewById(R.id.networkErrorInclude);
        this.webViewGroup = findViewById(R.id.webViewGroup);

        if(networkErrorView != null){
            networkErrorView.setVisibility(View.INVISIBLE);

            View retryButton = networkErrorView.findViewById(R.id.networkErrorButtonRetry);
            View appsListLink = networkErrorView.findViewById(R.id.networkErrorAppsListLink);

            retryButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    retryWebViewAction();
                }
            });

            appsListLink.setOnClickListener(onClickListenerApplication);
        }


        // Mobile ECT Feature

        View containerView = findViewById(R.id.ectViewGroup);
        View mainView = findViewById(R.id.mainViewGroup);
        DatabaseHandler database = new DatabaseHandler(getApplicationContext());
        MobileECT mobileECT = database.getMobileECT();

        boolean skipHelper = mobileECT != null && !mobileECT.isFirstLoad();

        mobileECTController = new MobileECTController(this,
                mainView,
                containerView,
                this.cordovaWebView,
                HubManagerHelper.getInstance().getApplicationHosted(),
                skipHelper);

        containerView.setVisibility(View.GONE);

        // Hide ECT Button
        ImageButton buttonECT = (ImageButton) findViewById(R.id.button_ect);
        if(buttonECT != null) {
            buttonECT.setOnClickListener(this.onClickListenerOpenECT);
            buttonECT.setVisibility(View.GONE);
        }




        // Load Application

        if (savedInstanceState == null) {
            cordovaWebView.loadUrl(url);
        } else {
            ((LinearLayout) findViewById(R.id.view_loading)).setVisibility(View.GONE);
        }

        // Customization Toolbar
        // Get Views from Xml Layout
        ImageButton buttonApplications = (ImageButton) findViewById(R.id.button_applications);
        ImageButton buttonBack = (ImageButton) findViewById(R.id.button_back);
        buttonForth = (ImageButton) findViewById(R.id.button_forth);

        // Actions onClick
        buttonApplications.setOnClickListener(onClickListenerApplication);
        buttonBack.setOnClickListener(onClickListenerBack);
        buttonForth.setOnClickListener(onClickListenerForth);

        // Background with differents states
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            buttonApplications.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_app_list)));
            buttonBack.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_left)));
            buttonForth.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_right)));
        } else {
            buttonApplications.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_app_list)));
            buttonBack.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_left)));
            buttonForth.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_right)));

        }

        // Check if it's a single application
        if(singleApp){
            buttonApplications.setVisibility(View.INVISIBLE);
            buttonApplications.setOnClickListener(null);
        }




    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        cordovaWebView.saveState(outState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        flagNumberLoadings++;
        imageView.setVisibility(View.VISIBLE);
        spinnerStart();
        cordovaWebView.restoreState(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        stopLoadingAnimation();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (cordovaWebView.canGoBack()) {
                    cordovaWebView.goBack();
                    enableDisableButtonForth();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        EventLogger.logMessage(getClass(), "on Destroy called");
        super.onDestroy();
        if (this.cordovaWebView != null) {
            this.cordovaWebView.handleDestroy();
        }
    }

    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }

    /**
     * Launch an activity for which you would like a result when it finished. When this activity exits, your
     * onActivityResult() method is called.
     *
     * @param command The command object
     * @param intent The intent to start
     * @param requestCode The request code that is passed to callback to identify the activity
     */
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        // If multitasking turned on, then disable it for activities that return results
        if (command != null) {
            this.keepRunning = false;
        }

        if (intent.getAction().contains("SCAN")) {
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
            return;
        }

        // Start activity
        super.startActivityForResult(intent, requestCode);
    }

    @Override
    /**
     * Called when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param data              An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Code to send info about File Chooser
        if (cordovaWebView != null && requestCode == CordovaChromeClient.FILECHOOSER_RESULTCODE) {
            ValueCallback<Uri> mUploadMessage = this.cordovaWebView.getWebChromeClient().getValueCallback();
            EventLogger.logMessage(getClass(), "did we get here?");
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            EventLogger.logMessage(getClass(), "result = " + result);
            // Uri filepath = Uri.parse("file://" + FileUtils.getRealPathFromURI(result, this));
            // Log.d(TAG, "result = " + filepath);
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }

        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            if (intent != null && intent.getAction() != null && intent.getAction().contains("SCAN")) {
                callback.onActivityResult(BarcodeScanner.REQUEST_CODE, resultCode, intent);
            } else {
                callback.onActivityResult(requestCode, resultCode, intent);
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    /**
     * Get the Android activity.
     *
     * @return
     */
    public Activity getActivity() {
        return this;
    }

    /**
     * Called when a message is sent to plugin.
     *
     * @param id The message id
     * @param data The message data
     * @return Object or null
     */
    public Object onMessage(String id, Object data) {

        return null;
    }

    public ExecutorService getThreadPool() {
        return threadPool;
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void enableDisableButtonForth() {
        if (cordovaWebView.canGoForward()) {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                buttonForth.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                        R.drawable.icon_chevron_right)));
            } else {
                buttonForth.setImageDrawable(createSelectorIconApplications(getResources().getDrawable(
                        R.drawable.icon_chevron_right)));
            }
        } else {
            Drawable iconForth = getResources().getDrawable(R.drawable.icon_chevron_right_inactive);

            BitmapDrawable disabled = getDisableButton(iconForth);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                buttonForth.setImageDrawable(disabled);
            } else {
                buttonForth.setImageDrawable(disabled);
            }
        }
    }

    /**
     * Creates the selector icon applications.
     *
     * @param icon the icon
     * @return the drawable
     */
    private Drawable createSelectorIconApplications(Drawable icon) {
        StateListDrawable drawable = new StateListDrawable();

        BitmapDrawable disabled = getDisableButton(icon);

        drawable.addState(new int[] { -android.R.attr.state_pressed }, icon);
        drawable.addState(new int[] { -android.R.attr.state_enabled }, icon);
        drawable.addState(StateSet.WILD_CARD, disabled);

        return drawable;
    }

    /**
     * Gets the disable button.
     *
     * @param icon the icon
     * @return the disable button
     */
    private BitmapDrawable getDisableButton(Drawable icon) {
        Bitmap enabledBitmap = ((BitmapDrawable) icon).getBitmap();

        // Setting alpha directly just didn't work, so we draw a new bitmap!
        Bitmap disabledBitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(), icon.getIntrinsicHeight(),
                android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);

        Paint paint = new Paint();
        paint.setAlpha(90);
        canvas.drawBitmap(enabledBitmap, 0, 0, paint);

        BitmapDrawable disabled = new BitmapDrawable(getResources(), disabledBitmap);

        return disabled;
    }

    /**
     *  Mobile ECT Container
     */
    public void showMobileECTButton(boolean show){
        ImageButton buttonECT = (ImageButton) findViewById(R.id.button_ect);
        if(buttonECT != null) {
            buttonECT.setVisibility(show ? View.VISIBLE : View.GONE);
            findViewById(R.id.toolbar).invalidate();
        }

    }

    @Override
    public void onSendFeedbackClickListener() {
        mobileECTController.sendFeedback();
    }

    @Override
    public void onCloseECTClickListener() {
        mobileECTController.closeECTView();
    }

    @Override
    public void onCloseECTHelperClickListener() {

        DatabaseHandler database = new DatabaseHandler(getApplicationContext());
        database.addMobileECT(false);
        mobileECTController.setSkipECTHelper(true);
    }

    @Override
    public void onShowECTFeatureListener(boolean show) {
        this.showMobileECTButton(show);
    }


    /**
     * The Class CordovaCustomWebClient.
     */
    public class CordovaCustomWebClient extends CordovaLoaderWebClient {

        public CordovaCustomWebClient(CordovaInterface cordova, CordovaWebView view) {
            super(cordova, view);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            startLoadingAnimation();
            webViewLoadingFailed = false;
            EventLogger.logMessage(getClass(), "onPageStarted - webViewLoadingFailed: "+webViewLoadingFailed);
        }

        @SuppressLint("DefaultLocale")
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            EventLogger.logMessage(getClass(), "--------------- shouldOverrideUrlLoading ---------------");
			if(url.equals("about:blank"))
            	return super.shouldOverrideUrlLoading(view, url);

            if(url.startsWith(OPEN_URL_EXTERNAL_BROWSER_PREFIX)){
                String urlString = url.substring(OPEN_URL_EXTERNAL_BROWSER_PREFIX.length());
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
                startActivity(browserIntent);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            EventLogger.logMessage(getClass(), "________________ ONPAGEFINISHED _________________");
            enableDisableButtonForth();

            // Get Mobile ECT Api Info
            mobileECTController.getECTAPIInfo();

            if(!webViewLoadingFailed)
                cordovaWebView.setVisibility(View.VISIBLE);

            stopLoadingAnimation();
            showNetworkErrorView(webViewLoadingFailed);
        }


        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            List<String> trustedHosts = WebServicesClient.getInstance().getTrustedHosts();
            String host = HubManagerHelper.getInstance().getApplicationHosted();


            webViewLoadingFailed = true;

            EventLogger.logMessage(getClass(), "onReceivedSslError - webViewLoadingFailed: "+webViewLoadingFailed);

          //TODO remove comments to force the check the validity of SSL certificates, except for list of trusted servers
            //if (trustedHosts != null && host != null) {
            //    for (String trustedHost : trustedHosts) {
            //        if (host.contains(trustedHost)) {
                        handler.proceed();
                        return;
            //        }
            //    }
            //}
            //super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            EventLogger.logMessage(getClass(), "________________ ONRECEIVEDERROR _________________");

            cordovaWebView.setVisibility(View.INVISIBLE);
            webViewLoadingFailed = true;
            stopLoadingAnimation();

            EventLogger.logMessage(getClass(), "onReceivedError - webViewLoadingFailed: "+webViewLoadingFailed);
        }

    }


    @SuppressWarnings("deprecation")
    private void startLoadingAnimation() {
        int currentOrientation = this.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

	    BitmapDrawable ob = null;

        EventLogger.logMessage(getClass(), "startLoadingAnimation - webViewLoadingFailed: "+webViewLoadingFailed);

        if(webViewLoadingFailed){
            ob = new BitmapDrawable(getBitmapForVisibleRegion(networkErrorView));
        }
        else{
            ob = new BitmapDrawable(getBitmapForVisibleRegion(cordovaWebView));
        }

	    imageView.setBackgroundDrawable(ob);

	    imageView.setVisibility(View.VISIBLE);

	    LoadingTask loadingTask = new LoadingTask();
		Timer timer = new Timer();
		timer.schedule(loadingTask, 500);
    }

    /**
     * Stop loading animation.
     */
    private void stopLoadingAnimation() {
        if (imageView.getVisibility() == View.VISIBLE) {
            final Animation animationFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
            imageView.setVisibility(View.GONE);
            imageView.startAnimation(animationFadeOut);
        }
        spinnerStop();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    /**
     * Show the spinner. Must be called from the UI thread.
     */
    public void spinnerStart() {
        if(spinnerEnabled)
            return;

        spinnerEnabled = true;

        flagNumberLoadings++;
        LinearLayout viewLoading = (LinearLayout) findViewById(R.id.view_loading);
        if (viewLoading.getVisibility() != View.VISIBLE) {
            viewLoading.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Stop spinner - Must be called from UI thread
     */
    public void spinnerStop() {

        spinnerEnabled = false;

        if (flagNumberLoadings > 1) {
            flagNumberLoadings = 0;
            return;
        }
        LinearLayout viewLoading = (LinearLayout) findViewById(R.id.view_loading);
        if (viewLoading.getVisibility() == View.VISIBLE) {
            viewLoading.setVisibility(View.GONE);
        }
        flagNumberLoadings--;
    }

    /**
     * Gets the bitmap for visible region.
     *
     * @param webview the webview
     * @return the bitmap for visible region
     */
    public Bitmap getBitmapForVisibleRegion(View webview) {
        try {
            Bitmap returnedBitmap = null;
            webview.setDrawingCacheEnabled(true);
            returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
            webview.setDrawingCacheEnabled(false);
            return returnedBitmap;
        } catch (Exception e) {
            EventLogger.logError(getClass(), e.toString());
            return null;
        }
    }

    /**
     * Gets the app version.
     *
     * @return the app version
     */
    private String getAppVersion() {
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            EventLogger.logError(getClass(), e);
        }
        return "";
    }

    // ----------------------------------------------//
    // Download File and Open with Native application//
    // ----------------------------------------------//
    private static final HashMap<String, String> MIME_TYPES;
    static {
        MIME_TYPES = new HashMap<String, String>();
        MIME_TYPES.put(".pdf", "application/pdf");
        MIME_TYPES.put(".doc", "application/msword");
        MIME_TYPES.put(".docx", "application/msword");
        MIME_TYPES.put(".xls", "application/vnd.ms-powerpoint");
        MIME_TYPES.put(".xlsx", "application/vnd.ms-powerpoint");
        MIME_TYPES.put(".rtf", "application/vnd.ms-excel");
        MIME_TYPES.put(".wav", "audio/x-wav");
        MIME_TYPES.put(".gif", "image/gif");
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".txt", "text/plain");
        MIME_TYPES.put(".mpg", "video/*");
        MIME_TYPES.put(".mpeg", "video/*");
        MIME_TYPES.put(".mpe", "video/*");
        MIME_TYPES.put(".mp4", "video/*");
        MIME_TYPES.put(".avi", "video/*");
        MIME_TYPES.put(".ods", "application/vnd.oasis.opendocument.spreadsheet");
        MIME_TYPES.put(".odt", "application/vnd.oasis.opendocument.text");
        MIME_TYPES.put(".ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MIME_TYPES.put(".apk", "application/vnd.android.package-archive");
    }

    private String getMimeType(String extension) {
        return MIME_TYPES.get(extension);
    }

    private void openFile(Uri localUri, String extension, Context context) throws JSONException {
        EventLogger.logError(getClass(), "URI --> " + localUri.getPath());
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(localUri, getMimeType(extension));
        JSONObject obj = new JSONObject();

        try {
            context.startActivity(i);
            obj.put("message", "successfull downloading and openning");
        } catch (ActivityNotFoundException e) {
            obj.put("message", "Failed to open the file, no reader found");
            obj.put("ActivityNotFoundException", e.getMessage());
        }

    }

    private void downloadAndOpenFile(final Context context, final String fileUrl) {
        String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        filename = filename.replace("%20", "");
        final String extension = fileUrl.substring(fileUrl.lastIndexOf("."));
        final File tempFile = new File(getDirectorty(), filename);

        if (tempFile.exists()) {
            try {
                openFile(Uri.fromFile(tempFile), extension, context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(fileUrl));
        String cookie = CookieManager.getInstance().getCookie(cordovaWebView.getUrl());
        r.addRequestHeader("Cookie", cookie);
        r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        final DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                context.unregisterReceiver(this);

                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadId));

                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        try {
                            EventLogger.logMessage(getClass(), "Download with success");
                            openFile(Uri.fromFile(tempFile), extension, context);
                        } catch (JSONException e) {
                            EventLogger.logError(getClass(), e);
                        }
                    } else {
                        EventLogger.logMessage(getClass(),
                                "Reason: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                    }

                }
                c.close();
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        dm.enqueue(r);
    }

    /**
     * Gets the directorty.
     *
     * @return the directorty
     */
    private File getDirectorty() {
        File directory = null;
        if (Environment.getExternalStorageState() == null) {
            // create new file directory object
            directory = new File(Environment.getDataDirectory() + "/Download/");
            // if no directory exists, create new directory
            if (!directory.exists()) {
                directory.mkdir();
            }

            // if phone DOES have sd card
        } else if (Environment.getExternalStorageState() != null) {
            // search for directory on SD card
            directory = new File(Environment.getExternalStorageDirectory() + "/Download/");
            // if no directory exists, create new directory to store test
            // results
            if (!directory.exists()) {
                directory.mkdir();
            }
        }
        return directory;
    }



	private class LoadingTask extends TimerTask {

		  @Override
		  public void run() {

		   runOnUiThread(new Runnable(){

		    @Override
		    public void run() {
		    	if (imageView.getVisibility() == View.VISIBLE)
		    		spinnerStart();
		    }});
		  }

	}


    /**
     * Offline Support
     */

    private void showNetworkErrorView(boolean show){
        if(this.networkErrorView != null && this.webViewGroup != null) {
            showNetworkErrorRetryLoading(false);
            if(!show){
                if(this.networkErrorView.getVisibility() == View.VISIBLE) {
                    final Animation animationFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fadeout);
                    animationFadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            webViewGroup.setVisibility(View.VISIBLE);
                            networkErrorView.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    networkErrorView.startAnimation(animationFadeOut);
                }
            }
            else{
                if(this.networkErrorView.getVisibility() == View.INVISIBLE) {
                    final Animation animationFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    animationFadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            webViewGroup.setVisibility(View.INVISIBLE);
                            networkErrorView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    networkErrorView.startAnimation(animationFadeIn);
                }
            }

        }
    }


    private void retryWebViewAction(){

        ApplicationOutsystems app = (ApplicationOutsystems)getApplication();

        OfflineSupport.getInstance(getApplicationContext()).retryWebViewAction(this,app,cordovaWebView);

        showNetworkErrorRetryLoading(true);
    }


    protected void showNetworkErrorRetryLoading(boolean show) {

        if(this.networkErrorView != null) {
            ProgressBar progressbar = (ProgressBar) networkErrorView.findViewById(R.id.progress_bar);
            progressbar.setVisibility(show ? View.VISIBLE : View.INVISIBLE);

            View retryButton = networkErrorView.findViewById(R.id.networkErrorButtonRetry);
            retryButton.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
    }


}
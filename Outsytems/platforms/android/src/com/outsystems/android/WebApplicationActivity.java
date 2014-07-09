/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;
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
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.webkit.HttpAuthHandler;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;
import com.phonegap.plugins.barcodescanner.BarcodeScanner;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class WebApplicationActivity extends BaseActivity implements CordovaInterface {

    CordovaWebView cordovaWebView;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    @SuppressWarnings("unused")
    private int activityState = 0; // 0=starting, 1=running (after 1st resume), 2=shutting down

    // Plugin to call when activity result is received
    protected CordovaPlugin activityResultCallback = null;

    private AssetManager mngr;
    private Button buttonForth;
    protected ProgressDialog spinnerDialog = null;
    private ImageView imageView;

    protected boolean activityResultKeepRunning;
    private int flagNumberLoadings = 0;

    private OnClickListener onClickListenerBack = new OnClickListener() {

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

        mngr = getAssets();

        // Hide action bar
        getSupportActionBar().hide();

        cordovaWebView = (CordovaWebView) this.findViewById(R.id.mainView);
        imageView = (ImageView) this.findViewById(R.id.image_view);
        Config.init(this);

        Application application = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            application = (Application) bundle.get("key_application");
        }

        // cordovaWebView.setHttpAuthUsernamePassword(HubManagerHelper.getInstance().getApplicationHosted(),
        // "OutSystems", "admin", "outsystems");

        // Authenticator.setDefault(new Authenticator() {
        // protected PasswordAuthentication getPasswordAuthentication() {
        // return new PasswordAuthentication("admin", "outsystems".toCharArray());
        // }
        // });

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

        // spinnerStart("", "Loading");

        // Set by <content src="index.html" /> in config.xml
        cordovaWebView.setWebViewClient(new CordovaCustoWebClient(this, cordovaWebView));

        cordovaWebView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                    long contentLength) {
                // downloadAndOpenFile(WebApplicationActivity.this, url);
                downloadAndOpenFilePlu(WebApplicationActivity.this, url);
            }
        });

        // Set in the user agent OutSystemsApp
        String ua = cordovaWebView.getSettings().getUserAgentString();
        String newUA = ua.concat(" OutSystemsApp v. ");
        cordovaWebView.getSettings().setUserAgentString(newUA);
        if (savedInstanceState == null) {
            cordovaWebView.loadUrl(url);
        } else {
            ((LinearLayout) findViewById(R.id.view_loading)).setVisibility(View.GONE);
        }

        // Customization Toolbar
        // Get Views from Xml Layout
        Button buttonApplications = (Button) findViewById(R.id.button_applications);
        Button buttonBack = (Button) findViewById(R.id.button_back);
        buttonForth = (Button) findViewById(R.id.button_forth);

        // Actions onClick
        buttonApplications.setOnClickListener(onClickListenerApplication);
        buttonBack.setOnClickListener(onClickListenerBack);
        buttonForth.setOnClickListener(onClickListenerForth);

        // Background with differents states
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            buttonApplications.setBackgroundDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_apps)));
            buttonBack.setBackgroundDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_back)));
            buttonForth.setBackgroundDrawable(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_forth)));
        } else {
            buttonApplications.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_apps)));
            buttonBack.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_back)));
            buttonForth.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_forth)));

        }

        // cwv.loadUrl("http://causecode.com", 60000);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v7.app.ActionBarActivity#onConfigurationChanged(android.content.res.Configuration)
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // cordovaWebView.reload();
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
    protected void onResume() {
        super.onResume();
        stopLoadingAnimation();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (cordovaWebView.canGoBack()) {
                    startLoadingAnimation();
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
        Log.d("Destroying the View", "onDestroy()");
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
            Log.d("outsystems", "did we get here?");
            if (null == mUploadMessage)
                return;
            Uri result = intent == null || resultCode != Activity.RESULT_OK ? null : intent.getData();
            Log.d("outsystems", "result = " + result);
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
                buttonForth.setBackgroundDrawable(createSelectorIconApplications(getResources().getDrawable(
                        R.drawable.icon_chevron_forth)));
            } else {
                buttonForth.setBackground(createSelectorIconApplications(getResources().getDrawable(
                        R.drawable.icon_chevron_forth)));
            }
        } else {
            Drawable iconForth = getResources().getDrawable(R.drawable.icon_chevron_forth);

            BitmapDrawable disabled = getDisableButton(iconForth);
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                buttonForth.setBackgroundDrawable(disabled);
            } else {
                buttonForth.setBackground(disabled);
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

    public class CordovaCustoWebClient extends CordovaWebViewClient {

        public CordovaCustoWebClient(CordovaInterface cordova, CordovaWebView view) {
            super(cordova, view);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            String identifierCordova = "/cdvload/";
            if (url.contains(identifierCordova)) {
                // Get path to load local file Cordova JS
                String[] split = url.split(identifierCordova);
                String path = "";
                if (split.length > 1) {
                    path = split[1];
                }

                try {
                    InputStream stream = WebApplicationActivity.this.mngr.open("www/" + path);
                    WebResourceResponse response = new WebResourceResponse("text/javascript", "UTF-8", stream);
                    return response;

                } catch (IOException e) {
                    Log.e("Outsytems - WebView", e.toString());
                }
            }
            return null;
        }

        @SuppressLint("DefaultLocale")
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("outsystems", "--------------- shouldOverrideUrlLoading ---------------");

            BitmapDrawable ob = new BitmapDrawable(getBitmapForVisibleRegion(cordovaWebView));
            imageView.setBackgroundDrawable(ob);
            imageView.setVisibility(View.VISIBLE);
            LinearLayout viewLoading = (LinearLayout) findViewById(R.id.view_loading);
            if (viewLoading.getVisibility() != View.VISIBLE)
                spinnerStart();
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.d("outsystems", "________________ ONPAGEFINISHED _________________");
            enableDisableButtonForth();
            stopLoadingAnimation();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            List<String> trustedHosts = WebServicesClient.getInstance().getTrustedHosts();
            String host = HubManagerHelper.getInstance().getApplicationHosted();
            if (trustedHosts != null && host != null) {
                for (String trustedHost : trustedHosts) {
                    if (host.contains(trustedHost)) {
                        handler.proceed();
                        return;
                    }
                }
            }
            super.onReceivedSslError(view, handler, error);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("outsystems", "________________ ONRECEIVEDERROR _________________");
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting()) {
                spinnerStop();
            } else {
                cordovaWebView.setVisibility(View.INVISIBLE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setBackgroundColor(getResources().getColor(R.color.white_color));
                // finish();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.apache.cordova.CordovaWebViewClient#onReceivedHttpAuthRequest(android.webkit.WebView,
         * android.webkit.HttpAuthHandler, java.lang.String, java.lang.String)
         */
        @Override
        public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
            Log.e("outsystems", "----------- onReceivedHttpAuthRequest ----------------");
            super.onReceivedHttpAuthRequest(view, handler, host, realm);
            handler.proceed("admin", "outsystems");
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.webkit.WebViewClient#onReceivedLoginRequest(android.webkit.WebView, java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public void onReceivedLoginRequest(WebView view, String realm, String account, String args) {
            // TODO Auto-generated method stub
            Log.e("outsystems", "----------- ONRECEIVEDLOGINREQUEST ----------------");
            super.onReceivedLoginRequest(view, realm, account, args);
        }

    }

    @SuppressWarnings("deprecation")
    private void startLoadingAnimation() {
        BitmapDrawable ob = new BitmapDrawable(getBitmapForVisibleRegion(cordovaWebView));
        imageView.setBackgroundDrawable(ob);
        imageView.setVisibility(View.VISIBLE);
        spinnerStart();
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
    }

    /**
     * Show the spinner. Must be called from the UI thread.
     * 
     * @param title Title of the dialog
     * @param message The message of the dialog
     */
    public void spinnerStart() {
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
    public static Bitmap getBitmapForVisibleRegion(WebView webview) {
        Bitmap returnedBitmap = null;
        webview.setDrawingCacheEnabled(true);
        returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
        webview.setDrawingCacheEnabled(false);
        return returnedBitmap;
    }

    public void downloadAndOpenFile(final Context context, final String urlDownload) {
        // String urlDirectDownload = urlDownload.replace("_download.aspx", "MyDocuments.aspx");
        String urlDirectDownload = urlDownload;
        // Get filename
        final String filename = getFileName(urlDirectDownload);
        // The place where the downloaded PDF file will be put
        final File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);
        if (tempFile.exists()) {
            // Delete the File
            tempFile.delete();
        }

        // Show progress dialog while downloading
        spinnerStart();
        // Create the download request
        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(urlDirectDownload));
        r.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
        r.setMimeType("application/octet-stream");
        final DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                context.unregisterReceiver(this);

                spinnerStop();
                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadId));

                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        try {
                            MimeTypeMap map = MimeTypeMap.getSingleton();
                            String ext = MimeTypeMap.getFileExtensionFromUrl(tempFile.getName());
                            String type = map.getMimeTypeFromExtension(ext);

                            if (type == null)
                                type = "*/*";

                            Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
                            Uri data = Uri.fromFile(tempFile);
                            myIntent.setDataAndType(data, type);
                            startActivity(myIntent);
                        } catch (Exception e) {
                            Log.e("outsystems", e.toString());
                        }
                    } else {
                        Log.d("outsystems", "Reason: " + c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));
                    }
                }
                c.close();
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        // Enqueue the request
        dm.enqueue(r);
    }

    public static String getFileName(String url) {
        String fileName;
        int slashIndex = url.lastIndexOf("/");
        int qIndex = url.lastIndexOf("?");
        if (qIndex > slashIndex) {// if has parameters
            fileName = url.substring(slashIndex + 1, qIndex);
        } else {
            fileName = url.substring(slashIndex + 1);
        }
        return fileName;
    }

    // ----------------------------------------------//
    // Download File and Open with plugin //
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

    private void downloadAndOpenFilePlu(final Context context, final String fileUrl) {
        final String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
        final String extension = fileUrl.substring(fileUrl.lastIndexOf("."));
        final File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), filename);

        if (tempFile.exists()) {
            try {
                openFile(Uri.fromFile(tempFile), extension, context);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return;
        }

        DownloadManager.Request r = new DownloadManager.Request(Uri.parse(fileUrl));
        r.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, filename);
        final DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                context.unregisterReceiver(this);

                long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Cursor c = dm.query(new DownloadManager.Query().setFilterById(downloadId));

                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL)
                        try {
                            openFile(Uri.fromFile(tempFile), extension, context);
                        } catch (JSONException e) {
                            Log.e("outsystems", e.toString());
                        }
                }
                c.close();
            }
        };
        context.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        dm.enqueue(r);
    }
}
package com.outsystems.android;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.cordova.Config;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.StateSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;

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

        // Local Url to load application
        String url = String.format(WebServicesClient.URL_WEB_APPLICATION, HubManagerHelper.getInstance()
                .getApplicationHosted(), application.getPath());

        // spinnerStart("", "Loading");

        // Set by <content src="index.html" /> in config.xml
        cordovaWebView.setWebViewClient(new CordovaCustoWebClient(this, cordovaWebView));

        // Set in the user agent OutSystemsApp
        String ua = cordovaWebView.getSettings().getUserAgentString();
        String newUA = ua.concat(" OutSystemsApp v. ");
        cordovaWebView.getSettings().setUserAgentString(newUA);
        cordovaWebView.loadUrl(url);

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
        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
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

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("outsystems", "--------------- shouldOverrideUrlLoading ---------------");
            BitmapDrawable ob = new BitmapDrawable(getBitmapForVisibleRegion(cordovaWebView));
            imageView.setBackgroundDrawable(ob);
            imageView.setVisibility(View.VISIBLE);
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
            spinnerStop();
        }
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
}
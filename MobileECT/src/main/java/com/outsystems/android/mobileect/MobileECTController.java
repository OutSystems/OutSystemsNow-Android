package com.outsystems.android.mobileect;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.outsystems.android.mobileect.interfaces.OSECTContainerListener;
import com.outsystems.android.mobileect.interfaces.OSECTListener;
import com.outsystems.android.mobileect.javascript.OSECTJavaScriptAPI;
import com.outsystems.android.mobileect.model.OSECTApi;
import com.outsystems.android.mobileect.model.OSECTSupportedAPIVersions;
import com.outsystems.android.mobileect.parsing.OSECTApiVersion;
import com.outsystems.android.mobileect.parsing.OSECTWebAppInfo;
import com.outsystems.android.mobileect.view.OSECTContainer;

import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;


/**
 * Created by lrs on 18-11-2014.
 */
public class MobileECTController implements OSECTListener {


    /**
     * The TAG for logging.
     */
    private static final String TAG = "MobileECTController";

    // JavaScript API

    private static final String ECT_JS_EnvironmentUID = "outsystems.api.requestInfo.getEnvironmentKey()";
    private static final String ECT_JS_EspaceUID = "outsystems.api.requestInfo.getEspaceKey()";
    private static final String ECT_JS_ApplicationUID = "outsystems.api.requestInfo.getApplicationKey()";
    private static final String ECT_JS_ScreenUID = "outsystems.api.requestInfo.getWebScreenKey()";
    private static final String ECT_JS_ScreenName = "outsystems.api.requestInfo.getWebScreenName()";
    private static final String ECT_JS_UserId = "ECT_JavaScript.userId";
    private static final String ECT_JS_UserAgentHeader = "navigator.userAgent";
    private static final String ECT_JS_SupportedApiVersions = "ECT_JavaScript.supportedApiVersions";

// Mobile ECT POST fields

    private static final String ECT_FEEDBACK_Message = "Message";
    private static final String ECT_FEEDBACK_EnvironmentUID = "EnvironmentUID";
    private static final String ECT_FEEDBACK_EspaceUID = "EspaceUID";
    private static final String ECT_FEEDBACK_ApplicationUID = "ApplicationUID";
    private static final String ECT_FEEDBACK_ScreenUID = "ScreenUID";
    private static final String ECT_FEEDBACK_ScreenName = "ScreenName";
    private static final String ECT_FEEDBACK_UserId = "UserId";
    private static final String ECT_FEEDBACK_ViewportWidth = "ViewportWidth";
    private static final String ECT_FEEDBACK_ViewportHeight = "ViewportHeight";
    private static final String ECT_FEEDBACK_UserAgentHeader = "UserAgentHeader";
    private static final String ECT_FEEDBACK_RequestURL = "RequestURL";
    private static final String ECT_FEEDBACK_SoundMessageBase64 = "FeedbackSoundMessageBase64";
    private static final String ECT_FEEDBACK_SoundMessageMimeType = "FeedbackSoundMessageMimeType";
    private static final String ECT_FEEDBACK_ScreenshotBase64 = "FeedbackScreenshotBase64";
    private static final String ECT_FEEDBACK_ScreenshotMimeType = "FeedbackScreenshotMimeType";


    // Mobile ECT Types

    private static final String ECT_Audio_MimeType = "audio/mp3";
    private static final String ECT_Image_MimeType = "image/jpeg";

    // ECT Api Version

    private static final String ECT_SUPPORTED_API_VERSION = "1.0.0";


    private Activity currentActivity;
    private View mainView;
    private View containerView;
    private WebView webView;
    private String hostname;

    private OSECTContainer ectContainerFragment;

    private OSECTWebAppInfo ectWebAppInfo;
    private OSECTSupportedAPIVersions supportedAPIVersions;

    private OSECTJavaScriptAPI javaScriptAPI;

    public MobileECTController(Activity currentActivity, View mainView, View containerView, WebView webView, String hostname){
        this.currentActivity = currentActivity;
        this.mainView = mainView;
        this.containerView = containerView;
        this.webView = webView;
        this.hostname = hostname;

        this.init();
    }


    private void init(){
        this.supportedAPIVersions = new OSECTSupportedAPIVersions();
        this.javaScriptAPI = new OSECTJavaScriptAPI(this.webView, this);
    }

    /**
     * ECT API
     */
    public void getECTAPIInfo(){

        String javaScriptFunction = "(\n" +
                "function(){\n" +
                " var obj = new Object();\n" +
                " obj.ECTAvailable = typeof ECT_JavaScript != 'undefined';\n" +
                " obj."+ECT_FEEDBACK_EnvironmentUID+" = " + ECT_JS_EnvironmentUID + "; \n" +
                " obj."+ECT_FEEDBACK_EspaceUID+" = " + ECT_JS_EspaceUID + "; \n" +
                " obj."+ECT_FEEDBACK_ApplicationUID+" = " + ECT_JS_ApplicationUID + "; \n" +
                " obj."+ECT_FEEDBACK_ScreenUID+" = " + ECT_JS_ScreenUID + "; \n" +
                " obj."+ECT_FEEDBACK_ScreenName+" = " + ECT_JS_ScreenName + "; \n" +
                " obj."+ECT_FEEDBACK_UserId+" = " + ECT_JS_UserId + "; \n" +
                " obj."+ECT_FEEDBACK_UserAgentHeader+" = " + ECT_JS_UserAgentHeader + "; \n" +
                " obj.SupportedApiVersions = " + ECT_JS_SupportedApiVersions + "; \n" +

                " var jsonString= JSON.stringify(obj);\n" +
                "return jsonString;\n" +
                "}\n" +
                ")()";


        this.javaScriptAPI.evaluateJavascript(javaScriptFunction);

    }



    /**
     * ECT Features
     */

    public void openECTView(){
        Bitmap screenCapture = getBitmapForVisibleRegion(webView);
        this.ectContainerFragment = OSECTContainer.newInstance(screenCapture);
        showOrHideContainerFragment(this.ectContainerFragment);

        int currentOrientation = this.currentActivity.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        this.currentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    public void closeECTView(){
        showOrHideContainerFragment(this.ectContainerFragment);
        this.ectContainerFragment = null;

        this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }


    private Bitmap getBitmapForVisibleRegion(WebView webview) {
        try {
            Bitmap returnedBitmap = null;
            webview.setDrawingCacheEnabled(true);
            returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
            webview.setDrawingCacheEnabled(false);
            return returnedBitmap;
        } catch (Exception e) {

            return null;
        }
    }


    private void showOrHideContainerFragment(OSECTContainer containerFrag){
        if(containerView.getVisibility() == View.GONE){
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add (containerView.getId(), containerFrag);
            fragmentTransaction.addToBackStack ("ectContainerFrag");
            fragmentTransaction.commit ();

            containerView.setVisibility(View.VISIBLE);
            mainView.setVisibility(View.GONE);
        }
        else
        {
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove (containerFrag);
            fragmentTransaction.commit ();

            containerView.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
        }
    }



    public void sendFeedback(){
        //this.isECTFeatureAvailable();
//TODO
    }


    @Override
    public void updateECTApiInfo() {
        String jsResult =  this.javaScriptAPI.getResultValue();
        boolean showECT = false;
        if(jsResult != null){
            // Parse ectInfo

            JsonReader reader = new JsonReader(new StringReader(jsResult));
// Must set lenient to parse single values
            reader.setLenient(true);
            try {
                if (reader.peek() != JsonToken.NULL) {
                    if (reader.peek() == JsonToken.STRING) {
                        String jsonString = reader.nextString();

                        try {

                            Gson gson = new Gson();
                            this.ectWebAppInfo = gson.fromJson(jsonString, OSECTWebAppInfo.class);

                        } catch (Exception e) {
                            Log.e(TAG,"Error parsing to JSON: "+e.getMessage());
                            this.ectWebAppInfo = null;
                        }

                    }
                }
            } catch (IOException e) {
                Log.e(TAG,"Error parsing jsResult: "+e.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }


            // check if ECT feature is available
            if(this.ectWebAppInfo != null){
                if(this.ectWebAppInfo.isECTAvailable()){

                    this.supportedAPIVersions.removeAllVersions();

                    List<OSECTApiVersion> apiVersions = this.ectWebAppInfo.getSupportedApiVersions();
                    if(apiVersions != null){
                        Iterator<OSECTApiVersion> iterator = apiVersions.iterator();
                        while (iterator.hasNext()){
                            OSECTApiVersion current = iterator.next();

                            OSECTApi api = new OSECTApi(current.getApiVersion(), current.isCurrentVersion(), current.getURL());
                            this.supportedAPIVersions.addVersion(api);
                        }

                        this.supportedAPIVersions.checkCompatibilityWithVersion(ECT_SUPPORTED_API_VERSION);
                    }

                    showECT = this.supportedAPIVersions.hasSupportedAPIVersion();
                }
            }
        }

        // Update activity
        OSECTContainerListener containerListener = (OSECTContainerListener)this.currentActivity;
        containerListener.onShowECTFeatureListener(showECT);
    }
}

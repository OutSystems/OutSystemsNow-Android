package com.outsystems.android.mobileect;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;

import com.google.gson.Gson;
import com.outsystems.android.mobileect.clients.OSECTWSRequestHandler;
import com.outsystems.android.mobileect.clients.OSECTWebServicesClient;
import com.outsystems.android.mobileect.interfaces.OSECTContainerListener;
import com.outsystems.android.mobileect.interfaces.OSECTListener;
import com.outsystems.android.mobileect.javascript.OSECTJavaScriptAPI;
import com.outsystems.android.mobileect.model.OSECTApi;
import com.outsystems.android.mobileect.model.OSECTSupportedAPIVersions;
import com.outsystems.android.mobileect.parsing.OSECTApiVersion;
import com.outsystems.android.mobileect.parsing.OSECTWebAppInfo;
import com.outsystems.android.mobileect.view.OSECTContainer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;


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

    private boolean skipECTHelper;

    private OSECTContainer ectContainerFragment;

    private OSECTWebAppInfo ectWebAppInfo;
    private OSECTSupportedAPIVersions supportedAPIVersions;

    private OSECTJavaScriptAPI javaScriptAPI;

    public MobileECTController(Activity currentActivity, View mainView, View containerView, WebView webView, String hostname, boolean skipECTHelper) {
        this.currentActivity = currentActivity;
        this.mainView = mainView;
        this.containerView = containerView;
        this.webView = webView;
        this.hostname = hostname;
        this.skipECTHelper = skipECTHelper;

        this.supportedAPIVersions = new OSECTSupportedAPIVersions();
        this.javaScriptAPI = new OSECTJavaScriptAPI(this.webView, this);
    }


    public boolean isSkipECTHelper() {
        return skipECTHelper;
    }

    public void setSkipECTHelper(boolean skipECTHelper) {
        this.skipECTHelper = skipECTHelper;
    }


    /**
     * ECT API
     */
    public void getECTAPIInfo() {

        String javaScriptFunction = "(\n" +
                "function(){\n" +
                " var obj = new Object();\n" +
                " obj.ECTAvailable = typeof ECT_JavaScript != 'undefined';\n" +
                " obj." + ECT_FEEDBACK_EnvironmentUID + " = " + ECT_JS_EnvironmentUID + "; \n" +
                " obj." + ECT_FEEDBACK_EspaceUID + " = " + ECT_JS_EspaceUID + "; \n" +
                " obj." + ECT_FEEDBACK_ApplicationUID + " = " + ECT_JS_ApplicationUID + "; \n" +
                " obj." + ECT_FEEDBACK_ScreenUID + " = " + ECT_JS_ScreenUID + "; \n" +
                " obj." + ECT_FEEDBACK_ScreenName + " = " + ECT_JS_ScreenName + "; \n" +
                " obj." + ECT_FEEDBACK_UserId + " = " + ECT_JS_UserId + "; \n" +
                " obj." + ECT_FEEDBACK_UserAgentHeader + " = " + ECT_JS_UserAgentHeader + "; \n" +
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

    public void openECTView() {
        Bitmap screenCapture = getBitmapForVisibleRegion(mainView);
        this.ectContainerFragment = OSECTContainer.newInstance(screenCapture, this.skipECTHelper);
        showOrHideContainerFragment(this.ectContainerFragment);

        int currentOrientation = this.currentActivity.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        this.currentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    public void closeECTView() {
        showOrHideContainerFragment(this.ectContainerFragment);
        this.ectContainerFragment.releaseMedia();
        this.ectContainerFragment = null;

        this.supportedAPIVersions.removeAllVersions();
        this.ectWebAppInfo = null;
        this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }


    private Bitmap getBitmapForVisibleRegion(View view) {
        try {
            Bitmap returnedBitmap = null;
            view.setDrawingCacheEnabled(true);
            returnedBitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return returnedBitmap;
        } catch (Exception e) {

            return null;
        }
    }


    private void showOrHideContainerFragment(OSECTContainer containerFrag) {
        if (containerView.getVisibility() == View.GONE) {
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(containerView.getId(), containerFrag);
            fragmentTransaction.addToBackStack("ectContainerFrag");
            fragmentTransaction.commit();

            containerView.setVisibility(View.VISIBLE);
            mainView.setVisibility(View.GONE);
        } else {
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(containerFrag);
            fragmentTransaction.commit();

            containerView.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
        }
    }


    /**
     * ECT API
     */

    @Override
    public void updateECTApiInfo() {
        String jsResult = this.javaScriptAPI.getResultValue();
        boolean showECT = false;
        if (jsResult != null) {
            // Parse ectInfo
            JsonReader reader = new JsonReader(new StringReader(jsResult));
            reader.setLenient(true);
            try {
                if (reader.peek() != JsonToken.NULL) {
                    if (reader.peek() == JsonToken.STRING) {
                        String jsonString = reader.nextString();

                        try {
                            // Create objects from JSON
                            Gson gson = new Gson();
                            this.ectWebAppInfo = gson.fromJson(jsonString, OSECTWebAppInfo.class);

                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing to JSON: " + e.getMessage());
                            this.ectWebAppInfo = null;
                        }

                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Error parsing jsResult: " + e.getMessage());
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }

            // check if ECT feature is available
            if (this.ectWebAppInfo != null) {
                if (this.ectWebAppInfo.isECTAvailable()) {

                    this.supportedAPIVersions.removeAllVersions();

                    List<OSECTApiVersion> apiVersions = this.ectWebAppInfo.getSupportedApiVersions();
                    if (apiVersions != null) {
                        Iterator<OSECTApiVersion> iterator = apiVersions.iterator();
                        while (iterator.hasNext()) {
                            OSECTApiVersion current = iterator.next();

                            OSECTApi api = new OSECTApi(current.getApiVersion(), current.isCurrentVersion(), current.getURL());
                            this.supportedAPIVersions.addVersion(api);
                        }

                        // check if there is one compatible version with the current supported api version
                        this.supportedAPIVersions.checkCompatibilityWithVersion(ECT_SUPPORTED_API_VERSION);
                    }

                    showECT = this.supportedAPIVersions.hasSupportedAPIVersion();
                }
            }
        }

        // Update activity
        OSECTContainerListener containerListener = (OSECTContainerListener) this.currentActivity;
        containerListener.onShowECTFeatureListener(showECT);
    }

    /**
     * ECT Feedback
     */

    public void sendFeedback() {

        OSECTWebServicesClient ectWSClient = OSECTWebServicesClient.getInstance();

        OSECTWSRequestHandler requestHandler = new OSECTWSRequestHandler() {
            @Override
            public void requestFinish(Object result, boolean error, int statusCode) {

                Log.d(TAG, "Status Code: " + statusCode);

                if (error) {
                    // show status view with error message
                    ectContainerFragment.showStatusView(true, OSECTContainer.ECT_STATUS_FAILED_MESSAGE);
                }
                else{
                    // close ect feature
                    ectContainerFragment.hideECTView();
                }
            }
        };

        // Get feedback parameters map
        Map<String, String> feedbackParams = this.getFeedbackDictionary();

        ectWSClient.saveFeedback(this.currentActivity.getBaseContext(),
                                 this.hostname,
                                 this.supportedAPIVersions.getAPIVersionURL(),
                                 feedbackParams,
                                 requestHandler);
    }


    private Map<String, String> getFeedbackDictionary() {
        HashMap<String, String> map = new HashMap<String, String>();

        // Message
        String messageString = "";

        if(!this.ectContainerFragment.hasAudioComments())
            messageString = ectContainerFragment.getFeedbackMessage();

        map.put(ECT_FEEDBACK_Message,messageString);

        // EnvironmentUID - Available in outsystems.api.requestInfo.getEnvironmentKey()
        String environmentUID = this.ectWebAppInfo.getEnvironmentUID();
        map.put(ECT_FEEDBACK_EnvironmentUID,environmentUID);

        // EspaceUID - Available in outsystems.api.requestInfo.getEspaceKey()
        String espaceUID = this.ectWebAppInfo.getEspaceUID();
        map.put(ECT_FEEDBACK_EspaceUID,espaceUID);

        // ApplicationUID - Available in outsystems.api.requestInfo.getApplicationKey()
        String applicationUID =  this.ectWebAppInfo.getApplicationUID();
        map.put(ECT_FEEDBACK_ApplicationUID, applicationUID);

        // ScreenUID - Available in outsystems.api.requestInfo.getWebScreenKey()
        String screenUID = this.ectWebAppInfo.getScreenUID();
        map.put(ECT_FEEDBACK_ScreenUID, screenUID);

        // ScreenName - Available in outsystems.api.requestInfo.getWebScreenName()
        String screenName =  this.ectWebAppInfo.getScreenName();
        map.put(ECT_FEEDBACK_ScreenName, screenName);

        // UserId - Available in ECT_JavaScript.userId
        String userId =  this.ectWebAppInfo.getUserId();
        map.put(ECT_FEEDBACK_UserId, userId);

        final DisplayMetrics displaymetrics = new DisplayMetrics();
        this.currentActivity.getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);

        // ViewportWidth
        int width = (int)(displaymetrics.widthPixels / displaymetrics.density);
        String viewportWidth = String.valueOf(width);
        map.put(ECT_FEEDBACK_ViewportWidth, viewportWidth);

        // ViewportHeight
        int height = (int)(displaymetrics.heightPixels / displaymetrics.density);
        String viewportHeight =  String.valueOf(height);
        map.put(ECT_FEEDBACK_ViewportHeight,viewportHeight);

        // UserAgentHeader - Use this JS navigator.userAgent
        String userAgentHeader = this.ectWebAppInfo.getUserAgentHeader();
        map.put(ECT_FEEDBACK_UserAgentHeader,userAgentHeader);

        // RequestURL
        String requestURL = this.webView.getUrl();
        map.put(ECT_FEEDBACK_RequestURL,requestURL);

        // FeedbackSoundMessageBase64
        String audioString = "";

        if(this.ectContainerFragment.hasAudioComments() ){
            File audioFile = this.ectContainerFragment.getAudioComments();
            if(audioFile != null) {

                byte[] byteArray = new byte[0];
                try {
                    byteArray = FileUtils.readFileToByteArray(audioFile);
                    audioString = Base64.encodeToString(byteArray, Base64.DEFAULT);

                } catch (IOException e) {
                    Log.e(TAG,"Error reading file: "+e.getMessage());
                }
                            }
            else {
                Log.e(TAG,"No audio file found!");
            }
        }

        String feedbackSoundMessageBase64 = audioString;
        map.put(ECT_FEEDBACK_SoundMessageBase64, feedbackSoundMessageBase64);

        // FeedbackSoundMessageMimeType
        String feedbackSoundMessageMimeType = ECT_Audio_MimeType;
        map.put(ECT_FEEDBACK_SoundMessageMimeType,feedbackSoundMessageMimeType);


        //  FeedbackScreenshotBase64
        Bitmap imageBitmap = this.ectContainerFragment.getScreenCapture();
        String imageString = "";

        if(imageBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            imageString = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        String feedbackScreenshotBase64 = imageString;
        map.put(ECT_FEEDBACK_ScreenshotBase64,feedbackScreenshotBase64);

        // FeedbackScreenshotMimeType
        String feedbackScreenshotMimeType = ECT_Image_MimeType;
        map.put(ECT_FEEDBACK_ScreenshotMimeType,feedbackScreenshotMimeType);


        return map;
    }




}

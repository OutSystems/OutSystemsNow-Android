package com.outsystems.android.mobileect.javascript;

import android.os.Build;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.webkit.WebView;

import com.outsystems.android.mobileect.interfaces.OSECTJavaScriptListener;
import com.outsystems.android.mobileect.interfaces.OSECTListener;
import com.outsystems.android.mobileect.model.OSECTSupportedAPIVersions;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lrs on 24-11-2014.
 */
public class OSECTJavaScriptAPI implements OSECTJavaScriptListener {

    /**
     * The TAG for logging.
     */
    private static final String TAG = "OSECTJavaScriptAPI";

    private WebView webView;
    private OSECTListener ectController;

    private String resultValue;


    public OSECTJavaScriptAPI(WebView webView, OSECTListener ectListener) {
        this.webView = webView;
        this.ectController = ectListener;
    }


    public void evaluateJavascript(String expression) {
        if (webView == null)
            return;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            OSECTJavaScriptCallback jsCallback = new OSECTJavaScriptCallback(this);
            jsCallback.getJSValue(webView,expression);

        } else {
            OSECTJavaScriptInterface jsInterface = new OSECTJavaScriptInterface(this);
            webView.addJavascriptInterface(jsInterface,jsInterface.getInterfaceName());
            jsInterface.getJSValue(webView,expression);
        }

    }


    public String getResultValue() {
        return resultValue;
    }

    public void setResultValue(String resultValue) {
        this.resultValue = resultValue;
    }


    @Override
    public void onReceiveValue(String value) {
        // Parse JS Results
        this.setResultValue(value);
        // Update Mobile ECT Controller
        ectController.updateECTApiInfo();
    }

}

package com.outsystems.android.mobileect.javascript;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.outsystems.android.mobileect.interfaces.OSECTJavaScriptListener;

/**
 * Created by lrs on 24-11-2014.
 */
public class OSECTJavaScriptCallback  implements ValueCallback<String> {

    /** The TAG for logging. */
    private static final String TAG = "OSECTJavaScriptCallback";

    public String resultValue;
    private OSECTJavaScriptListener jsListener;

    public OSECTJavaScriptCallback(OSECTJavaScriptListener jsListener){
        this.jsListener = jsListener;
    }

    /**
     * Evaluates the expression and returns the value.
     * @param webView
     * @param expression
     * @return
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void getJSValue(WebView webView, String expression)
    {
        String code = "javascript:((function(){try{return "+expression+";}catch(js_eval_err){return '';}})());";
        webView.evaluateJavascript(code,this);
    }


    @Override
    public void onReceiveValue(String value) {
        Log.d(TAG,"onReceiveValue: "+ value);
        this.resultValue = value;
        this.jsListener.onReceiveValue(this.resultValue);
    }

}
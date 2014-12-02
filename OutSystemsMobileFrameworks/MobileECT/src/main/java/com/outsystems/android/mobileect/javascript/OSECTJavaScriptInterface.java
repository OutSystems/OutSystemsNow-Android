package com.outsystems.android.mobileect.javascript;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.outsystems.android.mobileect.interfaces.OSECTJavaScriptListener;

/**
 * Created by lrs on 21-11-2014.
 */
public class OSECTJavaScriptInterface {

    /** The TAG for logging. */
    private static final String TAG = "OSECTJavaScriptInterface";

    /** The javascript interface name for adding to web view. */
    private final String interfaceName = "OSECTHandler";


    /** Return value to wait for. */
    private String resultValue;

    /** JavaScript listener **/
    private OSECTJavaScriptListener jsListener;

    /**
     * Base Constructor.app
     */
    public OSECTJavaScriptInterface(OSECTJavaScriptListener jsListener){
        this.jsListener = jsListener;
    }


    /**
     * Evaluates the expression and returns the value.
     * @param webView
     * @param expression
     * @return
     */
    public void getJSValue(WebView webView, String expression)
    {
        String code = "javascript:"+interfaceName + ".setValue((function(){try{return " + expression
                + "+\"\";}catch(js_eval_err){return '';}})());";

        webView.loadUrl(code);

    }


    /**
     * Receives the value from the javascript.
     * @param value
     */
    @JavascriptInterface
    public void setValue(String value)
    {
        Log.d(TAG,"onReceiveValue: "+ value);
        resultValue = value;
        this.jsListener.onReceiveValue(resultValue);
    }

    /**
     * Gets the interface name
     * @return
     */
    public String getInterfaceName(){
        return this.interfaceName;
    }

    /**
     * Gets the return value
     * @return
     */
    public String getResultValue() {
        return resultValue;
    }

}

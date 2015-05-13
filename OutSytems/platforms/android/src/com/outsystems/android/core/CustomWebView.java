package com.outsystems.android.core;

import org.apache.cordova.CordovaChromeClient;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

public class CustomWebView extends CordovaWebView {
	private CordovaWebViewClient webViewClient;
    private CordovaChromeClient chromeClient;
	
	public CustomWebView(Context context) {
		super(context);
	}

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Deprecated
    public CustomWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(11)
    @Deprecated
    public CustomWebView(Context context, AttributeSet attrs, int defStyle, boolean privateBrowsing) {
        super(context, attrs, defStyle, privateBrowsing);
    }
	
    // OS Fix
    // mlc@03/10/2014
    // When the WebClientView is already set, do not replace it with a new one    
    @Override
    public void setWebViewClient(WebViewClient client) {
        this.webViewClient = (CordovaWebViewClient)client;
        super.setWebViewClient(client);
    }
            
    @Override
    public CordovaWebViewClient makeWebViewClient(CordovaInterface cordova) {
    	if(this.webViewClient == null)
    	{
    		webViewClient = super.makeWebViewClient(cordova);
    	}
    	return this.webViewClient;
    }

    @Override
    public void setWebChromeClient(WebChromeClient client) {
        this.chromeClient = (CordovaChromeClient)client;
        super.setWebChromeClient(client);
    }

    @Override
    public CordovaChromeClient makeWebChromeClient(CordovaInterface cordova) {
        if(this.chromeClient == null){
            this.chromeClient = super.makeWebChromeClient(cordova);
        }
        return this.chromeClient;
    }
}
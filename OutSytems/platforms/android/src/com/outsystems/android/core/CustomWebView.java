package com.outsystems.android;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;

import android.content.Context;
import android.webkit.WebViewClient;

final class OutSystemsNowCustomWebView extends CordovaWebView {
	private CordovaWebViewClient webViewClient;
	
	public OutSystemsNowCustomWebView(Context context) {
		super(context);
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
}
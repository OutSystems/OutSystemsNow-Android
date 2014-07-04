/*
 * OutSystems Project
 *
 * Copyright (C) 2014 OutSystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.widget.Toast;

public class Loader extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        // TODO Auto-generated method stub
        if (action.equals("echo")) {
            @SuppressWarnings("unused")
            String message = args.getString(0);
            // this.echo(message, callbackContext);
            Toast.makeText(cordova.getActivity(), "Toast test", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

//    @Override
//    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
//        // TODO Auto-generated method stub
//        super.initialize(cordova, webView);
//
//        webView.setWebViewClient(new WebViewClient() {
//
//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
//                // TODO Auto-generated method stub
//                return super.shouldInterceptRequest(view, url);
//            }
//
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                // TODO Auto-generated method stub
//                return super.shouldOverrideUrlLoading(view, url);
//            }
//
//            @Override
//            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                // TODO Auto-generated method stub
//                super.onPageStarted(view, url, favicon);
//            }
//
//        });
//    }

}

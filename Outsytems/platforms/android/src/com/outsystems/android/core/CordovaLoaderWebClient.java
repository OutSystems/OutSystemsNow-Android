/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android.core;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;

import android.content.res.AssetManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class CordovaLoaderWebClient extends CordovaWebViewClient {

    /** The identifier cordova. */
    private static String IDENTIFIER_CORDOVA = "/cdvload/";

    /** The mngr. */
    private AssetManager mngr;

    /**
     * @param cordova
     * @param view
     */
    public CordovaLoaderWebClient(CordovaInterface cordova, CordovaWebView view) {
        super(cordova, view);
        mngr = cordova.getActivity().getAssets();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.webkit.WebViewClient#shouldInterceptRequest(android.webkit.WebView, java.lang.String)
     */
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.contains(IDENTIFIER_CORDOVA)) {
            // Get path to load local file Cordova JS
            String[] split = url.split(IDENTIFIER_CORDOVA);
            String path = "";
            if (split.length > 1) {
                path = split[1];
            }

            try {
                InputStream stream = mngr.open("www/" + path);
                WebResourceResponse response = new WebResourceResponse("text/javascript", "UTF-8", stream);
                return response;

            } catch (IOException e) {
                EventLogger.logError(getClass(), e);
            }
        }
        return null;
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.outsystems.android;

import java.io.IOException;
import java.io.InputStream;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewClient;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;

import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;

public class Outsystems extends CordovaActivity {

    public static String KEY_APPLICATION = "key_application";

    private static String PREFIX_CORDOVA_JS = "/cdvload/";
    private static String USER_AGENT_OUTSYSTEMS = " OutSystemsApp v. ";

    private AssetManager mngr;
    private Button buttonForth;

    private OnClickListener onClickListenerBack = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (appView.canGoBack()) {
                appView.goBack();
                enableDisableButtonForth();
            } else {
                finish();
            }
        }
    };

    private OnClickListener onClickListenerForth = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (appView.canGoForward()) {
                appView.goForward();
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

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mngr = getAssets();
        Application application = null;
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            application = (Application) bundle.get(KEY_APPLICATION);

            // Local Url to load application
            String url = "https://" + HubManagerHelper.getInstance().getApplicationHosted() + "/"
                    + application.getPath();

            spinnerStart("", "Loading");

            super.init();
            int backgroundColor = this.getIntegerProperty("BackgroundColor", Color.WHITE);
            this.root.setBackgroundColor(backgroundColor);
            // Set by <content src="index.html" /> in config.xml
            appView.setWebViewClient(new CordovaCustoWebClient(this, super.appView));

            //
            String ua = appView.getSettings().getUserAgentString();
            String newUA = ua.concat(USER_AGENT_OUTSYSTEMS);
            appView.getSettings().setUserAgentString(newUA);
            // super.loadUrl(Config.getStartUrl());
            super.loadUrl(url);

            this.root.setBackgroundColor(backgroundColor);

            View view = new View(getApplicationContext());
            view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 2));
            view.setBackgroundColor(getResources().getColor(R.color.button_disable));
            this.root.addView(view);

            LayoutInflater li = LayoutInflater.from(getActivity());
            View toolbarWebApplication = li.inflate(R.layout.toolbar_web_application, null, false);

            // Get Views from Xml Layout
            Button buttonApplications = (Button) toolbarWebApplication.findViewById(R.id.button_applications);
            Button buttonBack = (Button) toolbarWebApplication.findViewById(R.id.button_back);
            buttonForth = (Button) toolbarWebApplication.findViewById(R.id.button_forth);

            // Actions onClick
            buttonApplications.setOnClickListener(onClickListenerApplication);
            buttonBack.setOnClickListener(onClickListenerBack);
            buttonForth.setOnClickListener(onClickListenerForth);

            // Background with differents states
            buttonApplications.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_apps)));
            buttonBack.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_back)));
            buttonForth.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_forth)));

            float heightToolbar = getResources().getDimension(R.dimen.toolbar_height);
            // this.root.addView(toolbarWebApplication);
            this.root.addView(toolbarWebApplication,
                    new LayoutParams(LayoutParams.MATCH_PARENT, (int) heightToolbar, 1));

            // super.loadUrl("file:///android_asset/www/index.html");

        }
    }

    @SuppressLint("NewApi")
    private void enableDisableButtonForth() {
        if (appView.canGoForward()) {
            buttonForth.setBackground(createSelectorIconApplications(getResources().getDrawable(
                    R.drawable.icon_chevron_forth)));
        } else {
            Drawable iconForth = getResources().getDrawable(R.drawable.icon_chevron_forth);

            BitmapDrawable disabled = getDisableButton(iconForth);
            buttonForth.setBackground(disabled);
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
                Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);

        Paint paint = new Paint();
        paint.setAlpha(90);
        canvas.drawBitmap(enabledBitmap, 0, 0, paint);

        BitmapDrawable disabled = new BitmapDrawable(getResources(), disabledBitmap);

        return disabled;
    }
    
    private void animateStart(final WebView view) {
        Animation anim = AnimationUtils.loadAnimation(getBaseContext(),
                android.R.anim.fade_in);
        view.startAnimation(anim);
    }
    
    private void animateEnd(final WebView view) {
        Animation anim = AnimationUtils.loadAnimation(getBaseContext(), android.R.anim.fade_out);
        view.startAnimation(anim);
    }

    public class CordovaCustoWebClient extends CordovaWebViewClient {

        public CordovaCustoWebClient(CordovaInterface cordova, CordovaWebView view) {
            super(cordova, view);
        }

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
            Log.d("OUTSYSTEMS", "URL: " + url);
            if (url.contains(PREFIX_CORDOVA_JS)) {

                // Get path to load local file Cordova JS
                String[] split = url.split(PREFIX_CORDOVA_JS);
                String path = "";
                if (split.length > 1) {
                    path = split[1];
                }

                try {
                    InputStream stream = Outsystems.this.mngr.open("www/" + path);
                    WebResourceResponse response = new WebResourceResponse("text/javascript", "UTF-8", stream);
                    return response;

                } catch (IOException e) {
                    Log.e("Outsytems - WebView", e.toString());
                }
            }
            return null;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            enableDisableButtonForth();
        }

    }
}

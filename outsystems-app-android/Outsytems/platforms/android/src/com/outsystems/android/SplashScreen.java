/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.arellomobile.android.push.PushManager;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.helpers.DeepLinkController;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.DeepLink;
import com.outsystems.android.model.HubApplicationModel;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class SplashScreen extends Activity {

    /** The time splash screen. */
    public static int TIME_SPLASH_SCREEN = 2000;
    private static PushManager pushManager;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);
     
        // Get data from Deep Link
        Uri data = this.getIntent().getData();

        if(data != null){
        	DeepLinkController.getInstance().createSettingsFromUrl(data);
        }        
        
        
        // Add delay to show splashscreen
        delaySplashScreen();
    }

    private void delaySplashScreen() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                goNextActivity();
            }
        }, TIME_SPLASH_SCREEN);
    }

    protected void goNextActivity() {
        DatabaseHandler database = new DatabaseHandler(getApplicationContext());
        List<HubApplicationModel> hubApplications = database.getAllHubApllications();
        openHubActivity();
        
        if(DeepLinkController.getInstance().hasValidSettings()){
        	DeepLink deepLinkSettings = DeepLinkController.getInstance().getDeepLinkSettings();
        	HubManagerHelper.getInstance().setApplicationHosted(deepLinkSettings.getEnvironment());
        	
            Intent intent = new Intent(getApplicationContext(), HubAppActivity.class);
            startActivity(intent);
        	
        }
        else{
	        	
	        if (hubApplications != null && hubApplications.size() > 0) {
	            HubApplicationModel hubApplication = hubApplications.get(0);
	            if (hubApplication != null) {
	                HubManagerHelper.getInstance().setApplicationHosted(hubApplication.getHost());
	                HubManagerHelper.getInstance().setJSFApplicationServer(hubApplication.isJSF());
	            }
	            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
	            if (hubApplication != null) {
	                intent.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, hubApplication.getName());
	                intent.putExtra(LoginActivity.KEY_AUTOMATICLY_LOGIN, true);
	            }
	            startActivity(intent);
	        }
	    }
        finish();
    }

    private void openHubActivity() {
        Intent intent = new Intent(this, HubAppActivity.class);
        startActivity(intent);
    }        
          
}

package com.outsystems.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.outsystems.android.ApplicationOutsystems;
import com.outsystems.android.ApplicationsActivity;
import com.outsystems.android.LoginActivity;
import com.outsystems.android.R;
import com.outsystems.android.WebApplicationActivity;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.model.AppSettings;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by lrs on 8/19/2015.
 */
public class ApplicationSettingsController {

    private static ApplicationSettingsController _instance;
    private AppSettings settings;
    private Context context;

    public ApplicationSettingsController(Context context){
        this.loadSettings(context);
        this.context = context;
    }

    public static ApplicationSettingsController getInstance(Context context) {
        if (_instance == null) {
            _instance = new ApplicationSettingsController(context);
        }
        return _instance;
    }


    private void loadSettings(Context context){
        Gson gson = new Gson();
        InputStream raw =  context.getResources().openRawResource(R.raw.appsettings);
        Reader rd = new BufferedReader(new InputStreamReader(raw));
        AppSettings appSettings = gson.fromJson(rd,AppSettings.class);

        this.settings = appSettings;
    }

    public boolean hasValidSettings(){
        return this.settings != null && settings.hasValidSettings();
    }


    public boolean hideActionBar(Activity currentActivity){
        boolean result = this.hasValidSettings();

        if (result){
            if(currentActivity instanceof ApplicationsActivity){
                result = this.settings.skipNativeLogin();
            }
        }

        return result;
    }

    public Intent getFirstActivity(){
        Intent result = null;

        // Create Entry to save hub application
        DatabaseHandler database = new DatabaseHandler(context);
        if (database.getHubApplication(settings.getDefaultHostname()) == null) {
            database.addHostHubApplication(settings.getDefaultHostname(), settings.getDefaultHostname(), HubManagerHelper
                    .getInstance().isJSFApplicationServer());
        }

        HubManagerHelper.getInstance().setApplicationHosted(settings.getDefaultHostname());


        if(settings.skipNativeLogin()){
            if(settings.skipApplicationList()){
                result = new Intent(context, WebApplicationActivity.class); // webview
            }
            else{
                result = new Intent(context, ApplicationsActivity.class); // applist
            }
        }
        else {
            result = new Intent(context, LoginActivity.class);

            result.putExtra(LoginActivity.KEY_AUTOMATICLY_LOGIN, false);
            result.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, settings.getDefaultHostname());

        }

        return result;
    }
}

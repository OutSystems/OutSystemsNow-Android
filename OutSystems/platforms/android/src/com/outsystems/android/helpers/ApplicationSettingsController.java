package com.outsystems.android.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.outsystems.android.ApplicationsActivity;
import com.outsystems.android.LoginActivity;
import com.outsystems.android.R;
import com.outsystems.android.WebApplicationActivity;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.model.AppSettings;
import com.outsystems.android.model.Application;

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

    public ApplicationSettingsController(){
    }

    public static ApplicationSettingsController getInstance() {
        if (_instance == null) {
            _instance = new ApplicationSettingsController();
        }
        return _instance;
    }


    public void loadSettings(Context context){
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

    public boolean hideNavigationBar(){
        return this.hasValidSettings() && this.settings.hideNavigationBar();
    }


    public Intent getFirstActivity(Context context, boolean offline){
        Intent result = null;


        // Create Entry to save hub application
        DatabaseHandler database = new DatabaseHandler(context);
        if (database.getHubApplication(settings.getDefaultHostname()) == null) {
            database.addHostHubApplication(settings.getDefaultHostname(), settings.getDefaultHostname(), HubManagerHelper
                    .getInstance().isJSFApplicationServer());
        }

        database.close();

        HubManagerHelper.getInstance().setApplicationHosted(settings.getDefaultHostname());


        if(offline || settings.skipNativeLogin()){
            if(settings.skipApplicationList() && settings.getDefaultApplicationURL() != null){

                String url = settings.getDefaultApplicationURL();

                // Ensure that the url format its correct
                String applicationName = url.replace("\\", "/");

                // Get the application's name
                if(applicationName.contains("/")){

                    while(applicationName.startsWith("/")){
                        applicationName = applicationName.substring(1);
                    }

                    url = applicationName;

                    int slashPosition = applicationName.indexOf("/");

                    if(slashPosition > 0 ){
                        applicationName = applicationName.substring(0,slashPosition);
                    }
                }

                Application application = new Application(applicationName, -1, applicationName);
                application.setPath(url);

                result = new Intent(context, WebApplicationActivity.class); // webview
                result.putExtra(WebApplicationActivity.KEY_APPLICATION, application);
                result.putExtra(WebApplicationActivity.KEY_SINGLE_APPLICATION,true);


            }
            else{
                result = new Intent(context, ApplicationsActivity.class); // applist
            }
        }
        else {
            result = new Intent(context, LoginActivity.class);

            result.putExtra(LoginActivity.KEY_AUTOMATICALLY_LOGIN, false);
            result.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, settings.getDefaultHostname());

        }

        return result;
    }

    public Intent getNextActivity(Activity currentActivity){
        Intent result = null;

        if (!this.hasValidSettings())
            return null;

        if(currentActivity instanceof LoginActivity){

            if(settings.skipApplicationList()){
                // Go to WebApplicationActivity
                if (settings.getDefaultApplicationURL() != null){

                    String url = settings.getDefaultApplicationURL();

                    // Ensure that the url format its correct
                    String applicationName = url.replace("\\", "/");

                    // Get the application's name
                    if(applicationName.contains("/")){

                        while(applicationName.startsWith("/")){
                            applicationName = applicationName.substring(1);
                        }

                        url = applicationName;

                        int slashPosition = applicationName.indexOf("/");

                        if(slashPosition > 0 ){
                            applicationName = applicationName.substring(0,slashPosition);
                        }
                    }

                    Application application = new Application(applicationName, -1, applicationName);
                    application.setPath(url);

                    result = new Intent(currentActivity.getApplicationContext(), WebApplicationActivity.class); // webview
                    result.putExtra(WebApplicationActivity.KEY_APPLICATION, application);
                    result.putExtra(WebApplicationActivity.KEY_SINGLE_APPLICATION,true);

                    return result;
                }
            }

            // Otherwise...
            // Go to ApplicationsActivity
            result = new Intent(currentActivity.getApplicationContext(), ApplicationsActivity.class);

        }


        return result;
    }

    public AppSettings getSettings(){
        return settings;
    }

}

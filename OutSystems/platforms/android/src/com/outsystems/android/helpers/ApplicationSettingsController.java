package com.outsystems.android.helpers;

import android.content.Context;

import com.google.gson.Gson;
import com.outsystems.android.R;
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

    public ApplicationSettingsController(Context context){
        this.loadSettings(context);
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

        return this.settings != null;
    }

}

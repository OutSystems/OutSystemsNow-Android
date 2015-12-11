/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.ApplicationSettingsController;
import com.outsystems.android.helpers.DeepLinkController;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.helpers.OfflineSupport;
import com.outsystems.android.model.AppSettings;
import com.outsystems.android.model.Infrastructure;
import com.outsystems.android.widgets.CustomFontTextView;

/**
 * Class Hub App Activity.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class HubAppActivity extends BaseActivity {

	public boolean getInfrastructure = false;
    
    public HubAppActivity(){
    	super();
 
    }
    
    private void callInfrastructureService(final View v, final String urlHubApp) {

        final HubAppActivity activity = this;

        showLoading(v);
        WebServicesClient.getInstance().getInfrastructure(urlHubApp, new WSRequestHandler() {

            @Override
            public void requestFinish(Object result, boolean error, int statusCode) {
                EventLogger.logMessage(getClass(), "Status Code: " + statusCode);
                if (!error) {
                    Infrastructure infrastructure = (Infrastructure) result;
                    if (infrastructure == null) {
                        ((EditText) findViewById(R.id.edit_text_hub_url))
                                .setError(getString(R.string.label_error_wrong_address));
                        showError(findViewById(R.id.root_view));
                        stopLoading(v);
                        return;
                    } else if (infrastructure.getVersion() == null || !infrastructure.getVersion().startsWith(getString(R.string.required_module_version))) {
                    	// invalid OutSystems Now modules in the server         
                    	((EditText) findViewById(R.id.edit_text_hub_url))
                        	.setError(getString(R.string.label_invalid_version));
                    	showError(findViewById(R.id.root_view));
                    	stopLoading(v);
                    	return;
                    }

                    // Create Entry to save hub application
                    DatabaseHandler database = new DatabaseHandler(getApplicationContext());
                    if (database.getHubApplication(urlHubApp) == null) {
                        database.addHostHubApplication(urlHubApp, infrastructure.getName(), HubManagerHelper
                                .getInstance().isJSFApplicationServer());
                    }
                    database.close();

                    HubManagerHelper.getInstance().setApplicationHosted(urlHubApp);

                    ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
                    app.setDemoApplications(false);

                    boolean hasAppSettings = ApplicationSettingsController.getInstance().hasValidSettings();
                    Intent intent = null;

                    if(hasAppSettings) {
                        intent = ApplicationSettingsController.getInstance().getNextActivity(activity);
                    }

                    if(intent == null) {
                        // Start Login Activity
                        intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.putExtra(LoginActivity.KEY_AUTOMATICALLY_LOGIN, false);
                        if (infrastructure != null) {
                            intent.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, infrastructure.getName());
                        }
                    }

                    startActivity(intent);
                } else {
                    ((EditText) findViewById(R.id.edit_text_hub_url))
                            .setError(WebServicesClient.PrettyErrorMessage(statusCode)); // getString(R.string.label_error_wrong_address)
                    // avoid crashes
                    //  ((EditText) findViewById(R.id.edit_text_hub_url)).setMovementMethod(LinkMovementMethod.getInstance()); // enable links
                    showError(findViewById(R.id.root_view));
                }
                stopLoading(v);
            }
        });
 
	
    }

    private String checkEnvironmentURL(String url){
        String result = url.replaceAll(" ", "");

        String http = "http://";
        String https = "https://";

        if(result.startsWith(http)){
            result = result.substring(http.length());
        }
        else{
            if(result.startsWith(https)){
                result = result.substring(https.length());
            }
        }

        return result;
    }


    /** The on click listener. */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            String urlHubApp = ((EditText) findViewById(R.id.edit_text_hub_url)).getText().toString();
            HubManagerHelper.getInstance().setJSFApplicationServer(false);

            hideKeyboard();

            if (!"".equals(urlHubApp)) {
                ((EditText) findViewById(R.id.edit_text_hub_url)).setError(null);

                String urlHub = checkEnvironmentURL(urlHubApp);

                if(!urlHub.equals(urlHubApp)){
                    ((EditText) findViewById(R.id.edit_text_hub_url)).setText(urlHub);
                    urlHubApp = urlHub;
                }
                callInfrastructureService(v, urlHubApp);
            } else {
                ((EditText) findViewById(R.id.edit_text_hub_url))
                        .setError(getString(R.string.label_error_empty_address));
                showError(findViewById(R.id.root_view));
            }
        }
    };

    private OnClickListener onClickListenerDemo = new OnClickListener() {

        @Override
        public void onClick(View v) {
            HubManagerHelper.getInstance().setApplicationHosted(WebServicesClient.DEMO_HOST_NAME);
            HubManagerHelper.getInstance().setJSFApplicationServer(false);
            ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
            app.setDemoApplications(true);
            Intent intent = new Intent(getApplicationContext(), ApplicationsActivity.class);
            startActivity(intent);
        }
    };

    private OnClickListener onClickListenerHelp = new OnClickListener() {

        @SuppressWarnings("deprecation")
        @Override
        public void onClick(View v) {
            hideKeyboard();

            ImageButton imageButton = (ImageButton) findViewById(R.id.image_button_icon);
            LinearLayout viewHelp = (LinearLayout) findViewById(R.id.view_help);
            if (viewHelp.getVisibility() == View.VISIBLE) {
                viewHelp.setVisibility(View.GONE);
                imageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_help));

                // Application Settings
                boolean hasValidSettings = ApplicationSettingsController.getInstance().hasValidSettings();
                if(hasValidSettings){
                    // Change colors
                    AppSettings appSettings =  ApplicationSettingsController.getInstance().getSettings();

                    boolean customFgColor = appSettings.getTintColor() != null && !appSettings.getForegroundColor().isEmpty();
                    if(customFgColor){
                        int newColor = Color.parseColor(appSettings.getForegroundColor());
                        Drawable drawable = imageButton.getBackground();
                        drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                    }
                }

            } else {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_top);
                viewHelp.startAnimation(anim);
                viewHelp.setVisibility(View.VISIBLE);
                imageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_close));

                // Application Settings
                boolean hasValidSettings = ApplicationSettingsController.getInstance().hasValidSettings();
                if(hasValidSettings){
                    // Change colors
                    AppSettings appSettings =  ApplicationSettingsController.getInstance().getSettings();

                    boolean customFgColor = appSettings.getTintColor() != null && !appSettings.getForegroundColor().isEmpty();
                    if(customFgColor){
                        int newColor = Color.parseColor(appSettings.getForegroundColor());
                        Drawable drawable = imageButton.getBackground();
                        drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hub_app);
       
        final Button buttonGO = (Button) findViewById(R.id.button_go);
        buttonGO.setOnClickListener(onClickListener);

        Button buttonDemo = (Button) findViewById(R.id.button_demo);
        buttonDemo.setOnClickListener(onClickListenerDemo);

        ImageButton buttonHelp = (ImageButton) findViewById(R.id.image_button_icon);
        buttonHelp.setOnClickListener(onClickListenerHelp);

        // Events to Open external Browser
        aboutEvents();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Hide action bar
                getSupportActionBar().hide();
            }
        });

        
        // Check if deep link has valid settings                
        if(DeepLinkController.getInstance().hasValidSettings()){        	
        	DeepLinkController.getInstance().resolveOperation(this, null);
        }
        else{
        	getInfrastructure = false;
        }

        // Set Hostname
        String hostname = HubManagerHelper.getInstance().getApplicationHosted();
        if (hostname != null && !"".equals(hostname)) {
            ((EditText) findViewById(R.id.edit_text_hub_url)).setText(hostname);
            
            if(getInfrastructure){
            	callInfrastructureService(buttonGO, hostname);
            }
            
        } else {
            ((EditText) findViewById(R.id.edit_text_hub_url)).setText("");
        }

        final EditText editText = (EditText) findViewById(R.id.edit_text_hub_url);
        editText.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {
                int width = editText.getWidth();
                int height = editText.getHeight();

                ViewGroup.LayoutParams params = buttonGO.getLayoutParams();
                params.height = height;
                params.width = width;
                buttonGO.requestLayout();

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN)
                    editText.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    editText.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });


        // Application Settings
        boolean hasValidSettings = ApplicationSettingsController.getInstance().hasValidSettings();
        if(hasValidSettings){

            // Hide Demo
            View tryOurDemo = findViewById(R.id.try_our_demo);
            if(tryOurDemo != null){
                tryOurDemo.setVisibility(View.GONE);
            }

            // Change colors
            AppSettings appSettings =  ApplicationSettingsController.getInstance().getSettings();

            boolean customBgColor = appSettings.getTintColor() != null && !appSettings.getBackgroundColor().isEmpty();

            if(customBgColor){
                View root = findViewById(R.id.root_view);
                root.setBackgroundColor(Color.parseColor(appSettings.getBackgroundColor()));
            }

            boolean customFgColor = appSettings.getTintColor() != null && !appSettings.getForegroundColor().isEmpty();
            if(customFgColor){
                int newColor = Color.parseColor(appSettings.getForegroundColor());
                Drawable drawable = buttonGO.getBackground();
                drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);
                buttonGO.setTextColor(newColor);

                drawable = buttonHelp.getBackground();
                drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);

                ProgressBar progressBar = (ProgressBar)findViewById(R.id.progress_bar);
                drawable = progressBar.getIndeterminateDrawable();
                drawable.setColorFilter(newColor, PorterDuff.Mode.SRC_ATOP);

                CustomFontTextView environmentLabel = (CustomFontTextView)findViewById(R.id.text_view_title_hub);
                if(environmentLabel != null){
                    environmentLabel.setTextColor(newColor);
                }

                CustomFontTextView helpLabel = (CustomFontTextView)findViewById(R.id.text_view_help);
                if(helpLabel != null){
                    helpLabel.setTextColor(newColor);
                }
            }

        }

    }

    private void hideKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (this.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }
    
}

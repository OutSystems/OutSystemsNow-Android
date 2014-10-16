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
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.DeepLinkController;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Infrastructure;

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

                    HubManagerHelper.getInstance().setApplicationHosted(urlHubApp);

                    ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
                    app.setDemoApplications(false);
                    // Start Login Activity
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.putExtra(LoginActivity.KEY_AUTOMATICLY_LOGIN, false);
                    if (infrastructure != null) {
                        intent.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, infrastructure.getName());
                    }
                    startActivity(intent);
                } else {
                    ((EditText) findViewById(R.id.edit_text_hub_url))
                            .setError(WebServicesClient.PrettyErrorMessage(statusCode)); // getString(R.string.label_error_wrong_address)
                    ((EditText) findViewById(R.id.edit_text_hub_url)).setMovementMethod(LinkMovementMethod.getInstance()); // enable links
                    showError(findViewById(R.id.root_view));
                }
                stopLoading(v);
            }
        });
 
	
    }
    
    /** The on click listener. */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            final String urlHubApp = ((EditText) findViewById(R.id.edit_text_hub_url)).getText().toString();
            HubManagerHelper.getInstance().setJSFApplicationServer(false);

            if (!"".equals(urlHubApp)) {
                ((EditText) findViewById(R.id.edit_text_hub_url)).setError(null);
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
            HubManagerHelper.getInstance().setJSFApplicationServer(true);
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
            ImageButton imageButton = (ImageButton) findViewById(R.id.image_button_icon);
            LinearLayout viewHelp = (LinearLayout) findViewById(R.id.view_help);
            if (viewHelp.getVisibility() == View.VISIBLE) {
                viewHelp.setVisibility(View.GONE);
                imageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_help));
            } else {
                Animation anim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_top);
                viewHelp.startAnimation(anim);
                viewHelp.setVisibility(View.VISIBLE);
                imageButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.icon_close));
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

        // Hide action bar
        getSupportActionBar().hide();
        
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
        

    }

    
}

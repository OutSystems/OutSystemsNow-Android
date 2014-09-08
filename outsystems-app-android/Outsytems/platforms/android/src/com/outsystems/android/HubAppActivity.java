/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
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

import com.arellomobile.android.push.BasePushMessageReceiver;
import com.arellomobile.android.push.PushManager;
import com.arellomobile.android.push.utils.RegisterBroadcastReceiver;
import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
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

    /** The on click listener. */
    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            final String urlHubApp = ((EditText) findViewById(R.id.edit_text_hub_url)).getText().toString();
            HubManagerHelper.getInstance().setJSFApplicationServer(false);
            if (!"".equals(urlHubApp)) {
                ((EditText) findViewById(R.id.edit_text_hub_url)).setError(null);
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

        // Register receivers for push notifications
        registerReceivers();





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

        // Set Hostname
        String hostname = HubManagerHelper.getInstance().getApplicationHosted();
        if (hostname != null && !"".equals(hostname)) {
            ((EditText) findViewById(R.id.edit_text_hub_url)).setText(hostname);
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

    @Override
    public void onResume() {
        super.onResume();

        // Re-register receivers on resume
        registerReceivers();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Unregister receivers on pause
        unregisterReceivers();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        checkMessage(intent);

        setIntent(new Intent());
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        HubManagerHelper.getInstance().setApplicationHosted(null);
    }

    /** Methods to Push Notifications */
    // Registration receiver
    BroadcastReceiver mBroadcastReceiver = new RegisterBroadcastReceiver() {
        @Override
        public void onRegisterActionReceive(Context context, Intent intent) {
            checkMessage(intent);
        }
    };

    // Push message receiver
    private BroadcastReceiver mReceiver = new BasePushMessageReceiver() {
        @Override
        protected void onMessageReceive(Intent intent) {
            // JSON_DATA_KEY contains JSON payload of push notification.
            // showMessage("push message is " + intent.getExtras().getString(JSON_DATA_KEY));
            doOnMessageReceive(intent.getExtras().getString(JSON_DATA_KEY));

        }
    };

    // Registration of the receivers
    public void registerReceivers() {
        IntentFilter intentFilter = new IntentFilter(getPackageName() + ".action.PUSH_MESSAGE_RECEIVE");

        registerReceiver(mReceiver, intentFilter);

        registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "."
                + PushManager.REGISTER_BROAD_CAST_ACTION));
    }

    public void unregisterReceivers() {
        // Unregister receivers on pause
        try {
            unregisterReceiver(mReceiver);
        } catch (Exception e) {
            EventLogger.logError(getClass(), e);
        }

        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            EventLogger.logError(getClass(), e);
        }
    }

    private void checkMessage(Intent intent) {
        if (null != intent) {

            resetIntentValues();
        }
    }

    /**
     * Will check main Activity intent and if it contains any PushWoosh data, will clear it
     */
    private void resetIntentValues() {
        Intent mainAppIntent = getIntent();


        setIntent(mainAppIntent);
    }

    private void showMessage(String message) {
        // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void callRegisterToken(String deviceId) {
        WebServicesClient.getInstance().registerToken(deviceId, new WSRequestHandler() {

            @Override
            public void requestFinish(Object result, boolean error, int statusCode) {
                EventLogger.logMessage(getClass(), "Register Token in the server");
            }
        });
    }

    public void doOnMessageReceive(String message) {
        try {
            JSONObject messageJson = new JSONObject(message);
            if (messageJson.has("title")) {
                String title = messageJson.getString("title");
                AlertDialog.Builder builder = new AlertDialog.Builder(HubAppActivity.this);
                builder.setMessage(title).setTitle(getString(R.string.app_name));
                builder.setNeutralButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (JSONException e) {
            EventLogger.logError(getClass(), e);
        }
    }
}

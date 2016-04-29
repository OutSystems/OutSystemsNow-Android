/*
 * OutSystems Project
 *
 * Copyright (C) 2014 OutSystems.
 *
 * This software is proprietary.
 */
package com.outsystems.android;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.android.push.BasePushMessageReceiver;
import com.arellomobile.android.push.PushManager;
import com.arellomobile.android.push.utils.RegisterBroadcastReceiver;
import com.outsystems.android.core.EventLogger;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.DeepLinkController;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.widgets.TypefaceSpan;

import java.util.ArrayList;

/**
 * Class Base Activity.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class BaseActivity extends ActionBarActivity {
	
	private BroadcastReceiver mBroadcastReceiver;
	private BroadcastReceiver mReceiver;
    private boolean broadcastReceiversRegistered = false;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registration receiver
        mBroadcastReceiver = new RegisterBroadcastReceiver() {
            @Override
            public void onRegisterActionReceive(Context context, Intent intent) {
                checkMessage(intent);
            }
        };

        // Push message receiver
        mReceiver = new BasePushMessageReceiver() {
            @Override
            protected void onMessageReceive(Intent intent) {
                // JSON_DATA_KEY contains JSON payload of push notification.
                // showMessage("push message is " + intent.getExtras().getString(JSON_DATA_KEY));
                doOnMessageReceive(intent.getExtras().getString(JSON_DATA_KEY));
            }
        };

        // Register receivers for push notifications
        registerReceivers();

        checkMessage(getIntent());

    }

    private OnClickListener onClickListenerHyperLink = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String link = getString(R.string.label_about_link);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + link));
            startActivity(browserIntent);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        case android.R.id.home:
            try {
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    finish();
                }
            }catch(Exception e){
                EventLogger.logError(getClass(), e);
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets the title action bar.
     *
     * @param title the new title action bar
     */
    protected void setTitleActionBar(String title) {
        // Set text with Custom Font of app
        SpannableString titleActionBar = new SpannableString(title);
        titleActionBar.setSpan(new TypefaceSpan(this, "OpenSans-Regular.ttf"), 0, titleActionBar.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setTitle(titleActionBar);
    }

    /**
     * Setup action bar.
     */
    protected void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowCustomEnabled(false);
        ab.setIcon(null);

        setTitleActionBar(getResources().getString(R.string.label_back));

        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setLogo(getResources().getDrawable(R.drawable.icon_chevron_back));
    }

    /**
     * About events.
     */
    protected void aboutEvents() {
        TextView textViewLink = (TextView) findViewById(R.id.text_view_about_link);
        textViewLink.setOnClickListener(onClickListenerHyperLink);
    }

    /**
     * Show loading.
     *
     * @param buttonClick the button click
     */
    protected void showLoading(View buttonClick) {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
        buttonClick.setVisibility(View.INVISIBLE);
        progressbar.setVisibility(View.VISIBLE);
    }

    /**
     * Stop loading.
     *
     * @param buttonClick the button click
     */
    protected void stopLoading(View buttonClick) {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
        buttonClick.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.INVISIBLE);
    }

    /**
     * Show error.
     *
     * @param viewError the view error
     */
    protected void showError(View viewError) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        viewError.startAnimation(shake);
    }

    /** Methods to Push Notifications */
    // Registration of the receivers
    public void registerReceivers() {
        if(!broadcastReceiversRegistered) {
            IntentFilter intentFilter = new IntentFilter(getPackageName() + ".action.PUSH_MESSAGE_RECEIVE");

            registerReceiver(mReceiver, intentFilter);

            registerReceiver(mBroadcastReceiver, new IntentFilter(getPackageName() + "."
                    + PushManager.REGISTER_BROAD_CAST_ACTION));

            broadcastReceiversRegistered = true;
        }
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

        broadcastReceiversRegistered = false;
    }

    protected void checkMessage(Intent intent) {
        if (null != intent) {
            if (intent.hasExtra(PushManager.PUSH_RECEIVE_EVENT)) {
                // showMessage("push message is " + intent.getExtras().getString(PushManager.PUSH_RECEIVE_EVENT));
                doOnMessageReceive(intent.getExtras().getString(PushManager.PUSH_RECEIVE_EVENT));
            } else if (intent.hasExtra(PushManager.REGISTER_EVENT)) {
                String deviceId = intent.getExtras().getString(PushManager.REGISTER_EVENT);
                HubManagerHelper.getInstance().setDeviceId(deviceId);
                final ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
                app.callRegisterToken(deviceId);
            } else if (intent.hasExtra(PushManager.UNREGISTER_EVENT)) {
                showMessage("unregister");
            } else if (intent.hasExtra(PushManager.REGISTER_ERROR_EVENT)) {
                showMessage("register error");
            } else if (intent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT)) {
                showMessage("unregister error");
            }

            resetIntentValues();
        }
    }
    
    private void showMessage(String message) {
        // Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void callRegisterToken(String deviceId) {
        WebServicesClient.getInstance().registerToken(getApplicationContext(), deviceId, new WSRequestHandler() {

            @Override
            public void requestFinish(Object result, boolean error, int statusCode) {
                EventLogger.logMessage(getClass(), "Register Token in the server");
            }
        });
    }

    public void doOnMessageReceive(final String message) {
        EventLogger.logMessage(getClass(), "Received push message: " + message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    final JSONObject messageJson = new JSONObject(message);
                    if (messageJson.has("title")) {

                        // Showing a dialog when the app is going to background can cause some crashs
                        // http://blackriver.to/2012/08/android-annoying-exception-unable-to-add-window-is-your-activity-running/
                        if (!isFinishing()) {
                            String title = messageJson.getString("title");
                            AlertDialog.Builder builder = new AlertDialog.Builder(BaseActivity.this);
                            builder.setCancelable(true);
                            builder.setMessage(title).setTitle(getString(R.string.app_name));

                            if(messageJson.has("u")) {
                                try {
                                    String pnData = messageJson.getString("u");
                                    JSONObject deeplinkJson = new JSONObject(pnData);

                                    if (deeplinkJson != null && deeplinkJson.has("deeplink")) {
                                        builder.setNegativeButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int i) {
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    EventLogger.logError(getClass(), e);
                                }
                            }

                            builder.setNeutralButton(getString(R.string.button_ok), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    if(messageJson.has("u")) {

                                        String urlToOpen = null;
                                        try {
                                            String pnData = messageJson.getString("u");
                                            JSONObject deeplinkJson = new JSONObject(pnData);
                                            urlToOpen = deeplinkJson.getString("deeplink");
                                        } catch (Exception e) {
                                            EventLogger.logError(getClass(), e);
                                        }

                                        if (urlToOpen != null && !"".equals(urlToOpen)) {

                                            Uri uri = DeepLinkController.convertUrlToUri(urlToOpen, "osnow");

                                            DeepLinkController.getInstance().createSettingsFromUrl(uri);

                                            Intent intent = new Intent(getApplicationContext(), HubAppActivity.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                        }
                                    } else if(messageJson.has("l")) {
                                        try {
                                            String url = messageJson.getString("l");
                                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                            startActivity(browserIntent);
                                        }
                                        catch(Exception e)
                                        {
                                            EventLogger.logError(getClass(), e);
                                        }
                                    }
                                }
                            });


                            builder.show();

                        }

                    }
                } catch (Exception e) {
                    EventLogger.logError(getClass(), e);
                }
            }
        });
    }
    
    /**
     * Will check main Activity intent and if it contains any PushWoosh data, will clear it
     */
    private void resetIntentValues() {
        Intent mainAppIntent = getIntent();

        if (mainAppIntent.hasExtra(PushManager.PUSH_RECEIVE_EVENT)) {
            mainAppIntent.removeExtra(PushManager.PUSH_RECEIVE_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.REGISTER_EVENT)) {
            mainAppIntent.removeExtra(PushManager.REGISTER_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_EVENT)) {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.REGISTER_ERROR_EVENT)) {
            mainAppIntent.removeExtra(PushManager.REGISTER_ERROR_EVENT);
        } else if (mainAppIntent.hasExtra(PushManager.UNREGISTER_ERROR_EVENT)) {
            mainAppIntent.removeExtra(PushManager.UNREGISTER_ERROR_EVENT);
        }

        setIntent(mainAppIntent);
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
    
}

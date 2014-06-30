package com.outsystems.android;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.outsystems.android.core.DatabaseHandler;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Infrastructure;

public class HubAppActivity extends BaseActivity {

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            final String urlHubApp = ((EditText) findViewById(R.id.edit_text_hub_url)).getText().toString();
            HubManagerHelper.getInstance().setJSFApplicationServer(false);
            if (!"".equals(urlHubApp)) {
                showLoading(v);
                WebServicesClient.getInstance().getInfrastructure(urlHubApp, new WSRequestHandler() {

                    @Override
                    public void requestFinish(Object result, boolean error, int statusCode) {
                        Log.d("outystems", "Status Code: " + statusCode);
                        if (!error) {
                            Infrastructure infrastructure = (Infrastructure) result;

                            // Create Entry to save hub application
                            DatabaseHandler database = new DatabaseHandler(getApplicationContext());
                            if (database.getHubApplication(urlHubApp) == null) {
                                database.addHostHubApplication(urlHubApp, infrastructure.getName(), HubManagerHelper
                                        .getInstance().isJSFApplicationServer());
                            }

                            HubManagerHelper.getInstance().setApplicationHosted(urlHubApp);

                            // Start Login Activity
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            intent.putExtra(LoginActivity.KEY_AUTOMATICLY_LOGIN, false);
                            if (infrastructure != null) {
                                intent.putExtra(LoginActivity.KEY_INFRASTRUCTURE_NAME, infrastructure.getName());
                            }
                            startActivity(intent);
                        }
                        stopLoading(v);
                    }

                    @Override
                    public void requestError(int statusCode) {
                        // TODO Auto-generated method stub

                    }
                });
            }
        }
    };

    private OnClickListener onClickListenerDemo = new OnClickListener() {

        @Override
        public void onClick(View v) {
            HubManagerHelper.getInstance().setApplicationHosted(WebServicesClient.DEMO_HOST_NAME);
            HubManagerHelper.getInstance().setJSFApplicationServer(true);
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

        // Set Hostname
        String hostname = HubManagerHelper.getInstance().getApplicationHosted();
        if (hostname != null && !"".equals(hostname)) {
            ((EditText) findViewById(R.id.edit_text_hub_url)).setText(hostname);
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

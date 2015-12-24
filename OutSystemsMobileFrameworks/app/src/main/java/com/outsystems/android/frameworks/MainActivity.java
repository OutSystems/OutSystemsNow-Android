package com.outsystems.android.frameworks;

import android.app.Activity;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import com.outsystems.android.mobileect.MobileECTController;
import com.outsystems.android.mobileect.interfaces.OSECTContainerListener;

public class MainActivity extends Activity implements OSECTContainerListener {

    MobileECTController mobileECTController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(getActionBar() != null)
            getActionBar().hide();

        WebView webView = (WebView)this.findViewById(R.id.mainWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        webView.loadUrl("https://labsdev.outsystems.net/CarAccidentReporting");

        View mainView = findViewById(R.id.mainViewGroup);
        View containerView = findViewById(R.id.ectViewGroup);

        mobileECTController = new MobileECTController(this,
                mainView, containerView, webView, "https://labsdev.outsystems.net", true);

        ImageButton openButton = (ImageButton)this.findViewById(R.id.button_ect);

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mobileECTController.openECTView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSendFeedbackClickListener() {
        mobileECTController.sendFeedback();
    }

    @Override
    public void onCloseECTClickListener() {
        mobileECTController.closeECTView();
    }

    @Override
    public void onCloseECTHelperClickListener() {
        mobileECTController.setSkipECTHelper(true);
    }

    @Override
    public void onShowECTFeatureListener(boolean show) {

    }
}

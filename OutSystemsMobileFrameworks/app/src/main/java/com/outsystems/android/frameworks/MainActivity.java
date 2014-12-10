package com.outsystems.android.frameworks;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import com.outsystems.android.mobileect.MobileECTController;


public class MainActivity extends Activity {

    MobileECTController mobileECTController;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WebView webView = (WebView)this.findViewById(R.id.mainWebView);
        webView.loadUrl("http://labsdev.outsystems.net/Native");


        View containerView = findViewById(R.id.ectViewGroup);

        View mainView = findViewById(R.id.mainViewGroup);


        mobileECTController = new MobileECTController(this,
                mainView,
                containerView,
                webView,
                "labsdev.outsystems.net",
                false);

        Button openButton = (Button)this.findViewById(R.id.openButton);
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
}

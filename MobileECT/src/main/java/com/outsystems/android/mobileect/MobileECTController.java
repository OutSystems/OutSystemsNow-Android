package com.outsystems.android.mobileect;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;

import com.outsystems.android.mobileect.view.OSECTContainer;


/**
 * Created by lrs on 18-11-2014.
 */
public class MobileECTController {

    private Activity currentActivity;
    private View mainView;
    private View containerView;
    private WebView webView;
    private String hostname;

    private OSECTContainer ectContainerFragment;

    public MobileECTController(Activity currentActivity, View mainView, View containerView, WebView webView, String hostname){
        this.mainView = mainView;
        this.containerView = containerView;
        this.webView = webView;
        this.hostname = hostname;
        this.currentActivity = currentActivity;
    }



    public boolean isECTFeatureAvailable(){
        boolean result = true;

        return result;
    }


    public void openECTView(){
        Bitmap screenCapture = getBitmapForVisibleRegion(webView);
        this.ectContainerFragment = OSECTContainer.newInstance(screenCapture);
        showOrHideContainerFragment(this.ectContainerFragment);

        int currentOrientation = this.currentActivity.getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        this.currentActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    public void closeECTView(){
        showOrHideContainerFragment(this.ectContainerFragment);
        this.ectContainerFragment = null;

        this.currentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    }


    private Bitmap getBitmapForVisibleRegion(WebView webview) {
        try {
            Bitmap returnedBitmap = null;
            webview.setDrawingCacheEnabled(true);
            returnedBitmap = Bitmap.createBitmap(webview.getDrawingCache());
            webview.setDrawingCacheEnabled(false);
            return returnedBitmap;
        } catch (Exception e) {

            return null;
        }
    }


    private void showOrHideContainerFragment(OSECTContainer containerFrag){
        if(containerView.getVisibility() == View.GONE){
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add (containerView.getId(), containerFrag);
            fragmentTransaction.addToBackStack ("ectContainerFrag");
            fragmentTransaction.commit ();

            containerView.setVisibility(View.VISIBLE);
            mainView.setVisibility(View.GONE);
        }
        else
        {
            FragmentManager fragmentManager = currentActivity.getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove (containerFrag);
            fragmentTransaction.commit ();

            containerView.setVisibility(View.GONE);
            mainView.setVisibility(View.VISIBLE);
        }
    }

}

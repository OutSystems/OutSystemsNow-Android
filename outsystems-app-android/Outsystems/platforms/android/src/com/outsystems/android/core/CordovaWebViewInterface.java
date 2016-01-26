package com.outsystems.android.core;

import android.app.Activity;
import android.content.Intent;

import com.phonegap.plugins.barcodescanner.BarcodeScanner;

import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;

import java.util.concurrent.ExecutorService;

public class CordovaWebViewInterface extends CordovaInterfaceImpl {

    public CordovaWebViewInterface(Activity activity) {
        super(activity);
    }

    public CordovaWebViewInterface(Activity activity, ExecutorService threadPool) {
        super(activity, threadPool);
    }

    public CordovaPlugin getActivityResultCallback() {
        return this.activityResultCallback;
    }


    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {

        if (intent.getAction() != null && intent.getAction().contains("SCAN")) {
            intent.putExtra("requestCode",requestCode);
            super.startActivityForResult(command, intent, 0x0000c0de);
        }
        else{
            super.startActivityForResult(command, intent, requestCode);
        }

    }


    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (intent != null && intent.getAction() != null && intent.getAction().contains("SCAN")) {
            return super.onActivityResult(BarcodeScanner.REQUEST_CODE, resultCode, intent);
        } else {
            return super.onActivityResult(requestCode, resultCode, intent);
        }
    }
}

package com.outsystems.android.mobileect.clients;

/**
 * Created by lrs on 24-11-2014.
 */
public abstract class OSECTWSRequestHandler  {

    public abstract void requestFinish(Object result, boolean error, int statusCode);

}
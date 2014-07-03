package com.outsystems.android.core;

public abstract class WSRequestHandler {

    public abstract void requestFinish(Object result, boolean error, int statusCode);

}
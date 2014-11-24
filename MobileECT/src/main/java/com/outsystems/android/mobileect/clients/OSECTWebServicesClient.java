package com.outsystems.android.mobileect.clients;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lrs on 24-11-2014.
 */
public class OSECTWebServicesClient {

    public static final String BASE_URL = "http://%1$s/%2$s";

    private static volatile OSECTWebServicesClient instance = null;

    private AsyncHttpClient client = null;


    private Context context;


    // private constructor
    private OSECTWebServicesClient() {
        client = new AsyncHttpClient();
    }

    public static String PrettyErrorMessage(int statusCode) {
        switch(statusCode) {
            case -1001:
                return "The request timed out.";
            case -1003:
                return "Could not contact the specified server. Please verify the server name and your internet connection and try again.";
            case -1206:
                return "An SSL error has occurred and a secure connection to the server cannot be made.";
            case 404:
                return "The required ECT service was not detected, please try again.";
            default:
                return "There was an error trying to connect to the provided environment, please try again.";
        }
    }

    public static OSECTWebServicesClient getInstance() {
        instance = new OSECTWebServicesClient();
        return instance;
    }


    // post for content parameters
    private void post(String hubApp, String urlPath,
                      Map<String, String> parameters,
                      AsyncHttpResponseHandler asyncHttpResponseHandler) {

        RequestParams params = null;
        if (parameters != null) {
            params = new RequestParams(parameters);
        }

        client.setSSLSocketFactory(getSSLMySSLSocketFactory());
  

        client.post(context, getAbsoluteUrl(hubApp, urlPath), params,
                asyncHttpResponseHandler);
    }


    private MySSLSocketFactory getSSLMySSLSocketFactory() {
        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
        } catch (NoSuchAlgorithmException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        } catch (CertificateException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        } catch (IOException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        } catch (KeyStoreException e1) {
            Log.e(getClass().toString(), e1.getMessage(), e1);
        }
        MySSLSocketFactory sf = null;
        try {
            sf = new MySSLSocketFactory(trustStore);
        } catch (KeyManagementException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        } catch (UnrecoverableKeyException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        } catch (KeyStoreException e) {
            Log.e(getClass().toString(), e.getMessage(), e);
        }
        sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        return sf;
    }

    public static String getAbsoluteUrl(String hostname, String relativeUrl) {
        return String.format(BASE_URL, hostname,relativeUrl);
    }


    /**
     * Save Feedback
     */

    public void saveFeedback(final Context context, final String hostname,
                             final String ectURL,
                             final Map<String, String> feedbackParams,
                               final OSECTWSRequestHandler handler) {

        this.context = context;

        post(hostname, ectURL,
                feedbackParams, new AsyncHttpResponseHandler() {

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable arg3) {

                        try {
                            if(arg3 != null && arg3.getMessage() !=  null) {
                                if(arg3.getMessage().indexOf("UnknownHostException") != -1) {
                                    statusCode = -1003; // NSURLErrorCannotFindHost
                                } else if (arg3.getMessage().indexOf("SSL handshake timed out") != -1) {
                                    statusCode = -1206; // NSURLErrorClientCertificateRequired
                                } else if (arg3.getMessage().indexOf("SocketTimeoutException") != -1) {
                                    statusCode = -1001; // NSURLErrorTimedOut
                                }
                            }
                        } catch (Exception ex) {
                            statusCode = -1;
                        }

                        handler.requestFinish(null, true, statusCode);
                        // Show Error message
                    }

                    @Override
                    public void onSuccess(final int statusCode,
                                          Header[] headers, final byte[] content) {
                        if (statusCode != 200) {
                            handler.requestFinish(null, true, statusCode);
                            // Show Error message
                        } else {
                            // Close ECT
                            handler.requestFinish(null, false, statusCode);
                        }
                    }
                });
    }
}

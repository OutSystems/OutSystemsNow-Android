package com.outsystems.plugins.filechooser;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int FILECHOOSER_REQUESTCODE = 2001;

    private static final String MIME_TYPE_AUDIO = "audio/*";
    private static final String MIME_TYPE_IMAGE = "image/*";
    private static final String MIME_TYPE_VIDEO = "video/*";

    private static final String PARAM_ACCEPT = "accept";
    private static final String PARAM_CAPTURE = "capture";

    CallbackContext callback;
    private Uri fileUri;

    @Override
    public boolean execute(String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            this.cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    chooseFile(args, callbackContext);
                }
            });

            return true;
        }

        return false;
    }

    /**
     * Method to open a file chooser
     *
     * @param args
     * @param callbackContext
     */
    public void chooseFile( CordovaArgs args, CallbackContext callbackContext) {
        /* Specifies the types of files accepted by the server */      
        String acceptType = null;
        /* Specifies the media capture directly from the device */
        boolean capture = false;

        try {
            JSONObject obj = args.getJSONObject(0);
            acceptType = obj.getString(PARAM_ACCEPT);

            String captureString = obj.getString(PARAM_CAPTURE);
            if(captureString != null){
                try{
                    capture  = captureString.isEmpty() || Boolean.valueOf(captureString);
                }
                catch (Exception e){
                    capture = true;
                }
            }
        } catch (JSONException e) {
            Log.w(TAG, e.getMessage());
        }

        /* Launch an intent for a single type of file, otherwise returns false */
        boolean singleIntent = launchSingleIntent(acceptType, capture);

        if(!singleIntent){
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            ArrayList<Intent> otherIntents = new ArrayList();

            otherIntents.add(this.getImageIntent());
            otherIntents.add(this.getVideoIntent());
            otherIntents.add(this.getSoundIntent());
            otherIntents.add(this.getMyFilesIntent());

            Parcelable[] parcelables = new Parcelable[otherIntents.size()];
            for (int i = 0; i < parcelables.length; i++) {
                parcelables[i] = otherIntents.get(i);
            }

            Intent chooserIntent = Intent.createChooser(intent, "Choose an action");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, parcelables);
            cordova.startActivityForResult(this, chooserIntent, FILECHOOSER_REQUESTCODE);
        }

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    /**
     * Method to launch an intent for a specific type of file with/without direct capturing
     *
     * @param acceptType
     * @param capture
     * @returns true if a single intent was launched     
     */
    private boolean launchSingleIntent(String acceptType, boolean capture){

        boolean single = false;

        if(acceptType != null && acceptType.length() > 1){
            StringTokenizer st = new StringTokenizer(acceptType,",");
            single = st.countTokens() == 1;
        }

        /* If only on type of file was specified lauchn its intent*/
        if(single){

            Intent intent = getIntentForType(acceptType);

            if (intent == null){
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
            }

            if(capture){
                cordova.startActivityForResult(this, intent, FILECHOOSER_REQUESTCODE);
            }
            else{
                /* Launch a chooser intent if capture is not defined */
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.addCategory(Intent.CATEGORY_OPENABLE);
                fileIntent.setType("*/*");

                /* Create chooser intent */
                Intent chooserIntent = Intent.createChooser(fileIntent, "Choose an action");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{intent});

                cordova.startActivityForResult(this, chooserIntent, FILECHOOSER_REQUESTCODE);

            }

        }

        return single;
    }
    

    /**
     * Method to get an intent for a specific type of file
     *
     * @param type     
     * @returns an intent     
     */    
    private final Intent getIntentForType(String type){
        Intent result = null;

        if(type.equalsIgnoreCase(MIME_TYPE_IMAGE)){
            result = getImageIntent();
        }
        else{
            if (type.equalsIgnoreCase(MIME_TYPE_VIDEO)){
                result = getVideoIntent();
            }
            else{
                if(type.equalsIgnoreCase(MIME_TYPE_AUDIO)){
                    result = getSoundIntent();
                }
            }
        }

        return result;
    }


   /**
     * Method to get an intent for image files
     *     
     * @returns an intent     
     */ 
    private final Intent getImageIntent(){
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        fileUri = getOutputMediaFile(MIME_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        return intent;
    }

    public Uri getOutputMediaFile(String type)
    {
        if(Environment.getExternalStorageState() != null) {

            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"");

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mediaFile;
            if(type.equalsIgnoreCase(MIME_TYPE_IMAGE)) {
                mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                        "IMG_"+ timeStamp + ".jpeg");
            } else {
                return null;
            }

            return Uri.fromFile(mediaFile);
        }

        return null;
    }

    /**
     * Method to get an intent for video files
     *     
     * @returns an intent     
     */ 
    private final Intent getVideoIntent(){
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        return intent;
    }

    /**
     * Method to get an intent for audio files
     *     
     * @returns an intent     
     */ 
    private final Intent getSoundIntent(){
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        return intent;
    }


    /**
     * Method to get an intent for "my files" explorer
     *     
     * @returns an intent     
     */ 
    private final Intent getMyFilesIntent(){
        Intent intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
        intent.putExtra("CONTENT_TYPE", "*/*");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        return intent;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == FILECHOOSER_REQUESTCODE && callback != null) {

            if (resultCode == Activity.RESULT_OK) {
                if(data != null){
                    Uri uri = data.getData();

                    if (uri != null) {
                        Log.w(TAG, uri.toString());
                        callback.success(uri.toString());
                    } else {
                        callback.error("File uri was null");
                    }
                }
                else{
                    if(fileUri != null){
                        Log.w(TAG, fileUri.toString());
                        callback.success(fileUri.toString());
                        fileUri = null;
                    } else {
                        callback.error("File uri was null");
                    }
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);
            } else {
                callback.error(resultCode);
            }

        }
    }
}

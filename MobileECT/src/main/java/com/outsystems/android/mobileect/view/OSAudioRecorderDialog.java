package com.outsystems.android.mobileect.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.Chronometer;

import com.outsystems.android.mobileect.R;
import com.outsystems.android.mobileect.interfaces.OSECTAudioRecorderListener;

import static com.outsystems.android.mobileect.R.color.outSystems_red;


/**
 * Created by lrs on 25-11-2014.
 */
public class OSAudioRecorderDialog extends DialogFragment {


    OSECTAudioRecorderListener mCallback;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OSECTAudioRecorderDialog.
     */
    public static OSAudioRecorderDialog newInstance(OSECTAudioRecorderListener mCallback) {
        OSAudioRecorderDialog fragment = new OSAudioRecorderDialog();
        fragment.mCallback = mCallback;
        return fragment;
    }

    public OSAudioRecorderDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.ect_audiorecorder_alertview, null))
                // Add action buttons
                .setPositiveButton(R.string.audio_recorder_dialog_done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        stopRecording();
                    }
                })
                .setNegativeButton(R.string.audio_recorder_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        OSAudioRecorderDialog.this.getDialog().cancel();
                        cancelRecording();
                    }
                });


        return builder.create();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Button pButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button nButton =  ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);

        if(pButton!= null)
            pButton.setTextColor(getResources().getColor(outSystems_red));

        if(nButton != null)
            nButton.setTextColor(getResources().getColor(outSystems_red));

        getDialog().setCanceledOnTouchOutside(false);

        Chronometer audioRecorderTimer = (Chronometer) getDialog().findViewById(R.id.audioRecorderChronometer);
        if(audioRecorderTimer != null){

            audioRecorderTimer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
                @Override
                public void onChronometerTick(Chronometer chronometer) {
                    long myElapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();

                    if(myElapsedMillis>=30000)
                    {
                        chronometer.stop();
                        OSAudioRecorderDialog.this.getDialog().dismiss();
                        stopRecording();
                    }
                }
            });

            audioRecorderTimer.setFormat("%s");

            audioRecorderTimer.start();
        }
    }


    public void cancelRecording(){
         mCallback.onCancelAudioRecorder();
    }


    public void stopRecording(){
        mCallback.onStopAudioRecorder();
    }
}

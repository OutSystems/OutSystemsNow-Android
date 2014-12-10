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
import android.widget.TextView;

import com.outsystems.android.mobileect.R;

import static com.outsystems.android.mobileect.R.color.outSystems_red;


/**
 * Created by lrs on 09-12-2014.
 */
public class OSAlertMessageDialog extends DialogFragment {

    DialogInterface.OnClickListener positiveButtonClickListener;
    DialogInterface.OnClickListener negativeButtonClickListener;
    int titleId;
    int messageId;
    int positiveButtonTitleId;
    int negativeButtonTitleId;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OSECTAudioRecorderDialog.
     */
    public static OSAlertMessageDialog newInstance(int titleId, int messageId,
                                                   int positiveButtonTitleId, int negativeButtonTitleId,
                                                   DialogInterface.OnClickListener positiveClickListener,DialogInterface.OnClickListener negativeClickListener) {
        OSAlertMessageDialog fragment = new OSAlertMessageDialog();
        fragment.setTitleId(titleId);
        fragment.setMessageId(messageId);
        fragment.setPositiveButtonTitleId(positiveButtonTitleId);
        fragment.setNegativeButtonTitleId(negativeButtonTitleId);
        fragment.setPositiveButtonClickListener(positiveClickListener);
        fragment.setNegativeButtonClickListener(negativeClickListener);
        return fragment;
    }

    public OSAlertMessageDialog() {
        // Required empty public constructor
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }

    public void setPositiveButtonTitleId(int positiveButtonTitleId) {
        this.positiveButtonTitleId = positiveButtonTitleId;
    }

    public void setNegativeButtonTitleId(int negativeButtonTitleId) {
        this.negativeButtonTitleId = negativeButtonTitleId;
    }

    public void setPositiveButtonClickListener(DialogInterface.OnClickListener positiveButtonClickListener) {
        this.positiveButtonClickListener = positiveButtonClickListener;
    }

    public void setNegativeButtonClickListener(DialogInterface.OnClickListener negativeButtonClickListener) {
        this.negativeButtonClickListener = negativeButtonClickListener;
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
        builder.setView(inflater.inflate(R.layout.ect_message_alertview, null))
                // Add action buttons
                .setPositiveButton(positiveButtonTitleId, positiveButtonClickListener)
                .setNegativeButton(negativeButtonTitleId, negativeButtonClickListener);

        Dialog dialog = builder.create();

        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Button pButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_POSITIVE);
        Button nButton = ((AlertDialog) getDialog()).getButton(DialogInterface.BUTTON_NEGATIVE);

        if (pButton != null)
            pButton.setTextColor(getResources().getColor(outSystems_red));

        if (nButton != null)
            nButton.setTextColor(getResources().getColor(outSystems_red));


        TextView titleView = (TextView) ((AlertDialog) getDialog()).findViewById(R.id.alertMessageTitle);
        titleView.setText(this.titleId);

        TextView messageView = (TextView) ((AlertDialog) getDialog()).findViewById(R.id.alertMessageText);
        messageView.setText(this.messageId);


        getDialog().setCanceledOnTouchOutside(false);

    }


}

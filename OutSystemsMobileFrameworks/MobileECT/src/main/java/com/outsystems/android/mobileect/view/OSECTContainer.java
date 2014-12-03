package com.outsystems.android.mobileect.view;


import android.app.Activity;
import android.app.Fragment;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.outsystems.android.mobileect.R;
import com.outsystems.android.mobileect.interfaces.OSECTAudioRecorderListener;
import com.outsystems.android.mobileect.interfaces.OSECTContainerListener;

import java.io.File;
import java.io.IOException;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OSECTContainer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OSECTContainer extends Fragment implements OSECTAudioRecorderListener {

    public static final int ECT_STATUS_SENDING_MESSAGE = 0;
    public static final int ECT_STATUS_FAILED_MESSAGE = 1;

    private Bitmap screenCapture;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioFile;
    private boolean skipHelper;

    public boolean hasAudioComments() {
        return hasAudioComments;
    }

    private boolean hasAudioComments = false;


    OSECTContainerListener mCallback;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OSECTContainer.
     */
    public static OSECTContainer newInstance(Bitmap screenCapture, boolean skipHelper) {
        OSECTContainer fragment = new OSECTContainer();
        fragment.setScreenCapture(screenCapture);
        fragment.setSkipHelper(skipHelper);
        return fragment;
    }

    public OSECTContainer() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View ectContainerView = inflater.inflate(R.layout.ect_container_view, container, false);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(container.getContext(), R.anim.fade_in);

        this.configToolbarView(ectContainerView);

        this.configScreenCaptureView(ectContainerView);

        this.configHelperView(ectContainerView);

        this.configStatusView(ectContainerView);

        View ectToolbar = ectContainerView.findViewById(R.id.ectToolbarInclude);
        ectToolbar.startAnimation(fadeInAnimation);

        if(skipHelper){

            View helperGroup = ectContainerView.findViewById(R.id.ectHelperGroup);
            helperGroup.setBackgroundResource(android.R.color.transparent);
            helperGroup.setVisibility(View.GONE);

            View ectScreenCapture = ectContainerView.findViewById(R.id.ectScreenCapture);
            ectScreenCapture.setVisibility(View.VISIBLE);
        }
        else{
            View helperGroup = ectContainerView.findViewById(R.id.ectHelperGroup);
            helperGroup.startAnimation(fadeInAnimation);
        }

        this.screenCapture = null;

        return ectContainerView;
    }

    private void configToolbarView(View container) {

        EditText feedbackMessage = (EditText) container.findViewById(R.id.ectFeedbackMessage);
        feedbackMessage.setOnFocusChangeListener(onFocusChangeFeedbackMessage);
        feedbackMessage.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                   showSendButton(s.length() > 0);
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        ImageButton closeButton = (ImageButton)container.findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(onClickListenerCloseECT);

        Button sendButton = (Button)container.findViewById(R.id.buttonSend);
        sendButton.setOnClickListener(onClickListenerSendFeedback);

        ImageButton recordButton = (ImageButton)container.findViewById(R.id.buttonRecordAudio);
        recordButton.setOnClickListener(onClickListenerRecordAudio);

        ImageButton playButton = (ImageButton)container.findViewById(R.id.buttonPlayAudio);
        playButton.setOnClickListener(onClickListenerPlayAudio);

        ImageButton stopButton = (ImageButton)container.findViewById(R.id.buttonStopAudio);
        stopButton.setOnClickListener(onClickListenerStopPlay);

    }

    private void configScreenCaptureView(View container) {

        OSCanvasView screenCaptureView = (OSCanvasView)container.findViewById(R.id.ectScreenCapture);
        screenCaptureView.setBackgroundImage(this.screenCapture);
        screenCaptureView.setVisibility(View.GONE);

        View ectScreenContainer = container.findViewById(R.id.ectScreenContainer);
        ViewGroup.LayoutParams ectScreenContainerLayoutParams = ectScreenContainer.getLayoutParams();
        ectScreenContainerLayoutParams.height = this.screenCapture.getHeight();
        ectScreenContainerLayoutParams.width = this.screenCapture.getWidth();
    }

    private void configHelperView(View container){

        ImageView helperView = (ImageView)container.findViewById(R.id.ectHelperView);
        helperView.setOnClickListener(this.onClickListenerHelperImage);
        this.calculateHelperImage(helperView);

        ViewGroup.LayoutParams helperLayoutParams = helperView.getLayoutParams();
        helperLayoutParams.height = this.screenCapture.getHeight();
        helperLayoutParams.width = this.screenCapture.getWidth();

    }

    private void calculateHelperImage(ImageView helperView){

        int screenLayout = getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                if(landscape){
                    helperView.setBackgroundResource(R.drawable.ect_sketch_small_landscape);
                }
                else{
                    helperView.setBackgroundResource(R.drawable.ect_sketch_small_portrait);
                }
                break;
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                if(landscape){
                    helperView.setBackgroundResource(R.drawable.ect_sketch_normal_landscape);
                }
                else{
                    helperView.setBackgroundResource(R.drawable.ect_sketch_normal_portrait);
                }
                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                if(landscape){
                    helperView.setBackgroundResource(R.drawable.ect_sketch_large_landscape);
                }
                else{
                    helperView.setBackgroundResource(R.drawable.ect_sketch_large_portrait);
                }
                break;
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                if(landscape){
                    helperView.setBackgroundResource(R.drawable.ect_sketch_extra_landscape);
                }
                else{
                    helperView.setBackgroundResource(R.drawable.ect_sketch_extra_portrait);
                }
                break;
            default:
                // if undefined use the normal screen configuration
                if(landscape){
                    helperView.setBackgroundResource(R.drawable.ect_sketch_normal_landscape);
                }
                else{
                    helperView.setBackgroundResource(R.drawable.ect_sketch_normal_portrait);
                }
                break;
        }
    }

    private void configStatusView(View container) {
        View ectStatusView = container.findViewById(R.id.ectStatusInclude);
        ectStatusView.setVisibility(View.GONE);

        ImageButton closeButton = (ImageButton)container.findViewById(R.id.ectStatusCloseButton);
        closeButton.setOnClickListener(onClickListenerCloseStatus);

        Button sendButton = (Button)container.findViewById(R.id.ectStatusRetryButton);
        sendButton.setOnClickListener(onClickListenerSendFeedback);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OSECTContainerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnECTContainerClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("OSECTContainer","onDestroy");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v("OSECTContainer","onDestroyView");
        if(this.screenCapture != null) {
            this.screenCapture = null;
            Log.v("OSECTContainer","onDestroyView::screenCapture=null");
        }

        OSCanvasView screenCaptureView = (OSCanvasView)getView().findViewById(R.id.ectScreenCapture);
        if(screenCaptureView != null){
            screenCaptureView.getBackgroundImage().recycle();
            screenCaptureView.setBackgroundImage(null);
            Log.v("OSECTContainer", "onDestroyView::screenCaptureView.setBackgroundImage(null)");
        }

    }

    public boolean isSkipHelper() {
        return skipHelper;
    }

    public void setSkipHelper(boolean skipHelper) {
        this.skipHelper = skipHelper;
    }

    public void setScreenCapture(Bitmap screenCapture) {
        this.screenCapture = screenCapture;
    }


    public void hideECTView(){

        View ectToolbar = getView().findViewById(R.id.ectToolbarInclude);
        View statusToolbar = getView().findViewById(R.id.ectStatusInclude);

        View currentToolbar = ectToolbar;

        if(ectToolbar.getVisibility() == View.GONE && statusToolbar.getVisibility() == View.VISIBLE)
            currentToolbar = statusToolbar;


        Animation slideOutAnimation = AnimationUtils.loadAnimation(getView().getContext(), R.anim.slide_out_bottom);

        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mCallback.onCloseECTClickListener();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });

        currentToolbar.startAnimation(slideOutAnimation);

        this.hideKeyboard();
    }

    private void hideKeyboard(){
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }


    /**
     *  Helper View
     */

    public void hideHelperView(){
        View helperGroup = getView().findViewById(R.id.ectHelperGroup);
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(helperGroup.getContext(), R.anim.fade_out);

        View screenCaptureView = getView().findViewById(R.id.ectScreenCapture);
        screenCaptureView.setVisibility(View.VISIBLE);

        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                View helperGroup = getView().findViewById(R.id.ectHelperGroup);
                helperGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });
        helperGroup.startAnimation(fadeOutAnimation);

        // Close ECT Helper
        mCallback.onCloseECTHelperClickListener();

    }

    /**
     * Status View
     */

    private void setStatusMessage(int message){
        TextView ectStatusMessage = (TextView)getView().findViewById(R.id.ectStatusMessage);
        View closeButton = getView().findViewById(R.id.ectStatusCloseButton);
        View progressBar = getView().findViewById(R.id.ectStatusIndicator);
        View retryButton = getView().findViewById(R.id.ectStatusRetryButton);

        switch (message){
            case ECT_STATUS_SENDING_MESSAGE:
                ectStatusMessage.setText(R.string.status_sending_message);
                closeButton.setVisibility(View.GONE);
                retryButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

                break;
            case ECT_STATUS_FAILED_MESSAGE:
                ectStatusMessage.setText(R.string.status_failed_message);
                closeButton.setVisibility(View.VISIBLE);
                retryButton.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

                Animation shake = AnimationUtils.loadAnimation(ectStatusMessage.getContext(), R.anim.shake_it);
                ectStatusMessage.setAnimation(shake);

                break;
            default:
                break;
        }

    }

    public void showStatusView(boolean show, int message){
        View ectToolbar = getView().findViewById(R.id.ectToolbarInclude);
        View ectStatus =  getView().findViewById(R.id.ectStatusInclude);
        OSCanvasView canvasView = (OSCanvasView)getView().findViewById(R.id.ectScreenCapture);
        canvasView.setCanvasLocked(show);


        Animation fadeIn = AnimationUtils.loadAnimation(ectToolbar.getContext(), R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(ectToolbar.getContext(), R.anim.fade_out);


        if(show){
            this.setStatusMessage(message);

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    View ectToolbar = getView().findViewById(R.id.ectToolbarInclude);
                    ectToolbar.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    View ectStatus =  getView().findViewById(R.id.ectStatusInclude);
                    ectStatus.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if(ectStatus.getVisibility() == View.GONE)
                ectStatus.startAnimation(fadeIn);

            if(ectToolbar.getVisibility() == View.VISIBLE)
                ectToolbar.startAnimation(fadeOut);

        }
        else{

            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    View ectStatus = getView().findViewById(R.id.ectStatusInclude);
                    ectStatus.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            fadeIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    View ectToolbar =  getView().findViewById(R.id.ectToolbarInclude);
                    ectToolbar.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });


            if(ectToolbar.getVisibility() == View.GONE)
                ectToolbar.startAnimation(fadeIn);

            if(ectStatus.getVisibility() == View.VISIBLE)
                ectStatus.startAnimation(fadeOut);

        }
    }


    /**
     * Listeners
     */


    private View.OnFocusChangeListener onFocusChangeFeedbackMessage =new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            View helperGroup = getView().findViewById(R.id.ectHelperGroup);
            if(helperGroup.getVisibility() == View.VISIBLE)
                hideHelperView();
        }
    };


    private View.OnClickListener onClickListenerCloseStatus = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            showStatusView(false,-1);
        }
    };


    private View.OnClickListener onClickListenerCloseECT = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            View helperGroup = getView().findViewById(R.id.ectHelperGroup);
            if(helperGroup.getVisibility() == View.VISIBLE)
                hideHelperView();
            else
                hideECTView();

        }
    };


    private View.OnClickListener onClickListenerHelperImage = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideHelperView();
        }
    };

    private View.OnClickListener onClickListenerSendFeedback = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideKeyboard();
            showStatusView(true, ECT_STATUS_SENDING_MESSAGE);
            mCallback.onSendFeedbackClickListener();
        }
    };

    private View.OnClickListener onClickListenerRecordAudio  = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            initAudioRecorder();

            OSAudioRecorderDialog audioRecorderDialog = OSAudioRecorderDialog.newInstance(OSECTContainer.this);
            audioRecorderDialog.show(getFragmentManager(),"AudioRecorder");

            startRecording();
        }
    };


    private View.OnClickListener onClickListenerPlayAudio = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playRecordedAudio();
            showPlayOrStopButton(false);
        }
    };

    private View.OnClickListener onClickListenerStopPlay = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            stopRecordedAudio();
            showPlayOrStopButton(true);
        }
    };

    /**
     * Get Feedback Content
     */

    public String getFeedbackMessage(){
        String result = null;
        EditText editText = (EditText)getView().findViewById(R.id.ectFeedbackMessage);
        if(editText != null)
            result = editText.getText().toString();

        return result;
    }

    public Bitmap getScreenCapture(){
        return this.screenCapture;
    }


    public File getAudioComments(){
        return new File(audioFile);
    }


    /**
     * ECT Audio Recorder
     */

    private void initAudioRecorder(){
        audioFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ECTComment.3gpp";

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audioFile);

    }

    private void startRecording(){
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }

    }

    private void stopRecording(){
        try {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder  = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private void playRecordedAudio(){
        try{
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioFile);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopRecordedAudio(){
        try {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void showAudioButtons(boolean show){

        View playButton = getView().findViewById(R.id.buttonPlayAudio);
        if(playButton != null){
            playButton.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        View stopButton = getView().findViewById(R.id.buttonStopAudio);
        if(stopButton != null){
            stopButton.setVisibility(View.GONE);
        }

        View feedbackMessage = getView().findViewById(R.id.ectFeedbackMessage);
        if(feedbackMessage != null){
            feedbackMessage.setVisibility(!show ? View.VISIBLE : View.GONE);
        }
    }

    private void showSendButton(boolean show){
        View sendButton = getView().findViewById(R.id.buttonSend);
        if(sendButton != null){
            sendButton.setVisibility(show ? View.VISIBLE : View.GONE);
        }

        View microphoneButton = getView().findViewById(R.id.buttonRecordAudio);
        if(microphoneButton != null){
            microphoneButton.setVisibility(!show ? View.VISIBLE : View.GONE);
        }


    }


    private void showPlayOrStopButton(final boolean play){


        Animation fadeOut = AnimationUtils.loadAnimation(getView().getContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                if(play){
                    View stopButton = getView().findViewById(R.id.buttonStopAudio);
                    stopButton.setVisibility(View.GONE);
                }
                else{
                    View playButton = getView().findViewById(R.id.buttonPlayAudio);
                    playButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });

        Animation fadeIn = AnimationUtils.loadAnimation(getView().getContext(), R.anim.fade_in);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {

                if(play){
                    View playButton = getView().findViewById(R.id.buttonPlayAudio);
                    playButton.setVisibility(View.VISIBLE);
                }
                else{
                    View stopButton = getView().findViewById(R.id.buttonStopAudio);
                    stopButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });

        View playButton = getView().findViewById(R.id.buttonPlayAudio);
        View stopButton = getView().findViewById(R.id.buttonStopAudio);

        if(play){
            stopButton.startAnimation(fadeOut);
            playButton.startAnimation(fadeIn);
        }
        else{
            playButton.startAnimation(fadeOut);
            stopButton.startAnimation(fadeIn);
        }

    }

    public void releaseMedia(){

        if(this.mediaRecorder != null)
            this.mediaRecorder.release();

        if(this.mediaPlayer != null)
            this.mediaPlayer.release();

        if(this.audioFile != null){
            File file = new File(this.audioFile);
            if(file != null)
                file.delete();
        }
    }


    @Override
    public void onCancelAudioRecorder() {
        stopRecording();
        hasAudioComments = false;
        showAudioButtons(false);
        showSendButton(false);
    }

    @Override
    public void onStopAudioRecorder() {
        stopRecording();
        hasAudioComments = true;
        showAudioButtons(true);
        showSendButton(true);
    }



}

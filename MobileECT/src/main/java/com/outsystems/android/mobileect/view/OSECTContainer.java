package com.outsystems.android.mobileect.view;


import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import com.outsystems.android.mobileect.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OSECTContainer#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OSECTContainer extends Fragment {

    private Bitmap screenCapture;

    OnECTContainerClickListener mCallback;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OSECTContainer.
     */
    public static OSECTContainer newInstance(Bitmap screenCapture) {
        OSECTContainer fragment = new OSECTContainer();
        fragment.setScreenCapture(screenCapture);
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

        Button closeButton = (Button)ectContainerView.findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(onClickListenerCloseECT);

        OSCanvasView screenCaptureView = (OSCanvasView)ectContainerView.findViewById(R.id.ectScreenCapture);
        screenCaptureView.setBackgroundImage(this.screenCapture);
        screenCaptureView.setVisibility(View.GONE);

        ImageView helperView = (ImageView)ectContainerView.findViewById(R.id.ectHelperView);
        helperView.setBackgroundResource(R.drawable.ect_instructions_portrait);
        helperView.setOnClickListener(this.onClickListenerHelperImage);

        ViewGroup.LayoutParams helperLayoutParams = helperView.getLayoutParams();
        helperLayoutParams.height = this.screenCapture.getHeight();
        helperLayoutParams.width = this.screenCapture.getWidth();

        Animation fadeInAnimation = AnimationUtils.loadAnimation(container.getContext(), R.anim.fade_in);
        ectContainerView.startAnimation(fadeInAnimation);


        View ectScreenContainer = ectContainerView.findViewById(R.id.ectScreenContainer);
        ViewGroup.LayoutParams ectScreenContainerLayoutParams = ectScreenContainer.getLayoutParams();
        ectScreenContainerLayoutParams.height = this.screenCapture.getHeight();
        ectScreenContainerLayoutParams.width = this.screenCapture.getWidth();

        return ectContainerView;
    }



    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnECTContainerClickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnECTContainerClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }


    public Bitmap getScreenCapture() {
        return screenCapture;
    }

    public void setScreenCapture(Bitmap screenCapture) {
        this.screenCapture = screenCapture;
    }

    public void hideHelperView(){
        View helperGroup = getView().findViewById(R.id.ectHelperGroup);
        Animation fadeOutAnimation = AnimationUtils.loadAnimation(helperGroup.getContext(), R.anim.fade_out);

        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                View screenCaptureView = getView().findViewById(R.id.ectScreenCapture);
                screenCaptureView.setVisibility(View.VISIBLE);
                View helperGroup = getView().findViewById(R.id.ectHelperGroup);
                helperGroup.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });
        helperGroup.startAnimation(fadeOutAnimation);


    }

    private void hideECTView(){
        View ectScreenCapture = getView().findViewById(R.id.ectScreenCapture);
        Animation fadeOut = AnimationUtils.loadAnimation(getView().getContext(), R.anim.fade_out);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                View ectScreenCapture = getView().findViewById(R.id.ectScreenCapture);
                ectScreenCapture.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });


        View ectToolbar = getView().findViewById(R.id.ectToolbarInclude);
        Animation slideOutAnimation = AnimationUtils.loadAnimation(getView().getContext(), R.anim.slide_out_bottom);

        slideOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                mCallback.onCloseECTListener();
            }

            @Override
            public void onAnimationRepeat(Animation animation) { }

            @Override
            public void onAnimationStart(Animation animation) { }
        });

        ectScreenCapture.startAnimation(fadeOut);
        ectToolbar.startAnimation(slideOutAnimation);
    }

    /**
     * Click Listeners
     */

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


    /**
     *  ECTContainer Click Listener Interface
     */

    public interface OnECTContainerClickListener {
        public void onCloseECTListener();
    }

}

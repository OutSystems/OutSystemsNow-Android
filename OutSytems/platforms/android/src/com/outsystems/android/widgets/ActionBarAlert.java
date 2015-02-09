package com.outsystems.android.widgets;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ActionBarAlert{
    private FrameLayout mLayout;
    private TextView mTextView;
    private AnimatorSet mAnimSlideDown;
    private AnimatorSet mAnimSlideUp;
    private boolean mIsVisible = false;
    private Runnable mHideThread;

    public ActionBarAlert(Activity act){
        //...........................................
        Window window = act.getWindow(); //Container is a frame layout
        View v = window.getDecorView(); //Get Root View

        int resId = act.getResources().getIdentifier("action_bar_container","id","android");
        FrameLayout mActionBarRoot = (FrameLayout)v.findViewById(resId);

        //...........................................
        //Get the size of of tha action bar in another way since getHeight is zero until after Activity.onResume is called
        TypedValue tv = new TypedValue();
        int height = 56;
        try{
            if(act.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)){
                height = TypedValue.complexToDimensionPixelSize(tv.data, act.getResources().getDisplayMetrics());
            }//if
        }catch(Exception e){
            System.out.println("Error trying to get Action Bar Height " + e.getMessage());
        }//try

        //...........................................
        //Create a new layout that will live over the actionbar.
        mLayout = new FrameLayout(act);
        mLayout.setBackgroundColor(Color.parseColor("#CC2200"));
        mLayout.setVisibility(View.INVISIBLE);

        //Stop events from bubbling down to action bar.
        mLayout.setOnTouchListener(new View.OnTouchListener(){public boolean onTouch(View v,MotionEvent event){return true;}});

        //set the size of the message layout to the same size as the action bar.
        FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,height,Gravity.LEFT | Gravity.TOP);

        //...........................................
        //Create TextView that will display the alert message
        mTextView = new TextView(act);
        mTextView.setTextColor(Color.parseColor("#FFFFFF"));
        mTextView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        mLayout.addView(mTextView);

        //...........................................
        mAnimSlideDown = new AnimatorSet();
        ObjectAnimator alphaAnimD = ObjectAnimator.ofFloat(mLayout, "alpha", 0f, 1f);
        ObjectAnimator transAnimD = ObjectAnimator.ofFloat(mLayout,"translationY",-height,0f);
        transAnimD.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimSlideDown.playTogether(transAnimD, alphaAnimD);
        mAnimSlideDown.setDuration(250);

        mAnimSlideUp = new AnimatorSet();
        ObjectAnimator alphaAnimU = ObjectAnimator.ofFloat(mLayout, "alpha", 1f, 0f);
        ObjectAnimator transAnimU = ObjectAnimator.ofFloat(mLayout,"translationY",0f,-height);
        alphaAnimU.setInterpolator(new AccelerateDecelerateInterpolator());
        mAnimSlideUp.playTogether(transAnimU, alphaAnimU);
        mAnimSlideUp.setDuration(250);

        //...........................................
        //Reusable thread to hide the message after n amount of seconds.
        mHideThread = new Runnable(){ @Override public void run(){ hide(); }};

        //...........................................
        //Inject new views over the action bar.
        mActionBarRoot.addView(mLayout,mLayoutParams);
    }

    public void show(String msg){ show(msg,0); }
    public void show(String msg,int secToHide){
        mTextView.setText(msg);

        if(!mIsVisible){
            mLayout.setVisibility(View.VISIBLE);
            mAnimSlideDown.start();
            mIsVisible = true;
        }

        if(secToHide > 0) mLayout.postDelayed(mHideThread,secToHide * 1000);
    }

    public void hide(){
        if(!mIsVisible) return;

        mAnimSlideUp.start();
        mIsVisible = false;
    }

    public boolean isVisible(){ return mIsVisible; }
}
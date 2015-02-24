/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.TextView;

import com.outsystems.android.adapters.ApplicationsAdapter;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.DeepLinkController;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;

/**
 * Class description.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class ApplicationsActivity extends BaseActivity {

    // Constants
    public static String KEY_CONTENT_APPLICATIONS = "key_applications";
    public static String KEY_TITLE_ACTION_BAR = "key_title_action_bar";

    // Properties
    private View mLoadingView;
    private GridView gridView;
    private boolean mContentLoaded;

    private int mShortAnimationDuration;

    /** The on item click listener. */
    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), WebApplicationActivity.class);
            Application application = (Application) parent.getAdapter().getItem(position);
            if (application != null) {
                intent.putExtra(WebApplicationActivity.KEY_APPLICATION, application);

                // Check if there's only one app
                Bundle bundle = getIntent().getExtras();
                if (bundle != null) {
                    @SuppressWarnings("unchecked")
                    ArrayList<Application> applications = (ArrayList<Application>) bundle
                            .getSerializable(KEY_CONTENT_APPLICATIONS);

                    intent.putExtra(WebApplicationActivity.KEY_SINGLE_APPLICATION,applications != null && applications.size() == 1);
                }


            }
            startActivity(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applications);

        setupActionBar();

        mLoadingView = findViewById(R.id.loading_spinner);
        gridView = (GridView) findViewById(R.id.grid_view_applications);

        mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            @SuppressWarnings("unchecked")
            ArrayList<Application> applications = (ArrayList<Application>) bundle
                    .getSerializable(KEY_CONTENT_APPLICATIONS);
            String titleActionBar = bundle.getString(KEY_TITLE_ACTION_BAR);
            if (titleActionBar != null) {
                setTitleActionBar(titleActionBar);
            }
            if (applications == null)
                applications = new ArrayList<Application>();
            loadContentInGridview(applications);

        } else {
            loadApplications();
        }
        
        // Check if deep link has valid settings                
        if(DeepLinkController.getInstance().hasValidSettings()){
        	
        	DeepLinkController.getInstance().resolveOperation(this, null);

        }
        
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.FragmentActivity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
        if (HubManagerHelper.getInstance().getApplicationHosted() == null) {
            ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
            app.registerDefaultHubApplication();
        }
    }

    private void loadApplications() {
        gridView.setVisibility(View.GONE);
        
    	final DisplayMetrics displaymetrics = new DisplayMetrics();		
		getWindowManager().getDefaultDisplay().getRealMetrics(displaymetrics);
        
        WebServicesClient.getInstance().getApplications(getApplicationContext(), HubManagerHelper.getInstance().getApplicationHosted(), 
        		(int)(displaymetrics.widthPixels / displaymetrics.density), (int)(displaymetrics.heightPixels / displaymetrics.density),
                new WSRequestHandler() {

                    @Override
                    public void requestFinish(Object result, boolean error, int statusCode) {
                        if (!error) {
                        	
                        	ApplicationOutsystems app = (ApplicationOutsystems) getApplication();
                            app.setDemoApplications(true);
                            if(app.demoApplications) {
                            	// Using authentication in the web view
                                WebView webView = new WebView(getApplicationContext());
                                String url = String.format(WebServicesClient.BASE_URL,
                                        HubManagerHelper.getInstance().getApplicationHosted()).concat(
                                        "applications" + WebServicesClient.getApplicationServer());
                                url = url.replace("https", "http");
                                webView.loadUrl(url);
                            }
                        	
                            @SuppressWarnings("unchecked")
                            ArrayList<Application> applications = (ArrayList<Application>) result;
                            if (applications != null && applications.size() > 0) {
                                loadContentInGridview(applications);
                            }
                        }
                    }
                });
    }

    /**
     * Load content in gridview.
     * 
     * @param applications the applications
     */
    private void loadContentInGridview(ArrayList<Application> applications) {
    	
    	if(applications != null && !applications.isEmpty()) {    	
	        ApplicationsAdapter applicationsAdapter = new ApplicationsAdapter(getApplicationContext(), applications);
	        gridView.setAdapter(applicationsAdapter);
	        mContentLoaded = !mContentLoaded;
	        showContentOrLoadingIndicator(mContentLoaded);
	        gridView.setOnItemClickListener(onItemClickListener);
    	}
    	else {
    		TextView noAppsLabel = (TextView) findViewById(R.id.text_view_label_no_applications);
    		noAppsLabel.setVisibility(View.VISIBLE);
    		showContentOrLoadingIndicator(true);
    	}
    }

    /**
     * Cross-fades between {@link #gridView} and {@link #mLoadingView}.
     */
    private void showContentOrLoadingIndicator(boolean contentLoaded) {
        // Decide which view to hide and which to show.
        final View showView = contentLoaded ? gridView : mLoadingView;
        final View hideView = contentLoaded ? mLoadingView : gridView;

        // Set the "show" view to 0% opacity but visible, so that it is visible
        // (but fully transparent) during the animation.
        showView.setAlpha(0f);
        showView.setVisibility(View.VISIBLE);

        // Animate the "show" view to 100% opacity, and clear any animation listener set on
        // the view. Remember that listeners are not limited to the specific animation
        // describes in the chained method calls. Listeners are set on the
        // ViewPropertyAnimator object for the view, which persists across several
        // animations.
        showView.animate().alpha(1f).setDuration(mShortAnimationDuration).setListener(null);

        // Animate the "hide" view to 0% opacity. After the animation ends, set its visibility
        // to GONE as an optimization step (it won't participate in layout passes, etc.)
        hideView.animate().alpha(0f).setDuration(mShortAnimationDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                hideView.setVisibility(View.GONE);
            }
        });
    }
}

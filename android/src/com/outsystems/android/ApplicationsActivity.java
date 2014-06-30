package com.outsystems.android;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.outsystems.android.adapters.ApplicationsAdapter;
import com.outsystems.android.core.WSRequestHandler;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;

public class ApplicationsActivity extends BaseActivity {

    public static String KEY_CONTENT_APPLICATIONS = "key_applications";
    public static String KEY_TITLE_ACTION_BAR = "key_title_action_bar";

    private View mLoadingView;
    private GridView gridView;
    private boolean mContentLoaded;

    private int mShortAnimationDuration;

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getApplicationContext(), Outsystems.class);
            Application application = (Application) parent.getAdapter().getItem(position);
            if (application != null) {
                intent.putExtra(Outsystems.KEY_APPLICATION, application);
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
    }

    private void loadApplications() {
        gridView.setVisibility(View.GONE);
        WebServicesClient.getInstance().getApplications(HubManagerHelper.getInstance().getApplicationHosted(),
                new WSRequestHandler() {

                    @Override
                    public void requestFinish(Object result, boolean error, int statusCode) {
                        if (!error) {
                            @SuppressWarnings("unchecked")
                            ArrayList<Application> applications = (ArrayList<Application>) result;
                            if (applications != null && applications.size() > 0) {
                                loadContentInGridview(applications);
                            }
                        }
                    }

                    @Override
                    public void requestError(int statusCode) {

                    }
                });
    }

    private void loadContentInGridview(ArrayList<Application> applications) {
        ApplicationsAdapter applicationsAdapter = new ApplicationsAdapter(getApplicationContext(), applications);
        gridView.setAdapter(applicationsAdapter);
        mContentLoaded = !mContentLoaded;
        showContentOrLoadingIndicator(mContentLoaded);
        gridView.setOnItemClickListener(onItemClickListener);
    }

    /**
     * Cross-fades between {@link #mContentView} and {@link #mLoadingView}.
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

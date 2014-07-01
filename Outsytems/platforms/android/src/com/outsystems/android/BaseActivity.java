package com.outsystems.android;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.outsystems.android.widgets.TypefaceSpan;

public class BaseActivity extends ActionBarActivity {

    private OnClickListener onClickListenerHyperLink = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String link = getString(R.string.label_about_link);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + link));
            startActivity(browserIntent);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        case android.R.id.home:
            Intent upIntent = NavUtils.getParentActivityIntent(this);
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setTitleActionBar(String title) {
        // Set text with Custom Font of app
        SpannableString titleActionBar = new SpannableString(title);
        titleActionBar.setSpan(new TypefaceSpan(this, "MyriadPro-Regular.ttf"), 0, titleActionBar.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setTitle(titleActionBar);
    }

    protected void setupActionBar() {
        ActionBar ab = getSupportActionBar();
        ab.setDisplayShowCustomEnabled(false);
        ab.setIcon(null);

        setTitleActionBar(getResources().getString(R.string.label_back));

        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setLogo(getResources().getDrawable(R.drawable.icon_chevron_back));
    }

    protected void aboutEvents() {
        TextView textViewLink = (TextView) findViewById(R.id.text_view_about_link);

        // Code to add underline in the text
        /*
         * textViewLink.setMovementMethod(LinkMovementMethod.getInstance()); SpannableString spanString = new
         * SpannableString(getString(R.string.label_about_link)); spanString.setSpan(new UnderlineSpan(), 0,
         * spanString.length(), 0); textViewLink.setText(spanString);
         */

        textViewLink.setOnClickListener(onClickListenerHyperLink);
    }

    protected void showLoading(View buttonClick) {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
        buttonClick.setVisibility(View.INVISIBLE);
        progressbar.setVisibility(View.VISIBLE);
    }

    protected void stopLoading(View buttonClick) {
        ProgressBar progressbar = (ProgressBar) findViewById(R.id.progress_bar);
        buttonClick.setVisibility(View.VISIBLE);
        progressbar.setVisibility(View.INVISIBLE);
    }

    protected void showError(View viewError) {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        viewError.startAnimation(shake);
    }
}

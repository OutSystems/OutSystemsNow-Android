/*
 * OutSystems Project
 * 
 * Copyright (C) 2014 OutSystems.
 * 
 * This software is proprietary.
 */
package com.outsystems.android.adapters;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.outsystems.android.R;
import com.outsystems.android.core.WebServicesClient;
import com.outsystems.android.helpers.HubManagerHelper;
import com.outsystems.android.model.Application;

/**
 * Class Applications Adapter.
 * 
 * @author <a href="mailto:vmfo@xpand-it.com">vmfo</a>
 * @version $Revision: 666 $
 * 
 */
public class ApplicationsAdapter extends ArrayAdapter<Application> {

    private final Context context;
    private final List<Application> dataArray;

    DisplayImageOptions options;
    ImageLoader imageLoader = ImageLoader.getInstance();

    /**
     * Instantiates a new applications adapter.
     * 
     * @param context the context
     * @param applications the applications
     */
    public ApplicationsAdapter(Context context, List<Application> applications) {
        super(context, R.layout.applications_grid_item, applications);
        this.context = context;
        this.dataArray = applications;

        // Setting Options to lib Download Images
        options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.no_app_image)
                .showImageForEmptyUri(R.drawable.no_app_image).showImageOnFail(R.drawable.no_app_image)
                .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).defaultDisplayImageOptions(
                options).build();
        ImageLoader.getInstance().init(config);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ViewHolder viewHolder;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.applications_grid_item, null);

            // configure view holder
            viewHolder = new ViewHolder();
            viewHolder.textViewApplication = (TextView) rowView.findViewById(R.id.text_view_application);
            viewHolder.textViewDescription = (TextView) rowView.findViewById(R.id.text_view_description);
            viewHolder.imageViewApplication = (ImageView) rowView.findViewById(R.id.image_view_application);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) rowView.getTag();
        }

        Application application = dataArray.get(position);

        // Set data in the views of item gridview
        viewHolder.textViewApplication.setText(application.getName());
        if (viewHolder.textViewDescription != null) {
            viewHolder.textViewDescription.setText(application.getDescription() != "" ?  application.getDescription() : "(no description)");
        }

        if (application.getImageId() == 0) {
            imageLoader.displayImage("", viewHolder.imageViewApplication, options);
        } else {
            String url = WebServicesClient.getAbsoluteUrlForImage(
                    HubManagerHelper.getInstance().getApplicationHosted(), application.getImageId());
            imageLoader.displayImage(url, viewHolder.imageViewApplication, options);
        }
        return rowView;
    }

    /**
     * The Class ViewHolder.
     */
    static class ViewHolder {
        TextView textViewApplication;
        TextView textViewDescription;
        ImageView imageViewApplication;
    }
}

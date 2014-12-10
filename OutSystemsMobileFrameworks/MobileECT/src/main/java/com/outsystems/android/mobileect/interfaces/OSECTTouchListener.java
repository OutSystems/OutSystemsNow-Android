package com.outsystems.android.mobileect.interfaces;

import com.outsystems.android.mobileect.view.OSCanvasView;

/**
 * Created by lrs on 05-12-2014.
 */
public interface OSECTTouchListener {

    public void onTouchBeganNearROI(OSCanvasView.Point point);
    public void onTouchMovedNearROI(OSCanvasView.Point point);
    public void onTouchEndNearROI(OSCanvasView.Point point);
}

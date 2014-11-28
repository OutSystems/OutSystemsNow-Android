package com.outsystems.android.mobileect.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by lrs on 19-11-2014.
 */
public class OSCanvasView extends View implements View.OnTouchListener{

    private static final String TAG = "OSCanvasView";

    private Bitmap backgroundImage;
    private Path path;
    private Point[] points;
    private Paint paint;
    private int counter;

    public boolean isCanvasLocked() {
        return canvasLocked;
    }

    public void setCanvasLocked(boolean canvasLocked) {
        this.canvasLocked = canvasLocked;
    }

    private boolean canvasLocked;


    public OSCanvasView(Context context) {
        super(context);
        init(context);
    }

    public OSCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OSCanvasView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        setFocusable(true);
        setFocusableInTouchMode(true);

        this.setOnTouchListener(this);

        this.paint = new Paint();
        this.paint.setColor(Color.RED);
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Paint.Style.STROKE);
        this.paint.setStrokeWidth(8.0f);

        this.path = new Path();

        this.points = new Point[5];
        this.counter = 0;
        this.canvasLocked = false;
    }

    @Override
    public void onDraw(Canvas canvas) {

        if(this.getBackgroundImage() != null)
            canvas.drawBitmap(getBackgroundImage(),0,0,paint);

        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(canvasLocked)
            return false;

        Point touchPoint = new Point(motionEvent.getX(),motionEvent.getY());
        boolean result;

        switch (motionEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                this.counter = 0;
                this.points[0] = touchPoint;

                result = true;
                break;
            case MotionEvent.ACTION_MOVE:
                this.counter++;
                this.points[counter] = touchPoint;

                if(this.counter == 4){
                    float x = (this.points[2].x + this.points[4].x ) / 2.0f;
                    float y = (this.points[2].y + this.points[4].y ) / 2.0f;

                    this.points[3] = new Point(x,y);
                    this.path.moveTo(this.points[0].x , this.points[0].y );

                    path.cubicTo(this.points[1].x,this.points[1].y,
                                 this.points[2].x,this.points[2].y,
                                 this.points[3].x,this.points[3].y);

                    this.points[0] = this.points[3];
                    this.points[1] = this.points[4];
                    this.counter = 1;

                }

                this.invalidate();

                result = true;
                break;
            case MotionEvent.ACTION_UP:
                this.drawBitmap();
                path.reset();
                this.counter = 0;

                this.invalidate();

                result = true;
                break;

            default:
                // Do nothing
                result = super.onTouchEvent(motionEvent);
                break;
        }

        return result;
    }


    private void drawBitmap(){
        if(this.getBackgroundImage() == null){
            this.setBackgroundImage(Bitmap.createBitmap(this.getMeasuredWidth(), this.getMeasuredHeight(), Bitmap.Config.ARGB_8888));
        }

        Canvas canvas = new Canvas(this.getBackgroundImage());
        canvas.drawBitmap(this.getBackgroundImage(),0,0,this.paint);

        canvas.drawPath(this.path, this.paint);
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(Bitmap backgroundImage) {
        this.backgroundImage = backgroundImage;

        if(this.backgroundImage != null){
            this.setMeasuredDimension(this.backgroundImage.getWidth(), this.backgroundImage.getHeight());
        }
    }


    class Point {
        float x, y;

        public Point(float x, float y){
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return x + ", " + y;
        }
    }

}

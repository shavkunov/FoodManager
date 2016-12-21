package ru.spbau.mit.foodmanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Copypaste from Internet. I just need a view, which plays animated gif
 */

public class GifImageView extends View {

    private InputStream mInputStream;
    private Movie mMovie;
    private int mWidth, mHeight;
    private int realWidth = -1;
    private int realHeight = -1;
    private long mStart;
    private Context mContext;
    private Boolean visible = true;
    private Bitmap __bitmap;
    private Canvas __canvas;
    private Rect __dst;

    public GifImageView(Context context) {
        super(context);
        this.mContext = context;
    }

    public GifImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        if (attrs.getAttributeName(1).equals("background")) {
            int id = Integer.parseInt(attrs.getAttributeValue(1).substring(1));
            setGifImageResource(id);
        }
    }


    private void init() {
        setFocusable(true);
        mMovie = Movie.decodeStream(mInputStream);
        __bitmap = Bitmap.createBitmap(mMovie.width(), mMovie.height(), Bitmap.Config.ARGB_8888);
        __canvas = new Canvas(__bitmap);
        initSize();
    }

    private void initSize() {
        if (realWidth > 0) {
            mWidth = realWidth;
        } else {
            mWidth = mMovie.width();
        }
        if (realHeight > 0) {
            mHeight = realHeight;
        } else {
            mHeight = mMovie.height();
        }
        __dst = new Rect(0, 0, mWidth, mHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mWidth, mHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (visible) {
            canvas.drawColor(Color.TRANSPARENT);
            super.onDraw(canvas);
            long now = SystemClock.uptimeMillis();

            if (mStart == 0) {
                mStart = now;
            }

            if (mMovie != null) {
                int duration = mMovie.duration();
                if (duration == 0) {
                    duration = 1;
                }
                int relTime = (int) ((now - mStart) % duration);
                mMovie.setTime(relTime);
                //Draw in bitmap
                __canvas.drawColor(Color.TRANSPARENT);
                mMovie.draw(__canvas, 0, 0);
                //Draw bitmap on canvas with resize
                canvas.drawBitmap(__bitmap, null, __dst, null);
                this.invalidate();
            }
        }
    }

    public void setIsVisible(Boolean b) {

    }

    public void setGifImageResource(int id) {
        mInputStream = mContext.getResources().openRawResource(id);
        init();
    }

    /**
     * Set Width in pixels
     */
    public void setGifWidth(int w) {
        realWidth = w;
        initSize();
    }

    /**
     * Set Height in pixels
     */
    public void setGifHeight(int h) {
        realHeight = h;
        initSize();
    }
}
package com.galiazat.diplomtest4opencvimplement.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.opencv.BuildConfig;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public class VideoPreviewView extends SurfaceView {

    private final String TAG = VideoPreviewView.class.getSimpleName();

    public VideoPreviewView(Context context) {
        super(context);
    }

    public VideoPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void showMat(Mat mat){
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            float mScale;
            int mFrameHeight = mat.height();
            int mFrameWidth = mat.width();
            Bitmap mCacheBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, mCacheBitmap);
            if ((getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT) && (getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT))
                mScale = Math.min(((float)getHeight())/mFrameHeight, ((float)getWidth())/mFrameWidth);
            else
                mScale = 0;
            if (BuildConfig.DEBUG)
                Log.d(TAG, "mStretch value: " + mScale);

            if (mScale != 0) {
                canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                        new Rect((int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2),
                                (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2),
                                (int)((canvas.getWidth() - mScale*mCacheBitmap.getWidth()) / 2 + mScale*mCacheBitmap.getWidth()),
                                (int)((canvas.getHeight() - mScale*mCacheBitmap.getHeight()) / 2 + mScale*mCacheBitmap.getHeight())), null);
            } else {
                canvas.drawBitmap(mCacheBitmap, new Rect(0,0,mCacheBitmap.getWidth(), mCacheBitmap.getHeight()),
                        new Rect((canvas.getWidth() - mCacheBitmap.getWidth()) / 2,
                                (canvas.getHeight() - mCacheBitmap.getHeight()) / 2,
                                (canvas.getWidth() - mCacheBitmap.getWidth()) / 2 + mCacheBitmap.getWidth(),
                                (canvas.getHeight() - mCacheBitmap.getHeight()) / 2 + mCacheBitmap.getHeight()), null);
            }
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

}

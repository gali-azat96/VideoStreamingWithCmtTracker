package com.galiazat.videoStreamingCmt.custom;

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
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class VideoClientPreviewView extends SurfaceView {

    private final String TAG = VideoClientPreviewView.class.getSimpleName();
    private final List<PreviewListener> listeners = new ArrayList<>();

    public VideoClientPreviewView(Context context) {
        super(context);
    }

    public VideoClientPreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoClientPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoClientPreviewView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void addListener(PreviewListener listener){
        listeners.add(listener);
    }

    public void showFrame(VideoSourcePreviewView.SendingFrame sendingFrame){
        Canvas canvas = getHolder().lockCanvas();
        if (canvas != null) {
            canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);
            float mScale;
            int mFrameHeight = sendingFrame.getFrameHeight();
            int mFrameWidth = sendingFrame.getFrameWidth();
            Mat mat = Imgcodecs.imdecode(new MatOfByte(sendingFrame.getFrame()), Imgcodecs.CV_LOAD_IMAGE_ANYCOLOR);
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
            for (PreviewListener listener : listeners){
                CameraViewFrameImpl cameraViewFrame = new CameraViewFrameImpl(mat);
                Mat newMat = listener.frameReceived(cameraViewFrame);
                if (newMat != mat){
                    mat.release();
                    mat = newMat;
                }
            }
            Bitmap mCacheBitmap = Bitmap.createBitmap(mFrameWidth, mFrameHeight, Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, mCacheBitmap);
            mat.release();
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

    public void clear(){
        listeners.clear();
    }

    public interface PreviewListener{
        Mat frameReceived(CameraBridgeViewBase.CvCameraViewFrame inputFrame);
    }

    private static class CameraViewFrameImpl implements CameraBridgeViewBase.CvCameraViewFrame{

        Mat gray = null;
        Mat rgb;

        CameraViewFrameImpl(Mat rgb) {
            this.rgb = rgb;
        }

        @Override
        public Mat rgba() {
            return rgb;
        }

        @Override
        public Mat gray() {
            if (gray == null){
                gray = new Mat(rgb.size(), CvType.CV_8UC1);
            }
            return gray;
        }
    }

}


    //            Mat mat = new Mat(mFrameHeight + (mFrameHeight/2), mFrameWidth, CvType.CV_8UC1);
//            mat.put(0,0, sendingFrame.getFrame());
//    Mat mRgba;
//            if (previewType == ImageFormat.NV21) {
//                mRgba = new Mat(mFrameHeight, mFrameWidth, CvType.CV_8UC4);
//                Imgproc.cvtColor(mat, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
//            } else if (previewType == ImageFormat.YV12) {
//                mRgba = new Mat(mFrameHeight, mFrameWidth, CvType.CV_8UC3);
//                Imgproc.cvtColor(mat, mRgba, Imgproc.COLOR_YUV2RGB_I420, 4);
//            } else {
//                throw new IllegalStateException("Unsupported ImageFormat");
//            }
//            mat.release();

//            byte[] bytes = sendingFrame.getFrame();
//            BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
//            bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
//            Bitmap mCacheBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length,bitmap_options);

package com.galiazat.videoStreamingCmt.cmt;

import android.util.Log;

import com.galiazat.videoStreamingCmt.UtilRectangle;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

public class CmtThread extends Thread{

    private static final String TAG = CmtThread.class.getSimpleName();
    public static final int REDUCE_WIDTH = 400 ;
    public static final int REDUCE_HEIGHT = 240;

    final private Object threadRunningBlockSync = new Object();
    final private Object copyMatBlockSync = new Object();

    volatile private Mat mRgba;
    volatile private Mat mGray;
    volatile private UtilRectangle mRectangle;
    volatile private int mViewWidth;
    volatile private int mViewHeight;

    volatile private int[] res;
    private CMT cmt;

    @Override
    public void run() {
        while (!isInterrupted()){
            if (mGray == null){
                synchronized (threadRunningBlockSync) {
                    try {
                        threadRunningBlockSync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
            Mat gray;
            Mat rgba;
            UtilRectangle rectangle;
            boolean isNewObject;
            int viewWidth;
            int viewHeight;
            synchronized (copyMatBlockSync){
                isNewObject = mRectangle != null;
                gray = this.mGray;
                rectangle = this.mRectangle;
                rgba = this.mRgba;
                viewHeight = mViewHeight;
                viewWidth = mViewWidth;
            }
            gray = reduce(gray);
            if (isNewObject){
                Log.i(TAG,"TAG: case START_CMT");
                double w = gray.width();
                double h = gray.height();

                Rect _trackedBox = rectangle.toOpenCvRect();

                Log.i(TAG, "TAG: CMT START DEFINED: " + _trackedBox.x / 2 + " "
                        + _trackedBox.y / 2 + " " + _trackedBox.width / 2 + " "
                        + _trackedBox.height / 2);

                double px = (w) / (double) (viewWidth);
                double py = (h) / (double) (viewHeight);
                //
                cmt = new CMT();
                cmt.OpenCMT(gray.getNativeObjAddr(),
                        (long) (_trackedBox.x * px),
                        (long) (_trackedBox.y * py),
                        (long) (_trackedBox.width * px),
                        (long) (_trackedBox.height * py));
            } else {
                rgba = reduce(rgba);
                cmt.ProcessCMT(gray.getNativeObjAddr(), rgba.getNativeObjAddr());
                res = CMT.getRect();
            }
            synchronized (threadRunningBlockSync) {
                try {
                    threadRunningBlockSync.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    public void startTracking(Mat rgba, Mat gray, UtilRectangle rectangle, int viewHeight, int viewWidth){
        synchronized (copyMatBlockSync){
            mGray = gray;
            mRgba = rgba;
            mRectangle = rectangle;
            mViewHeight = viewHeight;
            mViewWidth = viewWidth;
            res = null;
        }
        synchronized (threadRunningBlockSync) {
            threadRunningBlockSync.notifyAll();
        }
    }

    public void trackObject(Mat rgba, Mat gray){
        synchronized (copyMatBlockSync){
            mGray = gray;
            mRgba = rgba;
            mRectangle = null;
        }
        synchronized (threadRunningBlockSync) {
            threadRunningBlockSync.notifyAll();
        }
    }

    public int[] getRes() {
        return res;
    }

    private static Mat reduce(Mat m) {
        Mat dst = new Mat();
        Imgproc.resize(m, dst, new org.opencv.core.Size(REDUCE_WIDTH, REDUCE_HEIGHT));
        return dst;
    }
}

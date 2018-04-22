package com.galiazat.diplomtest4opencvimplement;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.opencv.core.CvType.CV_32SC1;
import static org.opencv.core.CvType.CV_8U;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    public final static String TAG_OpenCV_INIT = "OpenCV_init";

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }

    @BindView(R.id.java_camera_view)
    JavaCameraView cameraView;

    Size size;
    Mat markers;
    List<Point> selectedPoints;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    cameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        cameraView.setVisibility(View.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        setOnTouchListener();
    }

    private void setOnTouchListener(){
        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Point point = new Point((int) event.getX(), (int) event.getY());
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN: {
                        if (markers != null){
                            markers.release();
                        }
                        markers = null;
                        selectedPoints = new ArrayList<>();
                        selectedPoints.add(point);
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        selectedPoints.add(point);
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        selectedPoints.add(point);
                        int viewHeight = cameraView.getHeight();
                        int viewWidth = cameraView.getWidth();
                        double matHeight = size.height;
                        double matWidth = size.width;
                        double heightCoef = matHeight / (viewHeight * 1.0);
                        double widthCoef = matWidth / (viewWidth * 1.0);
                        markers = new Mat(size, CV_32SC1, new Scalar(0));
                        Scalar scalar = new Scalar(255, 255, 255, 0);
                        Point lastPoint = selectedPoints.get(0);
                        lastPoint.x *= widthCoef;
                        lastPoint.y *= heightCoef;
                        for (int i=1; i< selectedPoints.size(); i++){
                            Point selectedPoint = selectedPoints.get(i);
                            selectedPoint.x *= widthCoef;
                            selectedPoint.y *= heightCoef;
                            Imgproc.line(markers, lastPoint, selectedPoint, scalar);
                            lastPoint = selectedPoint;
                        }
//                        markers.convertTo(markers, CV_8U);
//                        Imgproc.cvtColor(markers, markers, Imgproc.COLOR_GRAY2BGR);
//                        markers.convertTo(markers, CV_8UC1);
//                        Imgproc.cvtColor(markers, markers, Imgproc.COLOR_GRAY2BGR);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()){
            Log.d(TAG_OpenCV_INIT, "not working");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, mLoaderCallback);
        } else {
            Log.d(TAG_OpenCV_INIT, "working");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraView != null){
            cameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraView != null){
            cameraView.disableView();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    public native String validate(long matAddrGr, long addrArgba);

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
        size = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat mat = inputFrame.rgba();
        size = mat.size();
//        if (mRgba != null) {
//            mRgba.release();
//        }
//        mRgba = inputFrame.rgba();
//        }
//        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2BGR);
//        mRgba.convertTo(mRgba, CV_8UC3);
//        if (markers != null) {
//            Imgproc.watershed(mRgba, markers);
//        }

//        Imgproc.cvtColor(mRgba, imgGray, Imgproc.COLOR_RGB2GRAY);
        if (markers != null){
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
            mat = steptowatershed(mat);
        }
//        steptowatershed(mRgba);
        return mat;

    }

    public Mat steptowatershed(Mat img)
    {
        WatershedSegmenter segmenter = new WatershedSegmenter();
        segmenter.setMarkers(markers);
        return segmenter.process(img);
    }
    public class WatershedSegmenter
    {
        public Mat mMarkers;

        public void setMarkers(Mat markerImage)
        {
            mMarkers = markerImage.clone();
        }

        public Mat process(Mat image)
        {
            Imgproc.watershed(image, mMarkers);
            mMarkers.convertTo(mMarkers, CV_8U);
            Imgproc.cvtColor(mMarkers, mMarkers, Imgproc.COLOR_GRAY2BGR);
            Core.add(image, mMarkers, image);
            mMarkers.release();
//            image.release();
//            mMarkers.convertTo(mMarkers, CV_8U);
//            Imgproc.cvtColor(mMarkers, mMarkers, Imgproc.COLOR_GRAY2BGR);
            return image;
        }
    }
}

package com.galiazat.diplomtest4opencvimplement;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

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
    @BindView(R.id.seek_bar)
    SeekBar seekBar;
    @BindView(R.id.seekbar_value)
    TextView textView;

    private Size size;
    private UtilRectangle utilRectangle = new UtilRectangle();
    private boolean isFinished = false;

    double heightCoef;
    double widthCoef;

    double threshCoef = 0.5;
    Mat oldMap;

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

        cameraView.setVisibility(View.VISIBLE);
        cameraView.setCvCameraViewListener(this);

        seekBar.setProgress((int) (threshCoef*100));
        textView.setText((int) (threshCoef*100) + "");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshCoef = progress / 100.0;
                textView.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setOnTouchListener();
    }

    private void setOnTouchListener(){
        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Point point = new Point((int) event.getX() * widthCoef,
                        (int) event.getY() * heightCoef);
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN: {
                        isFinished = false;
                        utilRectangle = new UtilRectangle();
                        utilRectangle.addPoint(point);
                        return true;
                    }
                    case MotionEvent.ACTION_MOVE: {
                        utilRectangle.addPoint(point);
                        return true;
                    }
                    case MotionEvent.ACTION_UP: {
                        utilRectangle.addPoint(point);
                        isFinished = utilRectangle.isAreaNotEqualsZero();
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

        if (oldMap != null){
            oldMap.release();
        }

        Mat mat = inputFrame.rgba();
        size = mat.size();

        int viewHeight = cameraView.getHeight();
        int viewWidth = cameraView.getWidth();
        double matHeight = size.height;
        double matWidth = size.width;
        heightCoef = matHeight / (viewHeight * 1.0);
        widthCoef = matWidth / (viewWidth * 1.0);

        if (isFinished){
            Mat newMat = new Mat(mat, utilRectangle.toOpenCvRect());
            Imgproc.GaussianBlur(newMat, newMat, new Size(5, 5), 1, 1);
            Imgproc.cvtColor(newMat, newMat, Imgproc.COLOR_BGRA2GRAY);
            Imgproc.threshold(newMat, newMat, threshCoef * 255,
                    255, Imgproc.THRESH_BINARY);
            List<MatOfPoint> matOfPointList = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(newMat, matOfPointList, hierarchy, Imgproc.RETR_EXTERNAL,
                    Imgproc.RETR_LIST, new Point(utilRectangle.getLeft(), utilRectangle.getTop()));
            hierarchy.release();
            Imgproc.drawContours(mat, matOfPointList, -1, new Scalar(234,18,94));
            newMat.convertTo(newMat, CV_8U);
            Imgproc.cvtColor(newMat, newMat, Imgproc.COLOR_GRAY2RGB);
            Imgproc.resize(newMat, newMat, size);
            newMat.release();
//            mat.release();
//            oldMap = newMat;
            return mat;
//            mat = newMat;
        }
//        steptowatershed(mRgba);
        return mat;

    }

//    public Mat steptowatershed(Mat img)
//    {
//        WatershedSegmenter segmenter = new WatershedSegmenter();
//        segmenter.setMarkers(markers);
//        return segmenter.process(img);
//    }
//    public class WatershedSegmenter
//    {
//        public Mat mMarkers;
//
//        public void setMarkers(Mat markerImage)
//        {
//            mMarkers = markerImage.clone();
//        }
//
//        public Mat process(Mat image)
//        {
//            Imgproc.watershed(image, mMarkers);
//            mMarkers.convertTo(mMarkers, CV_8U);
//            Imgproc.cvtColor(mMarkers, mMarkers, Imgproc.COLOR_GRAY2BGR);
//            Core.add(image, mMarkers, image);
//            mMarkers.release();
////            image.release();
////            mMarkers.convertTo(mMarkers, CV_8U);
////            Imgproc.cvtColor(mMarkers, mMarkers, Imgproc.COLOR_GRAY2BGR);
//            return image;
//        }
//    }
}

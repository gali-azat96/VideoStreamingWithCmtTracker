package com.galiazat.diplomtest4opencvimplement.screen.videoSource;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.galiazat.diplomtest4opencvimplement.R;
import com.galiazat.diplomtest4opencvimplement.UtilRectangle;
import com.galiazat.diplomtest4opencvimplement.cmt.CMT;
import com.galiazat.diplomtest4opencvimplement.custom.VideoSourcePreviewView;
import com.galiazat.diplomtest4opencvimplement.screen.base.BaseActivity;
import com.galiazat.diplomtest4opencvimplement.screen.videoSource.domain.VideoSourcePresenter;
import com.galiazat.diplomtest4opencvimplement.screen.videoSource.domain.VideoSourceView;
import com.galiazat.diplomtest4opencvimplement.screen.videoSource.list.SupportedFormatsAdapter;
import com.galiazat.diplomtest4opencvimplement.streaming.IpUtils;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VideoSourceActivity extends BaseActivity<VideoSourcePresenter>
        implements CameraBridgeViewBase.CvCameraViewListener2, VideoSourceView, VideoSourcePreviewView.VideoSourceListener {

    public final static String TAG = VideoSourceActivity.class.getSimpleName();
    private final static int STATE_IDLE = 0;
    private final static int STATE_CMT = 1;

    @BindView(R.id.java_camera_view)
    VideoSourcePreviewView cameraView;
    @BindView(R.id.seekbar_value)
    TextView textView;
    @BindView(R.id.supported_formats_list)
    RecyclerView supportedFormatsList;

    private SupportedFormatsAdapter adapter;

    private Size size;
    private UtilRectangle utilRectangle = new UtilRectangle();
    private boolean isFinished = false;

    double heightCoef;
    double widthCoef;

    private int state = STATE_IDLE;

    CMT cmt;

    public static void start(Activity activity){
        Intent intent = new Intent(activity, VideoSourceActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_source);

        ButterKnife.bind(this);

        cameraView.setVisibility(View.VISIBLE);
        cameraView.setCvCameraViewListener(this);
        cameraView.addListener(this);
        setOnTouchListener();
        supportedFormatsList.setVisibility(View.GONE);
        presenter.startSocket();
    }

    @Override
    protected void attachPresenter() {
        presenter.attach(this);
    }

    @Override
    protected VideoSourcePresenter createPresenter() {
        return new VideoSourcePresenter();
    }

    private void setOnTouchListener(){
        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Point point = new Point((int) ((event.getX() - cameraView.getX())),
                        (int) (event.getY() - cameraView.getY()));
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
        if (cameraView != null){
            cameraView.enableView();
            cameraView.addListener(this);
        }
        textView.setText(IpUtils.getLocalIpAddress());
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        cameraView.selectFormat(0);
    }

    @Override
    public void onCameraViewStopped() {
        size = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat mat = inputFrame.rgba();
        size = mat.size();

        int viewHeight = cameraView.getHeight();
        int viewWidth = cameraView.getWidth();
        double matHeight = size.height;
        double matWidth = size.width;
        heightCoef = matHeight / (viewHeight * 1.0);
        widthCoef = matWidth / (viewWidth * 1.0);


        if (isFinished){
            Mat mGray = Reduce(inputFrame.gray());
            if (state == STATE_IDLE) {
                Log.i(TAG,"TAG: case START_CMT");
                double w = mGray.width();
                double h = mGray.height();

                Rect _trackedBox = utilRectangle.toOpenCvRect();

                Log.i("TAG", "TAG: CMT START DEFINED: " + _trackedBox.x / 2 + " "
                        + _trackedBox.y / 2 + " " + _trackedBox.width / 2 + " "
                        + _trackedBox.height / 2);

                double px = (w) / (double) (viewWidth);
                double py = (h) / (double) (viewHeight);
                //
                cmt = new CMT();
                cmt.OpenCMT(mGray.getNativeObjAddr(),
                        (long) (_trackedBox.x * px),
                        (long) (_trackedBox.y * py),
                        (long) (_trackedBox.width * px),
                        (long) (_trackedBox.height * py));
                state = STATE_CMT;
            } else {
                Mat mRgba2 = Reduce(mat);
                cmt.ProcessCMT(mGray.getNativeObjAddr(), mRgba2.getNativeObjAddr());
                double px = (double) mat.width() / (double) mRgba2.width();
                double py = (double) mat.height() / (double) mRgba2.height();
                int[] l = CMT.getRect();
                if (l != null) {
                    Point topLeft = new Point(l[0] * px, l[1] * py);
                    Point topRight = new Point(l[2] * px, l[3] * py);
                    Point bottomLeft = new Point(l[4] * px, l[5] * py);
                    Point bottomRight = new Point(l[6] * px, l[7] * py);

                    Imgproc.line(mat, topLeft, topRight, new Scalar(255, 255,
                            255), 3);
                    Imgproc.line(mat, topRight, bottomRight, new Scalar(255,
                            255, 255), 3);
                    Imgproc.line(mat, bottomRight, bottomLeft, new Scalar(255,
                            255, 255), 3);
                    Imgproc.line(mat, bottomLeft, topLeft, new Scalar(255, 255,
                            255), 3);
                }
                mRgba2.release();
            }
            mGray.release();
            return mat;
        }
        return mat;

    }

    static final int WIDTH = 400 ;
    static final int HEIGHT =240;

    private static Mat Reduce(Mat m) {
        Mat dst = new Mat();
        Imgproc.resize(m, dst, new org.opencv.core.Size(WIDTH, HEIGHT));
        return dst;
    }

    @OnClick(R.id.menu_button)
    public void onMenuClicked(){
        if (supportedFormatsList.getVisibility() != View.VISIBLE){
            if (adapter == null){
                adapter = new SupportedFormatsAdapter(this);
                adapter.setSupportedFormats(cameraView.getSupportedPreviewSizes());
                supportedFormatsList.setAdapter(adapter);
                supportedFormatsList.setLayoutManager(new LinearLayoutManager(this));
            }
            Animation bottomUp = AnimationUtils.loadAnimation(this,
                    R.anim.bottom_up);
            supportedFormatsList.startAnimation(bottomUp);
            supportedFormatsList.setVisibility(View.VISIBLE);
        }
    }

    public void onSupportedFormatClicked(int index) {
        int selected = cameraView.getSelectedFormatIndex();
        cameraView.selectFormat(index);
        adapter.selectedChanged(selected, index);
        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_down);
        supportedFormatsList.startAnimation(bottomUp);
        supportedFormatsList.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void frameReceived(byte[] frame) {
        presenter.sendFrame(frame);
    }
}

package com.galiazat.videoStreamingCmt.screen.videoSource;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.galiazat.videoStreamingCmt.R;
import com.galiazat.videoStreamingCmt.UtilRectangle;
import com.galiazat.videoStreamingCmt.cmt.CMT;
import com.galiazat.videoStreamingCmt.cmt.CmtThread;
import com.galiazat.videoStreamingCmt.custom.VideoSourcePreviewView;
import com.galiazat.videoStreamingCmt.screen.base.BaseActivity;
import com.galiazat.videoStreamingCmt.screen.splash.SplashActivity;
import com.galiazat.videoStreamingCmt.screen.videoSource.domain.VideoSourcePresenter;
import com.galiazat.videoStreamingCmt.screen.videoSource.domain.VideoSourceView;
import com.galiazat.videoStreamingCmt.screen.videoSource.list.SupportedFormatsAdapter;
import com.galiazat.videoStreamingCmt.streaming.IpUtils;

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

import static com.galiazat.videoStreamingCmt.cmt.CmtThread.REDUCE_HEIGHT;
import static com.galiazat.videoStreamingCmt.cmt.CmtThread.REDUCE_WIDTH;

public class VideoSourceActivity extends BaseActivity<VideoSourcePresenter>
        implements CameraBridgeViewBase.CvCameraViewListener2, VideoSourceView, VideoSourcePreviewView.VideoSourceListener {

    public final static String TAG = VideoSourceActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 224;

    @BindView(R.id.java_camera_view)
    VideoSourcePreviewView cameraView;
    @BindView(R.id.seekbar_value)
    TextView textView;
    @BindView(R.id.supported_formats_list)
    RecyclerView supportedFormatsList;

    private SupportedFormatsAdapter adapter;

    private UtilRectangle utilRectangle = new UtilRectangle();
    private boolean isCurrentRectSended = true;
    private boolean isFinished = false;

    private CmtThread cmtThread;

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

        cmtThread = new CmtThread();
        cmtThread.start();
        checkAndRequestCameraPermission();
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
        cameraView.setOnTouchListener((v, event) -> {
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
                    isCurrentRectSended = false;
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isCameraPermissionGranted()) {
            cameraView.enableView();
            cameraView.addListener(this);
            textView.setText(IpUtils.getLocalIpAddress());
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
        cmtThread.interrupt();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat mat = inputFrame.rgba();
        Mat gray = inputFrame.gray();

        int viewHeight = cameraView.getHeight();
        int viewWidth = cameraView.getWidth();

        if (isFinished){
            if (!isCurrentRectSended){
                cmtThread.startTracking(mat, gray, utilRectangle, viewHeight, viewWidth);
                isCurrentRectSended = true;
            } else {
                cmtThread.trackObject(mat, gray);
                int[] l = cmtThread.getRes();

                double w = mat.width();
                double h = mat.height();

                double px = w / REDUCE_WIDTH;
                double py = h / REDUCE_HEIGHT;
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
            }
        }
        return mat;
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

    private void checkAndRequestCameraPermission() {
        if (isCameraPermissionGranted()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean isCameraPermissionGranted(){
        int cameraPermissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.CAMERA);
        return cameraPermissionCheck == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE){
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(cameraView, "Need camera permission", Snackbar.LENGTH_LONG).show();
                SplashActivity.start(this);
            }
        }
    }
}

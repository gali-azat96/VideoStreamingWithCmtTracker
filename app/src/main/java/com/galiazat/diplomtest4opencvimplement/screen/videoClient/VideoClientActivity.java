package com.galiazat.diplomtest4opencvimplement.screen.videoClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

import com.galiazat.diplomtest4opencvimplement.R;
import com.galiazat.diplomtest4opencvimplement.UtilRectangle;
import com.galiazat.diplomtest4opencvimplement.cmt.CMT;
import com.galiazat.diplomtest4opencvimplement.custom.VideoClientPreviewView;
import com.galiazat.diplomtest4opencvimplement.custom.VideoSourcePreviewView;
import com.galiazat.diplomtest4opencvimplement.screen.base.BaseActivity;
import com.galiazat.diplomtest4opencvimplement.screen.videoClient.domain.VideoClientPresenter;
import com.galiazat.diplomtest4opencvimplement.screen.videoClient.domain.VideoClientView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Azat Galiullin.
 */

public class VideoClientActivity extends BaseActivity<VideoClientPresenter>
                implements VideoClientView, VideoClientPreviewView.PreviewListener {

    private final static String IP_ADDRESS = "ip_address";
    private final static int STATE_IDLE = 0;
    private final static int STATE_CMT = 1;

    @BindView(R.id.preview)
    VideoClientPreviewView previewView;

    private Size size;
    private UtilRectangle utilRectangle = new UtilRectangle();
    private boolean isFinished = false;

    double heightCoef;
    double widthCoef;

    private int state = STATE_IDLE;

    CMT cmt;

    private String ip;

    public static void start(Activity activity, String ipAddress){
        Intent intent = new Intent(activity, VideoClientActivity.class);
        intent.putExtra(IP_ADDRESS, ipAddress);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_client);
        ButterKnife.bind(this);
        presenter.connect(ip);
        setOnTouchListener();
        previewView.addListener(this);
    }

    private void setOnTouchListener(){
        previewView.setOnTouchListener((v, event) -> {
            Point point = new Point((int) ((event.getX() - previewView.getX()) * widthCoef),
                    (int) (event.getY() - previewView.getY()) * heightCoef);
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
        });
    }

    @Override
    public Mat frameReceived(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat mat = inputFrame.rgba();
        size = mat.size();

        int viewHeight = previewView.getHeight();
        int viewWidth = previewView.getWidth();
        double matHeight = size.height;
        double matWidth = size.width;
        heightCoef = matHeight / (viewHeight * 1.0);
        widthCoef = matWidth / (viewWidth * 1.0);
        if (isFinished){
            Mat mGray = Reduce(inputFrame.gray());
            if (state == STATE_IDLE) {
                double w = mGray.width();
                double h = mGray.height();

                Rect _trackedBox = utilRectangle.toOpenCvRect();

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
        }
        return mat;
    }

    static final int WIDTH = 400 ;
    static final int HEIGHT = 240;

    private static Mat Reduce(Mat m) {
        Mat dst = new Mat();
        Imgproc.resize(m, dst, new org.opencv.core.Size(WIDTH, HEIGHT));
        return dst;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (previewView != null){
            previewView.clear();
        }
    }

    @Override
    protected void attachPresenter() {
        presenter.attach(this);
    }

    @Override
    protected VideoClientPresenter createPresenter() {
        return new VideoClientPresenter();
    }

    @Override
    public void showFrame(VideoSourcePreviewView.VideoSourceListener.SendingFrame sendingFrame) {
        previewView.showFrame(sendingFrame);
    }

    @Override
    public void initArgs() {
        super.initArgs();
        ip = getIntent().getStringExtra(IP_ADDRESS);
    }
}

package com.galiazat.videoStreamingCmt.screen.videoSource.domain;

import android.view.MotionEvent;

import com.galiazat.videoStreamingCmt.UtilRectangle;
import com.galiazat.videoStreamingCmt.cmt.CmtThread;
import com.galiazat.videoStreamingCmt.screen.base.BasePresenter;
import com.galiazat.videoStreamingCmt.streaming.Constants;
import com.galiazat.videoStreamingCmt.streaming.source.ServerSocketThread;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static com.galiazat.videoStreamingCmt.cmt.CmtThread.REDUCE_HEIGHT;
import static com.galiazat.videoStreamingCmt.cmt.CmtThread.REDUCE_WIDTH;

/**
 * @author Azat Galiullin.
 */

public class VideoSourcePresenter extends BasePresenter<VideoSourceView, VideoSourceModel> {

    private ServerSocketThread serverSocketThread;
    private UtilRectangle utilRectangle = new UtilRectangle();
    private boolean isCurrentRectSended = true;
    private boolean isFinished = false;

    private CmtThread cmtThread;

    public VideoSourcePresenter() {
        super(new VideoSourceModel());
        startRecognitionThread();
        startStream();
    }

    public boolean onRectDrawing(float x, float y, int kindOfAction){
        Point point = new Point((int) x, (int) y);
        switch (kindOfAction){
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
    }

    public Mat onFrameFromCameraReceived(CameraBridgeViewBase.CvCameraViewFrame inputFrame,
                                          int viewWidth, int viewHeight){
        Mat mat = inputFrame.rgba();
        Mat gray = inputFrame.gray();

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

    private void startRecognitionThread(){
        cmtThread = new CmtThread();
        cmtThread.start();
    }

    private void startStream() {
        serverSocketThread = new ServerSocketThread(Constants.BASE_PORT);
        serverSocketThread.start();
    }

    public void sendFrame(byte[] frame) {
        serverSocketThread.sendFrame(frame);
    }

    @Override
    public void destroy() {
        super.destroy();
        serverSocketThread.interrupt();
        cmtThread.interrupt();
    }
}

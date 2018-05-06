package com.galiazat.diplomtest4opencvimplement.screen.videoSource.domain;

import com.galiazat.diplomtest4opencvimplement.screen.base.BasePresenter;
import com.galiazat.diplomtest4opencvimplement.streaming.Constants;
import com.galiazat.diplomtest4opencvimplement.streaming.source.ServerSocketThread;

import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public class VideoSourcePresenter extends BasePresenter<VideoSourceView, VideoSourceModel> {

    private ServerSocketThread serverSocketThread;

    public VideoSourcePresenter() {
        super(new VideoSourceModel());
    }

    public void startSocket(){
        serverSocketThread = new ServerSocketThread(Constants.BASE_PORT);
        serverSocketThread.start();
    }

    public void sendMat(Mat mat){
        serverSocketThread.sendMat(mat);
    }

    @Override
    public void destroy() {
        super.destroy();
        serverSocketThread.interrupt();
    }
}

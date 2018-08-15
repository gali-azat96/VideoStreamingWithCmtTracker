package com.galiazat.videoStreamingCmt.screen.videoSource.domain;

import android.view.SurfaceHolder;

import com.galiazat.videoStreamingCmt.custom.VideoSourcePreviewView;
import com.galiazat.videoStreamingCmt.screen.base.BasePresenter;
import com.galiazat.videoStreamingCmt.streaming.Constants;
import com.galiazat.videoStreamingCmt.streaming.source.ServerSocketThread;

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

    public void sendFrame(byte[] frame){
        serverSocketThread.sendFrame(frame);
    }

    @Override
    public void destroy() {
        super.destroy();
        serverSocketThread.interrupt();
    }
}

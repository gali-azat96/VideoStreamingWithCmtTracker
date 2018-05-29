package com.galiazat.diplomtest4opencvimplement.screen.videoSource.domain;

import android.view.SurfaceHolder;

import com.galiazat.diplomtest4opencvimplement.custom.VideoSourcePreviewView;
import com.galiazat.diplomtest4opencvimplement.screen.base.BasePresenter;
import com.galiazat.diplomtest4opencvimplement.streaming.Constants;
import com.galiazat.diplomtest4opencvimplement.streaming.source.ServerSocketThread;

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

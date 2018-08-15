package com.galiazat.videoStreamingCmt.screen.videoClient.domain;

import com.galiazat.videoStreamingCmt.custom.VideoSourcePreviewView;
import com.galiazat.videoStreamingCmt.screen.base.BasePresenter;
import com.galiazat.videoStreamingCmt.streaming.Constants;
import com.galiazat.videoStreamingCmt.streaming.client.ClientSocketListener;
import com.galiazat.videoStreamingCmt.streaming.client.ClientSocketThread;

import java.io.IOException;

/**
 * @author Azat Galiullin.
 */

public class VideoClientPresenter extends BasePresenter<VideoClientView, VideoClientModel> implements ClientSocketListener {

    private ClientSocketThread clientSocketThread;

    public VideoClientPresenter() {
        super(new VideoClientModel());
    }

    public void connect(String ip){
        try {
            clientSocketThread = new ClientSocketThread(ip, Constants.BASE_PORT,
                    null, this);
            clientSocketThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onFrameReceived(VideoSourcePreviewView.SendingFrame sendingFrame) {
        if (view != null){
            view.showFrame(sendingFrame);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (clientSocketThread != null){
            clientSocketThread.clear();
            clientSocketThread.interrupt();
        }
    }
}

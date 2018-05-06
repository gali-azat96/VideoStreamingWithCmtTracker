package com.galiazat.diplomtest4opencvimplement.screen.videoClient.domain;

import android.os.Handler;
import android.os.Looper;

import com.galiazat.diplomtest4opencvimplement.screen.base.BasePresenter;
import com.galiazat.diplomtest4opencvimplement.streaming.Constants;
import com.galiazat.diplomtest4opencvimplement.streaming.client.ClientSocketListener;
import com.galiazat.diplomtest4opencvimplement.streaming.client.ClientSocketThread;

import org.opencv.core.Mat;

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
    public void onMatReceived(Mat mat) {
        if (view != null){
            view.showMat(mat);
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

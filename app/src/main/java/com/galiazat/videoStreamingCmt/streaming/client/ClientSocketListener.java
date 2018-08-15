package com.galiazat.videoStreamingCmt.streaming.client;

import com.galiazat.videoStreamingCmt.custom.VideoSourcePreviewView;

import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public interface ClientSocketListener {
    void onFrameReceived(VideoSourcePreviewView.SendingFrame sendingFrame);
}

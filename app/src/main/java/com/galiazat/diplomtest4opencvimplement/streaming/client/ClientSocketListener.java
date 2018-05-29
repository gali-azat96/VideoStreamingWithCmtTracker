package com.galiazat.diplomtest4opencvimplement.streaming.client;

import com.galiazat.diplomtest4opencvimplement.custom.VideoSourcePreviewView;

import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public interface ClientSocketListener {
    void onFrameReceived(VideoSourcePreviewView.SendingFrame sendingFrame);
}

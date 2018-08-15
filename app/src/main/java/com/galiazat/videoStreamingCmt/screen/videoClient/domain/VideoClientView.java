package com.galiazat.videoStreamingCmt.screen.videoClient.domain;

import com.galiazat.videoStreamingCmt.custom.VideoSourcePreviewView;
import com.galiazat.videoStreamingCmt.screen.base.BaseView;

import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public interface VideoClientView extends BaseView{
    void showFrame(VideoSourcePreviewView.SendingFrame sendingFrame);
}

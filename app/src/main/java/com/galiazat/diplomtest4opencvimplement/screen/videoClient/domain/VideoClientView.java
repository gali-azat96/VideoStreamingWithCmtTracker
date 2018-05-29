package com.galiazat.diplomtest4opencvimplement.screen.videoClient.domain;

import com.galiazat.diplomtest4opencvimplement.custom.VideoSourcePreviewView;
import com.galiazat.diplomtest4opencvimplement.screen.base.BaseView;

import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public interface VideoClientView extends BaseView{
    void showFrame(VideoSourcePreviewView.SendingFrame sendingFrame);
}

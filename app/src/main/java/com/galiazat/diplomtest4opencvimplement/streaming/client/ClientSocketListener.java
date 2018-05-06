package com.galiazat.diplomtest4opencvimplement.streaming.client;

import org.opencv.core.Mat;

/**
 * @author Azat Galiullin.
 */

public interface ClientSocketListener {
    void onMatReceived(Mat mat);
}

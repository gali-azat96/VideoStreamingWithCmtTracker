package com.galiazat.diplomtest4opencvimplement.custom;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import com.galiazat.diplomtest4opencvimplement.entites.SupportedFormat;

import org.opencv.android.JavaCameraView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class VideoSourcePreviewView extends JavaCameraView {

    private final List<VideoSourceListener> listeners = new ArrayList<>();
    private int frameHeight, frameWidth;
    private int previewType;
    private List<SupportedFormat> supportedPreviewSizes;
    private int selectedFormatIndex;

    public VideoSourcePreviewView(Context context, int cameraId) {
        super(context, cameraId);
    }

    public VideoSourcePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onPreviewFrame(byte[] frame, Camera arg1) {
        super.onPreviewFrame(frame, arg1);
        for (VideoSourceListener listener: listeners){
            listener.frameReceived(new VideoSourceListener.SendingFrame(frame, previewType, frameHeight, frameWidth));
        }
    }

    public void addListener(VideoSourceListener listener){
        listeners.add(listener);
    }

    @Override
    public void disableView() {
        super.disableView();
        listeners.clear();
    }

    @Override
    protected boolean initializeCamera(int width, int height) {
        boolean b = super.initializeCamera(width, height);
        Camera.Parameters parameters = mCamera.getParameters();
        previewType = parameters.getPreviewFormat();
        frameHeight = parameters.getPreviewSize().height;
        frameWidth = parameters.getPreviewSize().width;

        supportedPreviewSizes = SupportedFormat.convertToSupportedFormatList(parameters.getSupportedPictureSizes());
        for (int i=0; i<supportedPreviewSizes.size(); i++){
            SupportedFormat format = supportedPreviewSizes.get(i);
            if (format.getSize().height == frameHeight && format.getSize().width == frameWidth){
                format.setSelected(true);
                selectedFormatIndex = i;
            }
        }
        return b;
    }

    public List<SupportedFormat> getSupportedPreviewSizes() {
        return supportedPreviewSizes;
    }

    public int getSelectedFormatIndex(){
        return selectedFormatIndex;
    }

    public void selectFormat(int index) {
        supportedPreviewSizes.get(index).setSelected(true);
        supportedPreviewSizes.get(selectedFormatIndex).setSelected(false);
        selectedFormatIndex = index;
        SupportedFormat format = supportedPreviewSizes.get(index);
        connectCamera(format.getSize().width, format.getSize().height);
    }

    public interface VideoSourceListener{
        void frameReceived(SendingFrame frame);

        class SendingFrame{
            private int frameHeight, frameWidth;
            private int previewType;
            private byte[] frame;

            public SendingFrame(byte[] frame, int previewType,
                                int frameHeight, int frameWidth) {
                this.frameHeight = frameHeight;
                this.frameWidth = frameWidth;
                this.previewType = previewType;
                this.frame = frame;
            }

            public int getFrameHeight() {
                return frameHeight;
            }

            public int getFrameWidth() {
                return frameWidth;
            }

            public int getPreviewType() {
                return previewType;
            }

            public byte[] getFrame() {
                return frame;
            }
        }

    }

}

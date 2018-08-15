package com.galiazat.videoStreamingCmt.entites;

import android.hardware.Camera;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Azat Galiullin.
 */

public class SupportedFormat {

    private Camera.Size size;
    private boolean isSelected = false;

    public SupportedFormat(Camera.Size size, boolean isSelected) {
        this.size = size;
        this.isSelected = isSelected;
    }

    public Camera.Size getSize() {
        return size;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public static List<SupportedFormat> convertToSupportedFormatList(List<Camera.Size> sizes){
        List<SupportedFormat> supportedFormats = new ArrayList<>();
        for (Camera.Size size : sizes){
            supportedFormats.add(new SupportedFormat(size, false));
        }
        return supportedFormats;
    }

}

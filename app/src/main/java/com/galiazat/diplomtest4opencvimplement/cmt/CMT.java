package com.galiazat.diplomtest4opencvimplement.cmt;

/**
 * @author Azat Galiullin.
 */

public class CMT {

    static {
        System.loadLibrary("native-lib");
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);

    public native void OpenCMT(long matAddrGr, long matAddrRgba, long x,
                               long y, long w, long h);

    public native void ProcessCMT(long matAddrGr, long matAddrRgba);

    public native void CMTSave(java.lang.String Path);
    public native void CMTLoad(java.lang.String Path);


    public static native int[] getRect();

}

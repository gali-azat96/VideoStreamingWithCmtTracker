package com.galiazat.diplomtest4opencvimplement.screen.splash.domain;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.galiazat.diplomtest4opencvimplement.App;
import com.galiazat.diplomtest4opencvimplement.screen.base.BasePresenter;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

/**
 * @author Azat Galiullin.
 */

public class SplashPresenter extends BasePresenter<SplashView, SplashModel>{

    private final static String TAG_OpenCV_INIT = "OpenCV_init";

    private enum USER_SELECTED_ACTION {SOURCE, CLIENT, NOT_SELECTED}

    private boolean isOpenCvInitialized = false;
    private USER_SELECTED_ACTION userSelectedAction = USER_SELECTED_ACTION.NOT_SELECTED;
    private InitOpenCvThread initOpenCvThread;
    private String ip;

    public SplashPresenter() {
        super(new SplashModel());
    }

    public void initOpenCv(){
        isOpenCvInitialized = false;
        userSelectedAction = USER_SELECTED_ACTION.NOT_SELECTED;
        initOpenCvThread = new InitOpenCvThread();
        initOpenCvThread.start();
    }

    public void onOpenClientScreenClicked(String ip){
        this.ip = ip;
        if (isOpenCvInitialized){
            view.openClientScreen(ip);
        } else {
            userSelectedAction = USER_SELECTED_ACTION.CLIENT;
        }
    }

    public void onOpenSourceScreenClicked(){
        if (isOpenCvInitialized){
            view.openSourceScreen();
        } else {
            userSelectedAction = USER_SELECTED_ACTION.SOURCE;
        }
    }

    private void onOpenCvInitialized(){
        isOpenCvInitialized = true;
        if (userSelectedAction != USER_SELECTED_ACTION.NOT_SELECTED){
            if (view != null) {
                switch (userSelectedAction){
                    case CLIENT:{
                        view.openClientScreen(ip);
                    }
                    default:{
                        view.openSourceScreen();
                    }
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (initOpenCvThread != null){
            initOpenCvThread.interrupt();
            initOpenCvThread = null;
        }
    }

    private class InitOpenCvThread extends Thread{

        @Override
        public void run() {
            System.loadLibrary("opencv_java3");
            if (isInterrupted()){
                return;
            }
            if (!OpenCVLoader.initDebug()){
                Log.d(TAG_OpenCV_INIT, "not working");
                OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, App.getInstance(), mLoaderCallback);
            } else {
                Log.d(TAG_OpenCV_INIT, "working");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }

        private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(App.getInstance()) {
            @Override
            public void onManagerConnected(int status) {
                if (isInterrupted()){
                    return;
                }
                switch (status){
                    case BaseLoaderCallback.SUCCESS:{
                        new Handler(Looper.getMainLooper()).post(SplashPresenter.this::onOpenCvInitialized);
                        break;
                    }
                    default:{
                        super.onManagerConnected(status);
                        break;
                    }
                }
            }
        };

    }



}

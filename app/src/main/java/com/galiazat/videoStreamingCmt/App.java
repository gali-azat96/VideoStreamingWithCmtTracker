package com.galiazat.videoStreamingCmt;

import android.app.Application;

/**
 * @author Azat Galiullin.
 */

public class App extends Application {

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static App getInstance(){
        return sInstance;
    }

}

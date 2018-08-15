package com.galiazat.videoStreamingCmt.screen.splash.domain;

import com.galiazat.videoStreamingCmt.screen.base.BaseView;

/**
 * @author Azat Galiullin.
 */

public interface SplashView extends BaseView{
    void openSourceScreen();

    void openClientScreen(String ip);
}

package com.galiazat.diplomtest4opencvimplement.screen.splash.domain;

import com.galiazat.diplomtest4opencvimplement.screen.base.BaseView;

/**
 * @author Azat Galiullin.
 */

public interface SplashView extends BaseView{
    void openSourceScreen();

    void openClientScreen(String ip);
}

package com.galiazat.videoStreamingCmt.screen.start.domain;

import com.galiazat.videoStreamingCmt.screen.base.BaseView;

/**
 * @author Azat Galiullin.
 */

public interface StartView extends BaseView{
    void openSourceScreen();

    void openClientScreen(String ip);
}

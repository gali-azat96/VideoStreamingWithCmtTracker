package com.galiazat.diplomtest4opencvimplement.screen.videoClient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.galiazat.diplomtest4opencvimplement.R;
import com.galiazat.diplomtest4opencvimplement.custom.VideoPreviewView;
import com.galiazat.diplomtest4opencvimplement.screen.base.BaseActivity;
import com.galiazat.diplomtest4opencvimplement.screen.videoClient.domain.VideoClientPresenter;
import com.galiazat.diplomtest4opencvimplement.screen.videoClient.domain.VideoClientView;

import org.opencv.android.JavaCameraView;
import org.opencv.core.Mat;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Azat Galiullin.
 */

public class VideoClientActivity extends BaseActivity<VideoClientPresenter>
                implements VideoClientView{

    private final static String IP_ADDRESS = "ip_address";

    @BindView(R.id.preview)
    VideoPreviewView previewView;

    private String ip;

    public static void start(Activity activity, String ipAddress){
        Intent intent = new Intent(activity, VideoClientActivity.class);
        intent.putExtra(IP_ADDRESS, ipAddress);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_client);
        ButterKnife.bind(this);
        presenter.connect(ip);
    }

    @Override
    protected void attachPresenter() {
        presenter.attach(this);
    }

    @Override
    protected VideoClientPresenter createPresenter() {
        return new VideoClientPresenter();
    }

    @Override
    public void showMat(Mat mat) {
        previewView.showMat(mat);
    }

    @Override
    public void initArgs() {
        super.initArgs();
        ip = getIntent().getStringExtra(IP_ADDRESS);
    }
}

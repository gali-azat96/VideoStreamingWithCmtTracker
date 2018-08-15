package com.galiazat.videoStreamingCmt.screen.start;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.galiazat.videoStreamingCmt.R;
import com.galiazat.videoStreamingCmt.screen.base.BaseActivity;
import com.galiazat.videoStreamingCmt.screen.start.domain.StartPresenter;
import com.galiazat.videoStreamingCmt.screen.start.domain.StartView;
import com.galiazat.videoStreamingCmt.screen.videoClient.VideoClientActivity;
import com.galiazat.videoStreamingCmt.screen.videoSource.VideoSourceActivity;
import com.redmadrobot.inputmask.MaskedTextChangedListener;
import com.redmadrobot.inputmask.model.Notation;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Azat Galiullin.
 */

public class StartActivity extends BaseActivity<StartPresenter>
                            implements StartView {

    @BindView(R.id.ip_input)
    EditText ipInput;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, StartActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        MaskedTextChangedListener maskedTextChangedListener = new MaskedTextChangedListener(
                "[099]{.}[099]{.}[099]{.}[099]",
                new ArrayList<Notation>(),
                true,
                ipInput,
                null,
                null
        );
        ipInput.addTextChangedListener(maskedTextChangedListener);
        ipInput.setOnFocusChangeListener(maskedTextChangedListener);
        ipInput.setHint(maskedTextChangedListener.placeholder());
        presenter.initOpenCv();
    }

    @Override
    protected void attachPresenter() {
        presenter.attach(this);
    }

    @Override
    protected StartPresenter createPresenter() {
        return new StartPresenter();
    }

    @OnClick(R.id.video_source)
    public void onSourceStreamClick(){
        presenter.onOpenSourceScreenClicked();
    }

    @OnClick(R.id.video_client)
    public void onStreamVideoClick(){
        presenter.onOpenClientScreenClicked(ipInput.getText().toString());
    }

    @Override
    public void openSourceScreen() {
        VideoSourceActivity.start(this);
    }

    @Override
    public void openClientScreen(String ip) {
        VideoClientActivity.start(this, ip);
    }
}

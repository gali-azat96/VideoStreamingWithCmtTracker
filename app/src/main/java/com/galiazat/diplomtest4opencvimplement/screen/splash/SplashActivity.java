package com.galiazat.diplomtest4opencvimplement.screen.splash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.galiazat.diplomtest4opencvimplement.R;
import com.galiazat.diplomtest4opencvimplement.screen.base.BaseActivity;
import com.galiazat.diplomtest4opencvimplement.screen.splash.domain.SplashPresenter;
import com.galiazat.diplomtest4opencvimplement.screen.splash.domain.SplashView;
import com.galiazat.diplomtest4opencvimplement.screen.videoClient.VideoClientActivity;
import com.galiazat.diplomtest4opencvimplement.screen.videoSource.VideoSourceActivity;
import com.redmadrobot.inputmask.MaskedTextChangedListener;
import com.redmadrobot.inputmask.model.Notation;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Azat Galiullin.
 */

public class SplashActivity extends BaseActivity<SplashPresenter>
                            implements SplashView{

    @BindView(R.id.ip_input)
    EditText ipInput;

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
    protected SplashPresenter createPresenter() {
        return new SplashPresenter();
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

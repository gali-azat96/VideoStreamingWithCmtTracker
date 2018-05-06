package com.galiazat.diplomtest4opencvimplement.screen.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Azat Galiullin.
 */

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity{

    protected P presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initArgs();
        if (presenter == null) {
            presenter = createPresenter();
        }
        attachPresenter();
    }

    public void initArgs() {

    }

    protected abstract void attachPresenter();

    protected abstract P createPresenter();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.destroy();
        }
    }
}

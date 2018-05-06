package com.galiazat.diplomtest4opencvimplement.screen.base;

/**
 * @author Azat Galiullin.
 */

public abstract class BasePresenter<V extends BaseView, M extends BaseModel> {

    protected V view;
    protected M model;

    public BasePresenter(M model) {
        this.model = model;
    }

    public void attach(V v){
        this.view = v;
    }

    public void destroy(){
        model.destroy();
        view = null;
    }

}

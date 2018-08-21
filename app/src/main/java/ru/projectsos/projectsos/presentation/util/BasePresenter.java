package ru.projectsos.projectsos.presentation.util;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Base presenter
 * Takes over RxJava-specific code for avoiding boilerplate code
 *
 * @param <View> interface for activity/fragment
 */
public abstract class BasePresenter<View extends MvpView> extends MvpPresenter<View> {

    private CompositeDisposable mCompositeDisposable;

    /**
     * Initializes special container for RxJava sources
     */
    protected BasePresenter() {
        super();
        mCompositeDisposable = new CompositeDisposable();
    }

    /**
     * Get special container for RxJava sources
     *
     * @return {@link CompositeDisposable}
     */
    protected CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    /**
     * Disposes special container for RxJava sources to prevent errors for subscribers
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }

}

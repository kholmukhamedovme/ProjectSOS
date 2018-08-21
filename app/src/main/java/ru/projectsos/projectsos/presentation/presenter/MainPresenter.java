package ru.projectsos.projectsos.presentation.presenter;

import android.support.annotation.NonNull;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.projectsos.projectsos.domain.MainInteractor;
import ru.projectsos.projectsos.presentation.util.BasePresenter;
import ru.projectsos.projectsos.presentation.view.MainView;

import static dagger.internal.Preconditions.checkNotNull;

@InjectViewState
public final class MainPresenter extends BasePresenter<MainView> {

    private static final String TAG = "PROJECT_SOS";

    private final MainInteractor mInteractor;

    private String mMacAddress;

    public MainPresenter(@NonNull MainInteractor mainInteractor) {
        mInteractor = checkNotNull(mainInteractor, "MainInteractor is required");
    }

    public void setMacAddress(String macAddress) {
        mMacAddress = macAddress;
    }

    @Override
    protected void onFirstViewAttach() {
        getCompositeDisposable().add(
                mInteractor.traceBluetoothState()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMapCompletable(state -> {
                            switch (state) {
                                case READY:
                                    return mInteractor.authenticateDevice(mMacAddress);
                                case BLUETOOTH_NOT_AVAILABLE:
                                    getViewState().informNoBluetoothAvailable();
                                    return Completable.complete();
                                case LOCATION_PERMISSION_NOT_GRANTED:
                                    getViewState().informGrantLocationPermission();
                                    return Completable.complete();
                                case BLUETOOTH_NOT_ENABLED:
                                    getViewState().informEnableBluetooth();
                                    return Completable.complete();
                                case LOCATION_SERVICES_NOT_ENABLED:
                                    getViewState().informEnableLocationServices();
                                    return Completable.complete();
                                default:
                                    return Completable.complete();
                            }
                        })
                        .subscribe()
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInteractor.gracefullyShutdown();
    }

}

package ru.projectsos.projectsos.presentation.presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.projectsos.projectsos.R;
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
        traceDeviceState();
    }

    @Override
    protected void onFirstViewAttach() {
        getCompositeDisposable().add(
                mInteractor.traceBluetoothState()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> {
                            switch (state) {
                                case READY:
                                    authenticateDevice();
                                    break;
                                case LOCATION_PERMISSION_NOT_GRANTED:
                                    getViewState().informGrantLocationPermission();
                                    break;
                                case LOCATION_SERVICES_NOT_ENABLED:
                                    getViewState().informEnableLocationServices();
                                    break;
                                case BLUETOOTH_NOT_ENABLED:
                                    getViewState().informEnableBluetooth();
                                    break;
                                case BLUETOOTH_NOT_AVAILABLE:
                                    getViewState().informNoBluetoothAvailable();
                                    break;
                            }
                        }, throwable -> Log.e(TAG, throwable.getLocalizedMessage(), throwable))
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gracefullyShutdown();
    }

    private void authenticateDevice() {
        getCompositeDisposable().add(
                mInteractor.authenticateDevice(mMacAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, throwable -> Log.e(TAG, throwable.getLocalizedMessage(), throwable))
        );
    }

    private void traceDeviceState() {
        getCompositeDisposable().add(
                mInteractor.traceDeviceState(mMacAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> {
                            switch (state) {
                                case CONNECTING:
                                    getViewState().informDeviceState(R.string.device_state_connecting);
                                    break;
                                case CONNECTED:
                                    getViewState().informDeviceState(R.string.device_state_connected);
                                    break;
                                case DISCONNECTING:
                                    getViewState().informDeviceState(R.string.device_state_disconnecting);
                                    break;
                                case DISCONNECTED:
                                    getViewState().informDeviceState(R.string.device_state_disconnected);
                                    break;
                            }
                        }, throwable -> Log.e(TAG, throwable.getLocalizedMessage(), throwable))
        );
    }

    private void gracefullyShutdown() {
        getCompositeDisposable().add(
                mInteractor.gracefullyShutdown()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, throwable -> Log.e(TAG, throwable.getLocalizedMessage(), throwable))
        );
    }

}

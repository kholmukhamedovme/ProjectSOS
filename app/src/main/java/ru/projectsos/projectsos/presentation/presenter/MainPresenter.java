package ru.projectsos.projectsos.presentation.presenter;

import android.support.annotation.NonNull;

import com.arellomobile.mvp.InjectViewState;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.projectsos.projectsos.domain.MainInteractor;
import ru.projectsos.projectsos.models.domain.BluetoothState;
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
                        .subscribe(this::onStateChanges)
        );
    }

    private void onStateChanges(BluetoothState state) {
        switch (state) {
            case READY:
                getCompositeDisposable().add(
                        mInteractor.authenticateDevice(mMacAddress)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe()
                );
                break;
            case BLUETOOTH_NOT_AVAILABLE:
                getViewState().informNoBluetoothAvailable();
                break;
            case LOCATION_PERMISSION_NOT_GRANTED:
                getViewState().informGrantLocationPermission();
                break;
            case BLUETOOTH_NOT_ENABLED:
                getViewState().informEnableBluetooth();
                break;
            case LOCATION_SERVICES_NOT_ENABLED:
                getViewState().informEnableLocationServices();
                break;
        }
    }

}

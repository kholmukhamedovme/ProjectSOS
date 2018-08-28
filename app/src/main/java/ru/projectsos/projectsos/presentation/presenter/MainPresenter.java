package ru.projectsos.projectsos.presentation.presenter;

import android.support.annotation.NonNull;
import android.util.Log;

import com.arellomobile.mvp.InjectViewState;

import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.projectsos.projectsos.R;
import ru.projectsos.projectsos.data.AuthConstants;
import ru.projectsos.projectsos.domain.MainInteractor;
import ru.projectsos.projectsos.models.domain.BluetoothState;
import ru.projectsos.projectsos.models.domain.DeviceState;
import ru.projectsos.projectsos.presentation.util.BasePresenter;
import ru.projectsos.projectsos.presentation.view.MainView;

import static dagger.internal.Preconditions.checkNotNull;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_REQUEST_RANDOM_KEY_COMMAND;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_RESPONSE;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_SEND_ENCRYPTED_KEY_COMMAND;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_SEND_SECRET_KEY_COMMAND;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_SUCCESS;

@InjectViewState
public final class MainPresenter extends BasePresenter<MainView> {

    private static final String TAG = "PROJECT_SOS";

    private final MainInteractor mInteractor;

    private String mMacAddress;

    /**
     * Конструктор
     *
     * @param mainInteractor интерактор бизнес-задач
     */
    public MainPresenter(@NonNull MainInteractor mainInteractor) {
        mInteractor = checkNotNull(mainInteractor, "MainInteractor is required");
    }

    /**
     * Установить MAC адрес устройства
     * Вызывается до запуска всех других методов, а также до связки с окном
     * При вызове запускается наблюдение за состоянием устройства
     *
     * @param macAddress MAC адрес
     */
    public void setMacAddress(String macAddress) {
        mMacAddress = macAddress;
        traceDeviceState();
    }

    /**
     * При первой связке окна запустить наблюдение за состоянием Bluetooth
     */
    @Override
    protected void onFirstViewAttach() {
        traceBluetoothState();
    }

    /**
     * При завершении запустить правильное выключение
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        gracefullyShutdown();
    }

    /**
     * Запустить наблюдение за состоянием устройства
     */
    private void traceDeviceState() {
        getCompositeDisposable().add(
                mInteractor.traceDeviceState(mMacAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onTraceDeviceState, this::onError)
        );
    }

    /**
     * Запустить наблюдение за состоянием Bluetooth
     */
    private void traceBluetoothState() {
        getCompositeDisposable().add(
                mInteractor.traceBluetoothState()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onTraceBluetoothState, this::onError)
        );
    }

    /**
     * Запустить подписку на уведомления
     */
    private void setupNotification() {
        getCompositeDisposable().add(
                mInteractor.setupNotification(mMacAddress)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(this::onSetupNotification, this::onError)
        );
    }

    /**
     * Отправить секретный ключ
     */
    private void sendSecretKey() {
        getCompositeDisposable().add(
                mInteractor.sendSecretKey()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, this::onError)
        );
    }

    /**
     * Запросить случайный ключ
     */
    private void requestRandomKey() {
        getCompositeDisposable().add(
                mInteractor.requestRandomKey()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, this::onError)
        );
    }

    /**
     * Отправить зашифрованный ключ
     *
     * @param randomKeyResponse уведомление со случайным ключом
     */
    private void sendEncryptedKey(byte[] randomKeyResponse) {
        getCompositeDisposable().add(
                mInteractor.sendEncryptedKey(randomKeyResponse)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, this::onError)
        );
    }

    /**
     * Запустить процедуру после первой аутентификации устройства
     */
    private void afterFirstAuthentication() {
        getCompositeDisposable().add(
                mInteractor.afterFirstAuthentication()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            // TODO: Parse data
                        }, this::onError)
        );
    }

    /**
     * Запустить правильное выключение
     */
    private void gracefullyShutdown() {
        getCompositeDisposable().add(
                mInteractor.gracefullyShutdown()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                        }, throwable -> Log.e(TAG, throwable.getLocalizedMessage(), throwable))
        );
    }

    /**
     * Реагирование на изменение состояния устройства
     *
     * @param state состояние устройства
     */
    private void onTraceDeviceState(DeviceState state) {
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
    }

    /**
     * Реагирование на изменение состояния Bluetooth
     *
     * @param state состояние Bluetooth
     */
    private void onTraceBluetoothState(BluetoothState state) {
        switch (state) {
            case READY:
                setupNotification();

                if (mInteractor.isFirstAuthentication()) {
                    sendSecretKey();
                } else {
                    requestRandomKey();
                }

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
    }

    /**
     * Реагирование на получение уведомлений
     *
     * @param bytes уведомление в байтах
     * @see AuthConstants
     */
    private void onSetupNotification(byte[] bytes) {
        if (bytes[0] == AUTH_RESPONSE && bytes[1] == AUTH_SEND_SECRET_KEY_COMMAND && bytes[2] == AUTH_SUCCESS) {
            requestRandomKey();
        } else if (bytes[0] == AUTH_RESPONSE && bytes[1] == AUTH_REQUEST_RANDOM_KEY_COMMAND && bytes[2] == AUTH_SUCCESS) {
            sendEncryptedKey(bytes);
        } else if (bytes[0] == AUTH_RESPONSE && bytes[1] == AUTH_SEND_ENCRYPTED_KEY_COMMAND && bytes[2] == AUTH_SUCCESS) {
            Log.d(TAG, "AUTHENTICATED");
            afterFirstAuthentication();
        } else {
            Log.e(TAG, "UNKNOWN: " + Arrays.toString(bytes));
        }
    }

    /**
     * Реагирование на ошибку
     *
     * @param throwable ошибка
     */
    private void onError(Throwable throwable) {
        Log.e(TAG, throwable.getLocalizedMessage(), throwable);
    }

}

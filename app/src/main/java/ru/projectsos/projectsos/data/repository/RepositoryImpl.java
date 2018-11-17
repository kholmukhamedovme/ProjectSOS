package ru.projectsos.projectsos.data.repository;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import ru.projectsos.projectsos.domain.Repository;
import ru.projectsos.projectsos.models.converter.AbstractConverter;
import ru.projectsos.projectsos.models.converter.RxBleClientStateToBluetoothStateConverter;
import ru.projectsos.projectsos.models.converter.RxBleConnectionStateToDeviceStateConverter;
import ru.projectsos.projectsos.models.domain.BluetoothState;
import ru.projectsos.projectsos.models.domain.DeviceState;

import static dagger.internal.Preconditions.checkNotNull;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_BYTE;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_CHAR;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_REQUEST_RANDOM_KEY_COMMAND;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_SEND_ENCRYPTED_KEY_COMMAND;
import static ru.projectsos.projectsos.data.AuthConstants.AUTH_SEND_SECRET_KEY_COMMAND;
import static ru.projectsos.projectsos.data.AuthConstants.SECRET_KEY;
import static ru.projectsos.projectsos.data.AuthConstants.encryptRandomKeyWithSecretKey;
import static ru.projectsos.projectsos.data.HeartRateConstants.ENABLE_GYROSCOPE_AND_HEART_RAW_DATA_COMMAND;
import static ru.projectsos.projectsos.data.HeartRateConstants.HMC_CHAR;
import static ru.projectsos.projectsos.data.HeartRateConstants.HRM_CHAR;
import static ru.projectsos.projectsos.data.HeartRateConstants.PING_COMMAND;
import static ru.projectsos.projectsos.data.HeartRateConstants.SENSOR_CHAR;
import static ru.projectsos.projectsos.data.HeartRateConstants.START_CONTINUOUS_MEASUREMENTS_COMMAND;
import static ru.projectsos.projectsos.data.HeartRateConstants.TURN_OFF_CONTINUOUS_MEASUREMENTS_COMMAND;
import static ru.projectsos.projectsos.data.HeartRateConstants.TURN_OFF_ONE_SHOT_MEASUREMENTS_COMMAND;
import static ru.projectsos.projectsos.data.HeartRateConstants.UNKNOWN_BUT_NECESSARY_COMMAND;

public final class RepositoryImpl implements Repository {

    private static final String AUTHENTICATION_KEY = "is_authenticated_before";
    private static final PublishSubject<Boolean> DISCONNECT_TRIGGER_SUBJECT = PublishSubject.create();

    private final RxBleClient mRxBleClient;
    private final SharedPreferences mSharedPreferences;
    private final AbstractConverter<RxBleClient.State, BluetoothState> mBluetoothStateConverter;
    private final AbstractConverter<RxBleConnection.RxBleConnectionState, DeviceState> mDeviceStateConverter;

    private Observable<RxBleClient.State> mStateChangesObservable;
    private Observable<RxBleConnection> mConnectionObservable;
    private RxBleDevice mDevice;

    /**
     * Конструктор репозитория аутентификации
     *
     * @param rxBleClient       клиент для работы с Bluetooth LE
     * @param sharedPreferences хранилище для настроек
     */
    public RepositoryImpl(@NonNull RxBleClient rxBleClient,
                          @NonNull SharedPreferences sharedPreferences) {
        mRxBleClient = checkNotNull(rxBleClient, "RxBleClient is required");
        mSharedPreferences = checkNotNull(sharedPreferences, "SharedPreferences is required");

        mBluetoothStateConverter = new RxBleClientStateToBluetoothStateConverter();
        mDeviceStateConverter = new RxBleConnectionStateToDeviceStateConverter();
    }

    //region Repository

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<DeviceState> traceDeviceState(String macAddress) {
        initDevice(macAddress);

        return mDevice.observeConnectionStateChanges()
                .map(mDeviceStateConverter::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<BluetoothState> traceBluetoothState() {
        initStateChangesObservable();

        return mStateChangesObservable
                .startWith(mRxBleClient.getState())
                .map(mBluetoothStateConverter::convert);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<byte[]> setupNotification(String macAddress) {
        initConnectionObservable(macAddress);

        return mConnectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(AUTH_CHAR))
                .flatMap(observable -> observable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isFirstAuthentication() {
        return !mSharedPreferences.getBoolean(AUTHENTICATION_KEY, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Completable sendSecretKey() {
        byte[] secretKeyWithCommand = ArrayUtils.addAll(new byte[]{AUTH_SEND_SECRET_KEY_COMMAND, AUTH_BYTE}, SECRET_KEY);

        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AUTH_CHAR, secretKeyWithCommand))
                .ignoreElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Completable requestRandomKey() {
        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AUTH_CHAR, new byte[]{AUTH_REQUEST_RANDOM_KEY_COMMAND, AUTH_BYTE}))
                .ignoreElements();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Completable sendEncryptedKey(byte[] randomKeyResponse) {
        byte[] randomKey = Arrays.copyOfRange(randomKeyResponse, 3, 19);
        byte[] encryptedRandomKey = encryptRandomKeyWithSecretKey(randomKey);
        byte[] dataToSend = ArrayUtils.addAll(new byte[]{AUTH_SEND_ENCRYPTED_KEY_COMMAND, AUTH_BYTE}, encryptedRandomKey);

        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AUTH_CHAR, dataToSend))
                .ignoreElements();
    }

    /**
     * Процедура после первой аутентификации устройства
     * Сохраняет флаг о прохождении первой аутентификации и сопрягается с устройством
     *
     * @return завершаемый источник
     */
    @Override
    public Completable afterFirstAuthentication() {
        return Completable.fromAction(() -> {
            if (isFirstAuthentication()) {
                mSharedPreferences
                        .edit()
                        .putBoolean(AUTHENTICATION_KEY, true)
                        .apply();

                mDevice.getBluetoothDevice().createBond();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Completable gracefullyShutdown() {
        return Completable.fromAction(() -> DISCONNECT_TRIGGER_SUBJECT.onNext(true));
    }

    //endregion

    //region HeartRateRepository

    @Override
    public Completable turnOffCurrentHeartMonitorMeasurement() {
        return Completable.mergeArray(
                mConnectionObservable
                        .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(HMC_CHAR, TURN_OFF_ONE_SHOT_MEASUREMENTS_COMMAND))
                        .ignoreElements(),
                mConnectionObservable
                        .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(HMC_CHAR, TURN_OFF_CONTINUOUS_MEASUREMENTS_COMMAND))
                        .ignoreElements()
        );
    }

    @Override
    public Completable enableGyroscopeAndHeartRawData() {
        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(SENSOR_CHAR, ENABLE_GYROSCOPE_AND_HEART_RAW_DATA_COMMAND))
                .ignoreElements();
    }

    @Override
    public Observable<byte[]> setupNotificationForHRM() {
        return mConnectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(HRM_CHAR))
                .flatMap(observable -> observable);
    }

    @Override
    public Completable startContinuousMeasurements() {
        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(HMC_CHAR, START_CONTINUOUS_MEASUREMENTS_COMMAND))
                .ignoreElements();
    }

    @Override
    public Completable sendUnknownButNecessaryCommand() {
        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(SENSOR_CHAR, UNKNOWN_BUT_NECESSARY_COMMAND))
                .ignoreElements();
    }

    @Override
    public Completable ping() {
        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(HMC_CHAR, PING_COMMAND))
                .ignoreElements();
    }

    //endregion

    /**
     * Инициализировать объект устройства
     *
     * @param macAddress MAC адрес
     */
    private void initDevice(String macAddress) {
        if (mDevice == null) {
            mDevice = mRxBleClient.getBleDevice(macAddress);
        }
    }

    /**
     * Инициализировать наблюдение за состоянием
     */
    private void initStateChangesObservable() {
        if (mStateChangesObservable == null) {
            mStateChangesObservable = mRxBleClient
                    .observeStateChanges()
                    .takeUntil(DISCONNECT_TRIGGER_SUBJECT)
                    .compose(ReplayingShare.instance());
        }
    }

    /**
     * Инициализировать наблюдение за подключение с устройством
     *
     * @param macAddress MAC адрес
     */
    private void initConnectionObservable(String macAddress) {
        initDevice(macAddress);

        if (mConnectionObservable == null) {
            mConnectionObservable = mDevice
                    .establishConnection(false)
                    .takeUntil(DISCONNECT_TRIGGER_SUBJECT)
                    .compose(ReplayingShare.instance());
        }
    }

}

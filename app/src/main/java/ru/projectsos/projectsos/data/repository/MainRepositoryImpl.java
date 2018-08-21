package ru.projectsos.projectsos.data.repository;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.jakewharton.rx.ReplayingShare;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleConnection;
import com.polidea.rxandroidble2.RxBleDevice;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.subjects.PublishSubject;
import ru.projectsos.projectsos.domain.MainRepository;
import ru.projectsos.projectsos.models.converter.AbstractConverter;
import ru.projectsos.projectsos.models.converter.RxBleClientStateToBluetoothStateConverter;
import ru.projectsos.projectsos.models.domain.BluetoothState;

import static dagger.internal.Preconditions.checkNotNull;
import static ru.projectsos.projectsos.data.Constants.*;

public final class MainRepositoryImpl implements MainRepository {

    private static final String TAG = "PROJECT_SOS";

    private static final String AUTHENTICATION_KEY = "authentication";
    private static final PublishSubject<Boolean> DISCONNECT_TRIGGER_SUBJECT = PublishSubject.create();

    private final RxBleClient mRxBleClient;
    private final SharedPreferences mSharedPreferences;
    private final AbstractConverter<RxBleClient.State, BluetoothState> mStateConverter;

    private ConnectableObservable<RxBleClient.State> mStateChangesObservable;
    private Observable<RxBleConnection> mConnectionObservable;
    private Disposable mStateChangesDisposable;
    private RxBleDevice mRxBleDevice;

    public MainRepositoryImpl(@NonNull RxBleClient rxBleClient,
                              @NonNull SharedPreferences sharedPreferences) {
        mRxBleClient = checkNotNull(rxBleClient, "RxBleClient is required");
        mSharedPreferences = checkNotNull(sharedPreferences, "SharedPreferences is required");
        mStateConverter = new RxBleClientStateToBluetoothStateConverter();

        mStateChangesObservable = mRxBleClient.observeStateChanges().publish();
        mStateChangesDisposable = mStateChangesObservable.connect();
    }

    @Override
    public Observable<BluetoothState> traceBluetoothState() {
        return mStateChangesObservable
                .startWith(mRxBleClient.getState())
                .map(mStateConverter::convert);
    }

    @Override
    public Completable authenticateDevice(String macAddress) {
        init(macAddress);

        return mConnectionObservable
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(AUTH_CHAR))
                .doOnNext(observable -> {
                    Log.d(TAG, "[SUCCESS] Setup notification");
                    writeSecretKeyCompletable().observeOn(AndroidSchedulers.mainThread()).subscribe();
                })
                .flatMap(observable -> observable)
                .flatMapCompletable(bytes -> {
                    Log.d(TAG, "[SUCCESS] Received notification " + Arrays.toString(bytes));

                    if (bytes[0] == AUTH_RESPONSE && bytes[1] == AUTH_SEND_SECRET_KEY_COMMAND && bytes[2] == AUTH_SUCCESS) {
                        Log.d(TAG, "[SUCCESS] Received notification from write secret key " + Arrays.toString(bytes));
                        return requestRandomKeyCompletable();
                    } else if (bytes[0] == AUTH_RESPONSE && bytes[1] == AUTH_REQUEST_RANDOM_KEY_COMMAND && bytes[2] == AUTH_SUCCESS) {
                        Log.d(TAG, "[SUCCESS] Received notification from request random key " + Arrays.toString(bytes));
                        return authenticateCompletable(bytes);
                    } else if (bytes[0] == AUTH_RESPONSE && bytes[1] == AUTH_SEND_ENCRYPTED_KEY_COMMAND && bytes[2] == AUTH_SUCCESS) {
                        Log.d(TAG, "[SUCCESS] Received notification from authenticate " + Arrays.toString(bytes));
                        return Completable.complete();
                    } else {
                        return Completable.error(new Throwable("UNKNOWN: " + Arrays.toString(bytes)));
                    }
                });
    }

    public void finish() {
        mStateChangesDisposable.dispose();
        DISCONNECT_TRIGGER_SUBJECT.onNext(true);
    }

    private void init(String macAddress) {
        if (mRxBleDevice == null || mConnectionObservable == null) {
            mRxBleDevice = mRxBleClient.getBleDevice(macAddress);

            mConnectionObservable = mRxBleDevice
                    .establishConnection(false)
                    .takeUntil(DISCONNECT_TRIGGER_SUBJECT)
                    .compose(ReplayingShare.instance());
        }
    }

    private boolean isAuthenticatedBefore() {
        return mSharedPreferences.getBoolean(AUTHENTICATION_KEY, false);
    }

    private Completable writeSecretKeyCompletable() {
        byte[] secretKeyWithCommand = ArrayUtils.addAll(new byte[]{AUTH_SEND_SECRET_KEY_COMMAND, AUTH_BYTE}, SECRET_KEY);

        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AUTH_CHAR, secretKeyWithCommand))
                .flatMapCompletable(bytes -> {
                    Log.d(TAG, "[SUCCESS] Write secret key completable " + Arrays.toString(bytes));
                    return Completable.complete();
                });
    }

    private Completable requestRandomKeyCompletable() {
        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AUTH_CHAR, new byte[]{AUTH_REQUEST_RANDOM_KEY_COMMAND, AUTH_BYTE}))
                .flatMapCompletable(bytes -> {
                    Log.d(TAG, "[SUCCESS] Request random key " + Arrays.toString(bytes));
                    return Completable.complete();
                });
    }

    private Completable authenticateCompletable(byte[] randomKeyResponse) {
        byte[] randomKey = Arrays.copyOfRange(randomKeyResponse, 3, 19);
        byte[] encryptedRandomKey = encryptRandomKeyWithSecretKey(randomKey);
        byte[] dataToSend = ArrayUtils.addAll(new byte[]{AUTH_SEND_ENCRYPTED_KEY_COMMAND, AUTH_BYTE}, encryptedRandomKey);

        return mConnectionObservable
                .flatMapSingle(rxBleConnection -> rxBleConnection.writeCharacteristic(AUTH_CHAR, dataToSend))
                .flatMapCompletable(bytes -> {
                    Log.d(TAG, "[SUCCESS] Authenticate " + Arrays.toString(bytes));
                    return Completable.complete();
                })
                .doOnComplete(() -> {
                    mSharedPreferences
                            .edit()
                            .putBoolean(AUTHENTICATION_KEY, true)
                            .apply();

                    mRxBleDevice.getBluetoothDevice().createBond();
                });
    }

}

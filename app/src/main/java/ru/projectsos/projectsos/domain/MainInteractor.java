package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.models.domain.BluetoothState;
import ru.projectsos.projectsos.models.domain.DeviceState;

import static dagger.internal.Preconditions.checkNotNull;

public final class MainInteractor {

    private final AuthenticationRepository mRepository;

    /**
     * Конструктор
     *
     * @param authenticationRepository репозиторий для аутентификации
     */
    public MainInteractor(AuthenticationRepository authenticationRepository) {
        mRepository = checkNotNull(authenticationRepository, "AuthenticationRepository is required");
    }

    /**
     * Следить за состоянием Bluetooth
     *
     * @return возвращает горячий источник
     */
    public Observable<BluetoothState> traceBluetoothState() {
        return mRepository.traceBluetoothState();
    }

    /**
     * Аутентифицировать устройство
     *
     * @return возвращает завершаемый источник
     */
    public Completable authenticateDevice(String macAddress) {
        return mRepository.authenticateDevice(macAddress);
    }

    /**
     * Следить за состоянием устройства
     *
     * @param macAddress MAC адрес
     * @return возвращает горячий источник
     */
    public Observable<DeviceState> traceDeviceState(String macAddress) {
        return mRepository.traceDeviceState(macAddress);
    }

    /**
     * Правильно выключиться
     * Отписаться от горячих источников
     *
     * @return возвращает завершаемый источник
     */
    public Completable gracefullyShutdown() {
        return mRepository.gracefullyShutdown();
    }

}

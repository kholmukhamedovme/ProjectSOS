package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.models.domain.BluetoothState;

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
     * Правильно выключиться
     * Отписаться от горячих источников
     */
    public void gracefullyShutdown() {
        mRepository.gracefullyShutdown();
    }

}

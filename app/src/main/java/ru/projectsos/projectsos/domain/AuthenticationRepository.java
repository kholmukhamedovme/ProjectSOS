package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.models.domain.BluetoothState;

public interface AuthenticationRepository {

    /**
     * Следить за состоянием Bluetooth
     *
     * @return возвращает горячий источник
     */
    Observable<BluetoothState> traceBluetoothState();

    /**
     * Аутентифицировать устройство
     *
     * @return возвращает завершаемый источник
     */
    Completable authenticateDevice(String macAddress);

    /**
     * Правильно выключиться
     * Отписаться от горячих источников
     */
    void gracefullyShutdown();

}

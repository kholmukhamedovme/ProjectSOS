package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.models.domain.BluetoothState;
import ru.projectsos.projectsos.models.domain.DeviceState;

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
     * Следить за состоянием устройства
     *
     * @return возвращает горячий источник
     */
    Observable<DeviceState> traceDeviceState(String macAddress);

    /**
     * Правильно выключиться
     * Завершить источники
     *
     * @return возвращает завершаемый источник
     */
    Completable gracefullyShutdown();

}

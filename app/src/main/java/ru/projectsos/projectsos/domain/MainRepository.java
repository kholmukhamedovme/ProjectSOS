package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.models.domain.BluetoothState;

public interface MainRepository {

    /**
     * Следить за состоянием Bluetooth
     *
     * @return возвращает горячий источник
     */
    Observable<BluetoothState> traceBluetoothState();

    /**
     * Авторизовать устройство
     *
     * @return возвращает завершаемый источник
     */
    Completable authenticateDevice(String macAddress);

}

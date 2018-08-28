package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.data.AuthConstants;
import ru.projectsos.projectsos.models.domain.BluetoothState;
import ru.projectsos.projectsos.models.domain.DeviceState;

public interface AuthRepository {

    /**
     * Следить за состоянием устройства
     *
     * @return возвращает горячий источник
     */
    Observable<DeviceState> traceDeviceState(String macAddress);

    /**
     * Следить за состоянием Bluetooth
     *
     * @return возвращает горячий источник
     */
    Observable<BluetoothState> traceBluetoothState();

    /**
     * Подписаться на уведомления
     *
     * @param macAddress MAC адрес устройства
     * @return возвращает горячий источник
     */
    Observable<byte[]> setupNotification(String macAddress);

    /**
     * Проверка на первую аутентификацию устройства
     *
     * @return возвращает {@code true} если это первая аутентификация, иначе {@code false}
     */
    boolean isFirstAuthentication();

    /**
     * Отправить секретный ключ (см. шаг #2)
     *
     * @return возвращает завершаемый источник
     * @see AuthConstants
     */
    Completable sendSecretKey();

    /**
     * Запросить случайный ключ (см. шаг #3)
     *
     * @return возвращает завершаемый источник
     * @see AuthConstants
     */
    Completable requestRandomKey();

    /**
     * Отправка зашифрованного ключа (см. шаг #4)
     *
     * @param randomKeyResponse уведомление со случайным ключом
     * @return возвращает завершаемый источник
     * @see AuthConstants
     */
    Completable sendEncryptedKey(byte[] randomKeyResponse);

    /**
     * Процедура после первой аутентификации устройства
     * Сохраняет флаг о прохождении первой аутентификации и сопрягается с устройством
     *
     * @return возвращает завершаемый источник
     */
    Completable afterFirstAuthentication();

    /**
     * Правильно выключиться
     * Завершить источники
     *
     * @return возвращает завершаемый источник
     */
    Completable gracefullyShutdown();

}

package ru.projectsos.projectsos.domain;

import io.reactivex.Completable;
import io.reactivex.Observable;
import ru.projectsos.projectsos.data.AuthConstants;
import ru.projectsos.projectsos.models.domain.BluetoothState;
import ru.projectsos.projectsos.models.domain.DeviceState;

import static dagger.internal.Preconditions.checkNotNull;

public final class MainInteractor {

    private final AuthRepository mRepository;

    /**
     * Конструктор
     *
     * @param authRepository репозиторий для аутентификации
     */
    public MainInteractor(AuthRepository authRepository) {
        mRepository = checkNotNull(authRepository, "AuthRepository is required");
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
     * Следить за состоянием Bluetooth
     *
     * @return возвращает горячий источник
     */
    public Observable<BluetoothState> traceBluetoothState() {
        return mRepository.traceBluetoothState();
    }

    /**
     * Подписаться на уведомления
     *
     * @param macAddress MAC адрес устройства
     * @return возвращает горячий источник
     */
    public Observable<byte[]> setupNotification(String macAddress) {
        return mRepository.setupNotification(macAddress);
    }

    /**
     * Проверка на первую аутентификацию устройства
     *
     * @return возвращает {@code true} если это первая аутентификация, иначе {@code false}
     */
    public boolean isFirstAuthentication() {
        return mRepository.isFirstAuthentication();
    }

    /**
     * Отправить секретный ключ (см. шаг #2)
     *
     * @return возвращает завершаемый источник
     * @see AuthConstants
     */
    public Completable sendSecretKey() {
        return mRepository.sendSecretKey();
    }

    /**
     * Запросить случайный ключ (см. шаг #3)
     *
     * @return возвращает завершаемый источник
     * @see AuthConstants
     */
    public Completable requestRandomKey() {
        return mRepository.requestRandomKey();
    }

    /**
     * Отправка зашифрованного ключа (см. шаг #4)
     *
     * @param randomKeyResponse уведомление со случайным ключом
     * @return возвращает завершаемый источник
     * @see AuthConstants
     */
    public Completable sendEncryptedKey(byte[] randomKeyResponse) {
        return mRepository.sendEncryptedKey(randomKeyResponse);
    }

    /**
     * Процедура после первой аутентификации устройства
     * Сохраняет флаг о прохождении первой аутентификации и сопрягается с устройством
     *
     * @return возвращает завершаемый источник
     */
    public Completable afterFirstAuthentication() {
        return mRepository.afterFirstAuthentication();
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

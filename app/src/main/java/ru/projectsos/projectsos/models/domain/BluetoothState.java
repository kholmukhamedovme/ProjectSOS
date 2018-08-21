package ru.projectsos.projectsos.models.domain;

public enum BluetoothState {

    /**
     * Всё готово для работы
     */
    READY,

    /**
     * Не дан доступ к геолокации пользователя.
     * Функции поиска и подключения к устройствам будут недоступны. Используется в API >= 23.
     */
    LOCATION_PERMISSION_NOT_GRANTED,

    /**
     * Выключены геолокационные сервисы или, другими словами, выключен GPS
     * Функция поиска будет недоступна. Используется в API >= 23.
     */
    LOCATION_SERVICES_NOT_ENABLED,

    /**
     * Bluetooth адаптер выключен.
     * Функции поиска и подключения к устройствам будут недоступны.
     */
    BLUETOOTH_NOT_ENABLED,

    /**
     * Отсутствует Bluetooth адаптер в смартфоне или ОС.
     */
    BLUETOOTH_NOT_AVAILABLE

}

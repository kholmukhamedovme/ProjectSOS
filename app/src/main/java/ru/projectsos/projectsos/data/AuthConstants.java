package ru.projectsos.projectsos.data;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * <h1>Класс для хранения специфичных для устройства констант</h1>
 *
 * <h2>Первая авторизация SMARTBAND'а состоит из четырех шагов:</h2>
 * <ol>
 * <li>Подписка на уведомления;</li>
 * <li>Отправка секретного ключа;</li>
 * <li>Запрашивание случайного ключа;</li>
 * <li>Отправка зашифрованного ключа.</li>
 * </ol>
 *
 * <h3>Рассмотрим каждый шаг по отдельности</h3>
 *
 * <h4>Подписка на уведомления</h4>
 * Подписываемся на уведомления характеристики {@link #AUTH_CHAR} устройства. Это можно сделать с помощью:
 * <ul>
 * <li>Android SDK ({@link android.bluetooth.BluetoothGatt#setCharacteristicNotification(BluetoothGattCharacteristic, boolean)});</li>
 * <li>Библиотеки RxAndroidBle ({@link com.polidea.rxandroidble2.RxBleConnection#setupNotification(UUID)}).</li>
 * </ul>
 *
 * <h4>Отправка секретного ключа</h4>
 * Отправляем на устройство по характеристике {@link #AUTH_CHAR} массив из 18 байтов:
 * {@link #AUTH_SEND_SECRET_KEY_COMMAND}, {@link #AUTH_BYTE} и {@link #SECRET_KEY}. В ответ придет уведомление,
 * состоящее из 3 бит: первый бит равен {@link #AUTH_RESPONSE}, второй — {@link #AUTH_SEND_SECRET_KEY_COMMAND}
 * и третий — {@link #AUTH_SUCCESS} или {@link #AUTH_FAIL}, в зависимости от результата.
 *
 * <h4>Запрашивание случайного ключа</h4>
 * Отправляем на устройство по характеристике {@link #AUTH_CHAR} массив из 2 байтов:
 * {@link #AUTH_REQUEST_RANDOM_KEY_COMMAND} и {@link #AUTH_BYTE}. В ответ придет уведомление, состоящее из 19 байтов:
 * {@link #AUTH_RESPONSE}, {@link #AUTH_REQUEST_RANDOM_KEY_COMMAND}, {@link #AUTH_SUCCESS} или {@link #AUTH_FAIL},
 * в зависимости от результата и 16 байт, которые и есть тот самый случайный ключ, который мы запросили
 *
 * <h4>Отправка зашифрованного ключа</h4>
 * Отправляем на устройство по характеристике {@link #AUTH_CHAR} массив из 18 байтов:
 * {@link #AUTH_SEND_ENCRYPTED_KEY_COMMAND}, {@link #AUTH_BYTE} и 16 байт зашифрованного ключа, которые мы получаем
 * через шифрование {@code AES/ECB/NoPadding}. На вход шифратора подаем случайный ключ из шага #4 в качестве шифруемого
 * сообщения и {@link #SECRET_KEY} в качестве шифра. Шифровать можно с помощью {@link javax.crypto.Cipher}. В ответ
 * придет уведомление, состоящее из 3 байтов: {@link #AUTH_RESPONSE}, {@link #AUTH_SEND_ENCRYPTED_KEY_COMMAND} и
 * {@link #AUTH_SUCCESS} или {@link #AUTH_FAIL}, в зависимости от результата
 *
 * <h3>Последующие авторизации</h3>
 * Все последующие авторизации отличаются лишь отсутствием шага #2.
 */
public final class AuthConstants {

    /**
     * Секретный ключ
     */
    public static final byte[] SECRET_KEY = new byte[]{0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45};

    /**
     * Уникальный универсальный идентификатор характеристики авторизации
     */
    public static final UUID AUTH_CHAR = UUID.fromString("00000009-0000-3512-2118-0009af100700");

    /**
     * Команда для отправки секретного ключа
     */
    public static final byte AUTH_SEND_SECRET_KEY_COMMAND = 0x01;

    /**
     * Команда для запроса случайного ключа
     */
    public static final byte AUTH_REQUEST_RANDOM_KEY_COMMAND = 0x02;

    /**
     * Команда для отправки зашифрованного ключа
     */
    public static final byte AUTH_SEND_ENCRYPTED_KEY_COMMAND = 0x03;

    /**
     * Бит уведомления
     */
    public static final byte AUTH_RESPONSE = 0x10;

    /**
     * Бит успешности команды
     */
    public static final byte AUTH_SUCCESS = 0x01;

    /**
     * Бит неудачности команды
     */
    public static final byte AUTH_FAIL = 0x04;

    /**
     * Бит непонятно чего :D
     * Работает даже если указать {@code 0x00}
     */
    public static final byte AUTH_BYTE = 0x00;

    /**
     * Зашифровать случайный ключ секретным ключом
     *
     * @param randomKey уведомление из шага #3
     * @return зашифрованный ключ для шага #4
     */
    public static byte[] encryptRandomKeyWithSecretKey(byte[] randomKey) {
        try {
            @SuppressLint("GetInstance")
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            return cipher.doFinal(randomKey);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

}

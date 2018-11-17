package ru.projectsos.projectsos.data;

import java.util.UUID;

public final class HeartRateConstants {

    public static final UUID HMC_CHAR = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");

    public static final UUID SENSOR_CHAR = UUID.fromString("00000001-0000-3512-2118-0009af100700");

    public static final UUID HRM_CHAR = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");

    public static final byte[] TURN_OFF_ONE_SHOT_MEASUREMENTS_COMMAND = new byte[]{0x15, 0x02, 0x00};

    public static final byte[] TURN_OFF_CONTINUOUS_MEASUREMENTS_COMMAND = new byte[]{0x15, 0x01, 0x00};

    public static final byte[] START_CONTINUOUS_MEASUREMENTS_COMMAND = new byte[]{0x15, 0x01, 0x01};

    public static final byte[] ENABLE_GYROSCOPE_AND_HEART_RAW_DATA_COMMAND = new byte[]{0x01, 0x03, 0x19};

    public static final byte[] UNKNOWN_BUT_NECESSARY_COMMAND = new byte[]{0x02};

    public static final byte[] PING_COMMAND = new byte[]{0x16};

}

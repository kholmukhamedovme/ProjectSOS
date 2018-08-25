package ru.projectsos.projectsos.models.converter;

import com.polidea.rxandroidble2.RxBleConnection;

import ru.projectsos.projectsos.models.domain.DeviceState;

public final class RxBleConnectionStateToDeviceStateConverter
        extends AbstractConverter<RxBleConnection.RxBleConnectionState, DeviceState> {

    @Override
    public DeviceState convert(RxBleConnection.RxBleConnectionState state) {
        switch (state) {
            case CONNECTING:
                return DeviceState.CONNECTING;
            case CONNECTED:
                return DeviceState.CONNECTED;
            case DISCONNECTING:
                return DeviceState.DISCONNECTING;
            case DISCONNECTED:
                return DeviceState.DISCONNECTED;
        }

        return null;
    }

}

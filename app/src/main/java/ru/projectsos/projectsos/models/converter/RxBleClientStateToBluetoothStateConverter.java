package ru.projectsos.projectsos.models.converter;

import com.polidea.rxandroidble2.RxBleClient;

import ru.projectsos.projectsos.models.domain.BluetoothState;

public final class RxBleClientStateToBluetoothStateConverter
        extends AbstractConverter<RxBleClient.State, BluetoothState> {

    @Override
    public BluetoothState convert(RxBleClient.State state) {
        switch (state) {
            case READY:
                return BluetoothState.READY;
            case LOCATION_PERMISSION_NOT_GRANTED:
                return BluetoothState.LOCATION_PERMISSION_NOT_GRANTED;
            case LOCATION_SERVICES_NOT_ENABLED:
                return BluetoothState.LOCATION_SERVICES_NOT_ENABLED;
            case BLUETOOTH_NOT_ENABLED:
                return BluetoothState.BLUETOOTH_NOT_ENABLED;
            case BLUETOOTH_NOT_AVAILABLE:
                return BluetoothState.BLUETOOTH_NOT_AVAILABLE;
        }

        return null;
    }

}

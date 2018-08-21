package ru.projectsos.projectsos.presentation.view;

import com.arellomobile.mvp.MvpView;

public interface MainView extends MvpView {

    /**
     * Inform user that his smart-phone doesn't have Bluetooth
     */
    void informNoBluetoothAvailable();

    /**
     * Inform user to enable Bluetooth
     */
    void informEnableBluetooth();

    /**
     * Inform user to grant permission to access location
     */
    void informGrantLocationPermission();

    /**
     * Inform user to enable location services
     */
    void informEnableLocationServices();

}

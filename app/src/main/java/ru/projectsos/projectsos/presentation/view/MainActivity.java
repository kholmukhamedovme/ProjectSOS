package ru.projectsos.projectsos.presentation.view;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;

import javax.inject.Inject;

import ru.projectsos.projectsos.App;
import ru.projectsos.projectsos.R;
import ru.projectsos.projectsos.presentation.presenter.MainPresenter;

public class MainActivity extends MvpAppCompatActivity implements MainView {

    public static final String EXTRA_MAC_ADDRESS = "extra_mac_address";

    private static final int ENABLE_BLUETOOTH_REQUEST_CODE = 1;
    private static final int GRANT_LOCATION_PERMISSION_REQUEST_CODE = 2;
    private static final int ENABLE_LOCATION_SERVICES_REQUEST_CODE = 3;

    @Inject
    @InjectPresenter
    MainPresenter mPresenter;

    /**
     * Provide presenter presented by Dagger to Moxy
     *
     * @return presenter as {@link MainPresenter}
     */
    @ProvidePresenter
    MainPresenter providePresenter() {
        return mPresenter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        App.getMainComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //region FIXME: Make launcher activity that gets MAC address of SMARTBAND device dynamically
        getIntent().putExtra(EXTRA_MAC_ADDRESS, "EA:8F:43:E4:8B:AE");
        //endregion

        String macAddress = getIntent().getStringExtra(EXTRA_MAC_ADDRESS);
        mPresenter.setMacAddress(macAddress);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ENABLE_BLUETOOTH_REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    showAlertDialog(
                            R.string.enable_bluetooth_canceled_dialog_title,
                            R.string.enable_bluetooth_canceled_dialog_message,
                            R.string.enable_bluetooth_canceled_dialog_button,
                            (dialog, which) -> finish()
                    );
                }
                break;
            case ENABLE_LOCATION_SERVICES_REQUEST_CODE:
                if (resultCode != RESULT_OK) {
                    showAlertDialog(
                            R.string.enable_location_services_canceled_dialog_title,
                            R.string.enable_location_services_canceled_dialog_message,
                            R.string.enable_location_services_canceled_dialog_button,
                            (dialog, which) -> finish()
                    );
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case GRANT_LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showAlertDialog(
                            R.string.grant_location_permission_denied_dialog_title,
                            R.string.grant_location_permission_denied_dialog_message,
                            R.string.grant_location_permission_denied_dialog_button,
                            (dialog, which) -> finish()
                    );
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    //region MainView

    /**
     * {@inheritDoc}
     */
    @Override
    public void informNoBluetoothAvailable() {
        showAlertDialog(
                R.string.no_bluetooth_available_dialog_title,
                R.string.no_bluetooth_available_dialog_message,
                R.string.no_bluetooth_available_dialog_button,
                (dialog, which) -> finish()
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void informEnableBluetooth() {
        showAlertDialog(
                R.string.enable_bluetooth_dialog_title,
                R.string.enable_bluetooth_dialog_message,
                R.string.enable_bluetooth_dialog_button,
                (dialog, which) -> startActivityForResult(
                        new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
                        ENABLE_BLUETOOTH_REQUEST_CODE
                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void informGrantLocationPermission() {
        showAlertDialog(
                R.string.grant_location_permission_dialog_title,
                R.string.grant_location_permission_dialog_message,
                R.string.grant_location_permission_dialog_button,
                (dialog, which) -> ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        GRANT_LOCATION_PERMISSION_REQUEST_CODE
                )
        );

        getSystemService(LOCATION_SERVICE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void informEnableLocationServices() {
        showAlertDialog(
                R.string.enable_location_services_dialog_title,
                R.string.enable_location_services_dialog_message,
                R.string.enable_location_services_dialog_button,
                (dialog, which) -> startActivityForResult(
                        new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                        ENABLE_LOCATION_SERVICES_REQUEST_CODE
                )
        );
    }

    //endregion

    /**
     * Show alert dialog
     *
     * @param title          title string resource
     * @param message        message string resource
     * @param buttonText     button text string resource
     * @param buttonListener button listener on click event
     */
    private void showAlertDialog(@StringRes int title,
                                 @StringRes int message,
                                 @StringRes int buttonText,
                                 DialogInterface.OnClickListener buttonListener) {
        new AlertDialog.Builder(this)
                .setNeutralButton(buttonText, buttonListener)
                .setCancelable(false)
                .setMessage(message)
                .setTitle(title)
                .create()
                .show();
    }

}

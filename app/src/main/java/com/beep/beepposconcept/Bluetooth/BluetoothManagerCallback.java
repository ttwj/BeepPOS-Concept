package com.beep.beepposconcept.Bluetooth;


import com.beep.beepposconcept.CEPAS.CallbackCEPASCard;

/**
 * Created by ttwj on 5/12/16.
 */



public interface BluetoothManagerCallback {

    public enum BluetoothManagerError {
        CARD_NOT_FOUND,
        INVALID_CARD,

        FAILED_DETECT_DEVICE,
        DEVICE_FAILED_TO_CONNECT
    }

    public enum BluetoothManagerStatus {
        ESTABLISHING_CONNECTION,
        DEVICE_CONNECTED
    }

    public void onReceiveCEPASCard(CallbackCEPASCard card);
    public void onError(BluetoothManagerError error);
    public void onStatus(BluetoothManagerStatus status);

}


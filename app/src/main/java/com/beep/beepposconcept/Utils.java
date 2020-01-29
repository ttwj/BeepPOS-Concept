package com.beep.beepposconcept;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by ttwj on 4/12/16.
 */

public class Utils {

    public static boolean checkBluetoothConnection(){
            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            return (bluetoothAdapter != null && bluetoothAdapter.isEnabled());
        }

}


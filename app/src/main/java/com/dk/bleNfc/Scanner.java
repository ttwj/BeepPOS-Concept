package com.dk.bleNfc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

//import com.huang.lochy.ble_nfc_demo.R;

/**
 * Created by lochy on 16/1/19.
 */
public class Scanner {
    public BluetoothAdapter mBAdapter = BluetoothAdapter.getDefaultAdapter();
    public static List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
    private boolean mScanning;

    private Context mContext;
    private ScannerCallback mScannerCallback = null;

    public Scanner(Context context, ScannerCallback callback) {
        mContext = context;
        mScannerCallback = callback;
        initialize();
    }

    public void startScan() {
        deviceList.removeAll(deviceList);
        scanLeDevice(true, 0);
    }

    public void startScan(long scanPeriod) {
        scanLeDevice(true, scanPeriod);
    }

    public void stopScan() {
        scanLeDevice(false, 0);
        mScannerCallback.onScanDeviceStopped();
    }

    public boolean isScanning() {
        return mScanning;
    }

    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
    {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            //搜到设备回调
            deviceList.add(device);
            mScannerCallback.onReceiveScanDevice(device, rssi, scanRecord);
        }
    };

    private boolean initialize() {
        //检测手机是否支持BLE
        if(!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(mContext, "此手机不支持BLE", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 判断蓝牙是否打开
        if ((mBAdapter != null) && !mBAdapter.isEnabled()) {
            mBAdapter.enable();
        }

        return true;
    }

    private Handler mHandler = new Handler();
    //搜索BLE设备
    private void scanLeDevice(final boolean enable, final long scanPeriod) {
        if (enable) {
            if (scanPeriod > 0) {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mScanning) {
                            mScanning = false;
                            mBAdapter.stopLeScan(mLeScanCallback);
                            mBAdapter.cancelDiscovery();
                            //停止搜索回调
                            mScannerCallback.onScanDeviceStopped();
                        }
                    }
                }, scanPeriod);
            }

            mScanning = true;
            mBAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBAdapter.stopLeScan(mLeScanCallback);
        }
    }
}

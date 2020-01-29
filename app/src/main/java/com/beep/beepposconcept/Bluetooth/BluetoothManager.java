package com.beep.beepposconcept.Bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.beep.beepposconcept.CEPAS.CEPASPurse;
import com.beep.beepposconcept.CEPAS.CallbackCEPASCard;
import com.dk.bleNfc.DeviceManager;
import com.dk.bleNfc.DeviceManagerCallback;
import com.dk.bleNfc.Scanner;
import com.dk.bleNfc.ScannerCallback;
import com.dk.bleNfc.card.Iso14443bCard;

import static android.support.v4.app.ActivityCompat.requestPermissions;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Created by ttwj on 5/12/16.
 */

public class BluetoothManager {
    public DeviceManager deviceManager;
    private Scanner mScanner;
    private Context mContext;
    private Activity parentActivity;

    private BluetoothDevice mNearestBle = null;
    private int lastRssi = -100;
    

    private static final String TAG = BluetoothManager.class.getClass().getSimpleName();


    private BluetoothManagerCallback listener;
    private CallbackCEPASCard.CardStatusListener cardStatusListener;

    public BluetoothManager(BluetoothManagerCallback listener, Context context, Activity parentActivity) {
        this.parentActivity = parentActivity;
        mContext = context;
        this.listener = listener;
        mScanner = new Scanner(context, scannerCallback);
        deviceManager = new DeviceManager(context);
        deviceManager.setCallBack(deviceManagerCallback);
        requestSinglePermission();


    }

    private static final int REQUEST_LOCATION = 1503;

    public void requestSinglePermission() {
        String locationPermission = Manifest.permission.ACCESS_FINE_LOCATION;
        int hasPermission = checkSelfPermission(mContext, locationPermission);
        String[] permissions = new String[] { locationPermission, Manifest.permission.ACCESS_COARSE_LOCATION };
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(parentActivity, permissions, REQUEST_LOCATION);
        } else {
            // Phew - we already have permission!
        }
    }

    public void set_CEPASCallbackListener(Fragment fragment){
        if(fragment instanceof CallbackCEPASCard.CardStatusListener){
            cardStatusListener = (CallbackCEPASCard.CardStatusListener) fragment;
        }
    }

    public CallbackCEPASCard cepasCard;


    public interface getCEPASCardListener {
        public void onReceive(CallbackCEPASCard card);
        public void onCardNotFound();
    }

    public void getCEPASCard(final getCEPASCardListener callback) {
        Log.d(TAG, "Establishing connection to card..");
        deviceManager.requestRfmSearchCard((byte) 0x00, new DeviceManager.onReceiveRfnSearchCardListener() {
            @Override
            public void onReceiveRfnSearchCard(final boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {
                deviceManager.mOnReceiveRfnSearchCardListener = null;
                if (cardType == DeviceManager.CARD_TYPE_ISO4443_B) {
                    Log.d(TAG, "Detected Type B card!");
                    Iso14443bCard card = (Iso14443bCard) deviceManager.getCard();
                    if (card != null) {
                        Log.d(TAG, "Detected card ok");
                        cepasCard = new CallbackCEPASCard(card);
                        //cardStatusListener.onCardReady();
                        listener.onReceiveCEPASCard(cepasCard);
                        callback.onReceive(cepasCard); //TODO remove unncessary callbacks

                    }
                    else {
                        callback.onCardNotFound();
                        listener.onError(BluetoothManagerCallback.BluetoothManagerError.CARD_NOT_FOUND);
                    }
                }
                else {
                    callback.onCardNotFound();
                    listener.onError(BluetoothManagerCallback.BluetoothManagerError.CARD_NOT_FOUND);
                }
            }
        });

    }

    public void read_card() {

        System.out.println("Activity发送寻卡/激活指令");

        deviceManager.requestRfmSearchCard((byte) 0x00, new DeviceManager.onReceiveRfnSearchCardListener() {
            @Override
            public void onReceiveRfnSearchCard(final boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {
                deviceManager.mOnReceiveRfnSearchCardListener = null;
                if (cardType == DeviceManager.CARD_TYPE_ISO4443_B) {
                    Log.d("MainActivity", "Detected Type B card! Could it be a CEPAS?");
                    Iso14443bCard card = (Iso14443bCard) deviceManager.getCard();
                    if (card != null) {
                        Log.d(TAG, "Detected card ok");
                        cepasCard = new CallbackCEPASCard(card);
                        cardStatusListener.onCardReady();
                        listener.onReceiveCEPASCard(cepasCard);


                    }
                    else {

                        listener.onError(BluetoothManagerCallback.BluetoothManagerError.CARD_NOT_FOUND);
                    }
                }
                else {

                    listener.onError(BluetoothManagerCallback.BluetoothManagerError.INVALID_CARD);
                }
            }
        });

    }

    public void searchAndConnect() {

        if (deviceManager.isConnection()) {
            deviceManager.requestDisConnectDevice();
            return;
        }

        if (!mScanner.isScanning()) {
            mScanner.startScan(0);
            mNearestBle = null;
            lastRssi = -100;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int searchCnt = 0;
                    while ((mNearestBle == null) && (searchCnt < 5000) && (mScanner.isScanning())) {
                        searchCnt++;
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mScanner.stopScan();
                    if (mNearestBle != null) {
                        mScanner.stopScan();
                        Log.d(TAG, "Found device, establishing connection..");
                        listener.onStatus(BluetoothManagerCallback.BluetoothManagerStatus.ESTABLISHING_CONNECTION);
                        deviceManager.requestConnectBleDevice(mNearestBle.getAddress());
                    } else {
                        Log.d(TAG, "Warning: Failed to find device!");
                        listener.onError(BluetoothManagerCallback.BluetoothManagerError.FAILED_DETECT_DEVICE);
                    }
                }
            }).start();
        }
    }


    //Scanner 回调
    private ScannerCallback scannerCallback = new ScannerCallback() {
        @Override
        public void onReceiveScanDevice(BluetoothDevice device, int rssi, byte[] scanRecord) {
            super.onReceiveScanDevice(device, rssi, scanRecord);
            System.out.println("Activity搜到设备：" + device.getName() + "信号强度：" + rssi);
            //搜索蓝牙设备并记录信号强度最强的设备
            if ((device.getName() != null) && (device.getName().contains("UNISMES") || device.getName().contains("BLE_NFC"))) {
                if (mNearestBle != null) {
                    if (rssi > lastRssi) {
                        mNearestBle = device;
                    }
                } else {
                    mNearestBle = device;
                    lastRssi = rssi;
                }
            }
        }

        @Override
        public void onScanDeviceStopped() {
            super.onScanDeviceStopped();
        }
    };

    private DeviceManagerCallback deviceManagerCallback = new DeviceManagerCallback() {


        @Override
        public void onReceiveConnectBtDevice(boolean blnIsConnectSuc) {
            super.onReceiveConnectBtDevice(blnIsConnectSuc);

            if (blnIsConnectSuc) {
                listener.onStatus(BluetoothManagerCallback.BluetoothManagerStatus.DEVICE_CONNECTED);
                System.out.println("Activity设备连接成功");
                Log.d(TAG, "SDK版本：" + DeviceManager.SDK_VERSIONS + "\r\n");

                //连接上后延时500ms后再开始发指令
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                deviceManager.requestVersionsDevice(new DeviceManager.onReceiveVersionsDeviceListener() {
                    @Override
                    public void onReceiveVersionsDevice(byte versions) {
                        Log.d(TAG, "设备版本:" + String.format("%02x", versions) + "\r\n");

                        deviceManager.requestBatteryVoltageDevice(new DeviceManager.onReceiveBatteryVoltageDeviceListener() {
                            @Override
                            public void onReceiveBatteryVoltageDevice(double voltage) {
                                Log.d(TAG, "设备电池电压:" + String.format("%.2f", voltage) + "\r\n");
                                if (voltage < 3.4) {
                                    Log.d(TAG, "设备电池电量低，请及时充电！");
                                } else {
                                    Log.d(TAG, "设备电池电量充足！");
                                }

                            }
                        });
                    }
                });
            }
            else {
                listener.onError(BluetoothManagerCallback.BluetoothManagerError.DEVICE_FAILED_TO_CONNECT);
            }
        }

        @Override
        public void onReceiveDisConnectDevice(boolean blnIsDisConnectDevice) {
            super.onReceiveDisConnectDevice(blnIsDisConnectDevice);
            System.out.println("Activity设备断开链接");

            Log.d(TAG, "设备断开链接!");

        }

        @Override
        public void onReceiveConnectionStatus(boolean blnIsConnection) {
            super.onReceiveConnectionStatus(blnIsConnection);
            System.out.println("Activity设备链接状态回调");
        }

        @Override
        public void onReceiveInitCiphy(boolean blnIsInitSuc) {
            super.onReceiveInitCiphy(blnIsInitSuc);
        }

        @Override
        public void onReceiveDeviceAuth(byte[] authData) {
            super.onReceiveDeviceAuth(authData);
        }

        @Override
        public void onReceiveRfnSearchCard(boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS) {
            Log.d("MainActivity", "Detected card?! " + cardType);
            super.onReceiveRfnSearchCard(blnIsSus, cardType, bytCardSn, bytCarATS);
            if (!blnIsSus) {
                return;
            }
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < bytCardSn.length; i++) {
                stringBuffer.append(String.format("%02x", bytCardSn[i]));
            }

            StringBuffer stringBuffer1 = new StringBuffer();
            for (int i = 0; i < bytCarATS.length; i++) {
                stringBuffer1.append(String.format("%02x", bytCarATS[i]));
            }
            System.out.println("Activity接收到激活卡片回调：UID->" + stringBuffer + " ATS->" + stringBuffer1);
        }

        @Override
        public void onReceiveRfmSentApduCmd(byte[] bytApduRtnData) {
            super.onReceiveRfmSentApduCmd(bytApduRtnData);

            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < bytApduRtnData.length; i++) {
                stringBuffer.append(String.format("%02x", bytApduRtnData[i]));
            }
            System.out.println("Activity接收到APDU回调：" + stringBuffer);
        }

        @Override
        public void onReceiveRfmClose(boolean blnIsCloseSuc) {
            super.onReceiveRfmClose(blnIsCloseSuc);
        }
    };

}

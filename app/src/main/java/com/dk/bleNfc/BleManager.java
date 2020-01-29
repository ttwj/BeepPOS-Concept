package com.dk.bleNfc;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lochy on 15/10/7.
 */
public class BleManager {
    private final static String TAG = "Lochy";
    private Context mContext;
    private BluetoothAdapter mBAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGatt mBluetoothGatt;
    private BluetoothDevice currentDevice = null;
    private BluetoothGattCharacteristic mcharacteristic = null;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 10000;
    private boolean mScanning;
    private Handler mHandler = new Handler();

    public int mConnectionState = STATE_DISCONNECTED;
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public onReceiveDataListener mOnReceiveDataListener;
    public onBleConnectListener mOnBleConnectListener;
    public onBleDisconnectListener mOnBleDisconnectListener;
    public onBleReadListener monBleReadListener;
    public onWriteSuccessListener mOnWriteSuccessListener;

    public BleManager(Context c) {
        mContext = c;
    }

    public interface onReceiveDataListener {
        public void OnReceiverData(byte[] data);
    }

    public interface onBleConnectListener {
        public void onBleConnect(boolean isConnectSucceed);
    }

    public interface onBleDisconnectListener {
        public void onBleDisconnect();
    }

    public interface onBleReadListener {
        public void onBleRead(byte[] value);
    }


    public interface onWriteSuccessListener {
        public void onWriteSuccess();
    }

    public void setOnReceiveDataListener(onReceiveDataListener l) {
        this.mOnReceiveDataListener = l;
    }

    public void setOnBleConnectListener(onBleConnectListener l) {
        this.mOnBleConnectListener = l;
    }

    public void setOnBledisconnectListener(onBleDisconnectListener l) {
        this.mOnBleDisconnectListener = l;
    }

    public void setOnWriteSuccessListener(onWriteSuccessListener l) {
        this.mOnWriteSuccessListener = l;
    }

    public void setOnBleReadListener(onBleReadListener l) {
        this.monBleReadListener = l;
    }

    public boolean connect(String mDeviceAddress, onBleConnectListener l) {
        if (mBAdapter == null || mDeviceAddress == null) {
            return false;
        }

        final BluetoothDevice device = mBAdapter.getRemoteDevice(mDeviceAddress);
        if (device == null) {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        setOnBleConnectListener(l);
        return true;
    }

    public boolean connect(String mDeviceAddress) {
        if (mBAdapter == null || mDeviceAddress == null) {
            return false;
        }

        final BluetoothDevice device = mBAdapter.getRemoteDevice(mDeviceAddress);
        if (device == null) {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(mContext, false, mGattCallback);
        return true;
    }

    // connection change and services discovered.
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {      //设备连接成功
                mConnectionState = STATE_CONNECTED;
                mBluetoothGatt.discoverServices();  //开始搜索服务
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {   //设备断开连接
                mConnectionState = STATE_DISCONNECTED;
                mcharacteristic = null;
                if (mOnBleDisconnectListener != null) {
                    mOnBleDisconnectListener.onBleDisconnect();
                }
                close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                System.out.println("发现服务:");

                Boolean searchFlag = false;
                //获取服务列表
                //List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();
                List<BluetoothGattService> serviceList = new ArrayList<BluetoothGattService>(mBluetoothGatt.getServices());
                //搜索FFF0服务
                for (BluetoothGattService gattService : serviceList) {
                    System.out.println(gattService.getUuid().toString());
                    if ( gattService.getUuid().toString().contains("fff0") || gattService.getUuid().toString().contains("FFF0") ) {
//                        System.out.println("搜到服务 FFF0");

                        if (mBluetoothGatt.getDevice().getName().contains("UNISMES")) {
                            //获取特征值列表
                            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                                if (gattCharacteristic.getUuid().timestamp() == 0xFFF1) {
//                                System.out.println("搜到特征值");
                                    searchFlag = true;
                                    mcharacteristic = gattCharacteristic;     //保存特征值
                                    //打开通知
                                    setCharacteristicNotification(mcharacteristic, true);
                                    if (mOnBleConnectListener != null) {
                                        mOnBleConnectListener.onBleConnect(true);
                                    }
                                }
                            }
                        }
                        else {
                            //获取特征值列表
                            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                                if (gattCharacteristic.getUuid().timestamp() == 0xFFF2) {
//                                System.out.println("搜到特征值");
                                    searchFlag = true;
                                    mcharacteristic = gattCharacteristic;     //保存特征值
                                    //打开通知
                                    setCharacteristicNotification(mcharacteristic, true);
                                    if (mOnBleConnectListener != null) {
                                        mOnBleConnectListener.onBleConnect(true);
                                    }
                                }
                            }
                        }
                    }
                }

                //未找到对应服务,断开当前连接
                if (!searchFlag) {
                    if (mConnectionState == STATE_CONNECTED) {
                        mBluetoothGatt.disconnect();
                        mConnectionState = STATE_DISCONNECTED;
                        if (mOnBleConnectListener != null) {
                            mOnBleConnectListener.onBleConnect(false);
                        }
                    }
                }
            } else {
                System.out.println("onServicesDiscovered received: " + status);
                if (mOnBleConnectListener != null) {
                    mOnBleConnectListener.onBleConnect(false);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (monBleReadListener != null)
                    monBleReadListener.onBleRead(characteristic.getValue());
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
//            Log.d(TAG, "发送数据完毕");
            if (mOnWriteSuccessListener != null) {
                mOnWriteSuccessListener.onWriteSuccess();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            byte[] return_data = characteristic.getValue();

            StringBuffer stringBuffer = new StringBuffer();
            for (int i=0; i<return_data.length; i++) {
                stringBuffer.append(String.format("%02x", return_data[i]));
            }
            System.out.println("onCharacteristicChanged：" + stringBuffer);

            if (mOnReceiveDataListener != null) {
                mOnReceiveDataListener.OnReceiverData(return_data);
            }
        }
    };

    //设置通知开关
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        if (mBAdapter == null || mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
        for (BluetoothGattDescriptor dp : descriptors) {
            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(dp);
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    //断开连接
    public boolean cancelConnect() {
        if (mConnectionState == STATE_CONNECTED) {
            mBluetoothGatt.disconnect();
            mConnectionState = STATE_DISCONNECTED;
            if (mOnBleDisconnectListener != null) {
                mOnBleDisconnectListener.onBleDisconnect();
            }
        }
        return true;
    }

    public boolean cancelConnect(onBleDisconnectListener l) {
        if (mConnectionState == STATE_CONNECTED) {
            setOnBledisconnectListener(l);
            mBluetoothGatt.disconnect();
            mConnectionState = STATE_DISCONNECTED;
            if (mOnBleDisconnectListener != null) {
                mOnBleDisconnectListener.onBleDisconnect();
            }
        }
        return true;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, onBleReadListener l) {
        if (mBAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        Log.d(TAG, "BluetoothAdapter readCharacteristic");
        setOnBleReadListener(l);
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    //写特征值
    public boolean writeDataToCharacteristic(byte[] writeData) {
        if ((mConnectionState == STATE_CONNECTED) && (mcharacteristic != null) && (writeData != null)) {
            mcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            if (writeData.length <= 20) {
                mcharacteristic.setValue(writeData);
                mBluetoothGatt.writeCharacteristic(mcharacteristic);
                StringBuffer stringBuffer = new StringBuffer();
                for (int i=0; i<writeData.length; i++) {
                    stringBuffer.append(String.format("%02x", writeData[i]));
                }
                System.out.println("发送数据：" + stringBuffer);
            }
            else {
                final byte[] writeBytes =  writeData;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] bytesTemp = new byte[20];
                        int i;
                        for (i=0; i<writeBytes.length/20; i++) {
                            System.arraycopy(writeBytes, i*20, bytesTemp, 0, 20);
                            mcharacteristic.setValue(bytesTemp);
                            mBluetoothGatt.writeCharacteristic(mcharacteristic);

                            StringBuffer stringBuffer = new StringBuffer();
                            for (int a=0; a<bytesTemp.length; a++) {
                                stringBuffer.append(String.format("%02x", bytesTemp[a]));
                            }
                            System.out.println("发送数据：" + stringBuffer);

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        int len = writeBytes.length % 20;
                        if (len > 0) {
                            byte[] bytes = new byte[len];
                            System.arraycopy(writeBytes, writeBytes.length - len, bytes, 0, len);
                            mcharacteristic.setValue(bytes);
                            mBluetoothGatt.writeCharacteristic(mcharacteristic);
//                            System.out.println("发送数据：" + bytes);
                        }
                    }
                }).start();
            }
        }
        else {
            return false;
        }
        return true;
    }

    public boolean writeCharacteristic(byte[] writeData, onReceiveDataListener l) {
        if ((mConnectionState == STATE_CONNECTED) && (mcharacteristic != null) && (writeData != null)) {
            mcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            setOnReceiveDataListener(l);
            if (writeData.length <= 20) {
                mcharacteristic.setValue(writeData);
                mBluetoothGatt.writeCharacteristic(mcharacteristic);
//                StringBuffer stringBuffer = new StringBuffer();
//                for (int i=0; i<writeData.length; i++) {
//                    stringBuffer.append(String.format("%02x", writeData[i]));
//                }
//                System.out.println("发送数据：" + stringBuffer);
            }
            else {
                final byte[] writeBytes =  writeData;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        byte[] bytesTemp = new byte[20];
                        int i;
                        for (i=0; i<writeBytes.length/20; i++) {
                            System.arraycopy(writeBytes, i*20, bytesTemp, 0, 20);
                            mcharacteristic.setValue(bytesTemp);
                            mBluetoothGatt.writeCharacteristic(mcharacteristic);
//                            System.out.println("发送数据：" + bytesTemp);
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        int len = writeBytes.length % 20;
                        if (len > 0) {
                            byte[] bytes = new byte[len];
                            System.arraycopy(writeBytes, writeBytes.length - len, bytes, 0, len);
                            mcharacteristic.setValue(bytes);
                            mBluetoothGatt.writeCharacteristic(mcharacteristic);
//                            System.out.println("发送数据：" + bytes);
                        }
                    }
                }).start();
            }
        }
        else {
            return false;
        }
        return true;
    }

    public void writeCharacteristic(byte[] s, onWriteSuccessListener l) {
        if (mcharacteristic != null) {
            setOnWriteSuccessListener(l);
            mcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mcharacteristic.setValue(s);
            mBluetoothGatt.writeCharacteristic(mcharacteristic);
//            characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
    }

    public void writeCharacteristic(byte[] s) {
        if (mcharacteristic != null) {
            mcharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            mcharacteristic.setValue(s);
            mBluetoothGatt.writeCharacteristic(mcharacteristic);
        }
    }

    //获取服务
    public List<BluetoothGattService> getServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
}


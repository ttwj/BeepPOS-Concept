package com.dk.bleNfc;

import android.content.Context;
import android.util.Log;

import com.dk.bleNfc.card.CpuCard;
import com.dk.bleNfc.card.DESFire;
import com.dk.bleNfc.card.FeliCa;
import com.dk.bleNfc.card.Iso14443bCard;
import com.dk.bleNfc.card.Iso15693Card;
import com.dk.bleNfc.card.Mifare;
import com.dk.bleNfc.card.Ntag21x;
import com.dk.bleNfc.card.Ultralight;

/**
 * Created by lochy on 16/1/19.
 */
public class DeviceManager {
    public final static String SDK_VERSIONS = "v1.4.0 20161026";
    private DeviceManagerCallback mDeviceManagerCallback = null;
    public BleManager bleManager = null;

    public CpuCard cpuCard;
    public Iso14443bCard iso14443bCard;
    public DESFire desFire;
    public Iso15693Card iso15693Card;
    public Mifare mifare;
    public Ntag21x ntag21x;
    public Ultralight ultralight;
    public FeliCa feliCa;
    public int mCardType;

    public onReceiveBatteryVoltageDeviceListener mOnReceiveBatteryVoltageDeviceListener;
    public onReceiveVersionsDeviceListener mOnReceiveVersionsDeviceListener;
    public onReceiveConnectBtDeviceListener mOnReceiveConnectBtDeviceListener;
    public onReceiveDisConnectDeviceListener mOnReceiveDisConnectDeviceListener;
    public onReceiveConnectionStatusListener mOnReceiveConnectionStatusListener;
    public onReceiveInitCiphyListener mOnReceiveInitCiphyListener;
    public onReceiveDeviceAuthListener mOnReceiveDeviceAuthListener;
    public onReceiveRfnSearchCardListener mOnReceiveRfnSearchCardListener;
    public onReceiveRfmSentApduCmdListener mOnReceiveRfmSentApduCmdListener;
    public onReceiveRfmSentBpduCmdListener mOnReceiveRfmSentBpduCmdListener;
    public onReceiveRfmCloseListener mOnReceiveRfmCloseListener;
    public onReceiveRfmSuicaBalanceListener mOnReceiveRfmSuicaBalanceListener;
    public onReceiveRfmFelicaReadListener mOnReceiveRfmFelicaReadListener;
    public onReceiveRfmFelicaCmdListener mOnReceiveRfmFelicaCmdListener;
    public onReceiveRfmUltralightCmdListener mOnReceiveRfmUltralightCmdListener;
    public onReceiveRfmMifareAuthListener mOnReceiveRfmMifareAuthListener;
    public onReceiveRfmMifareDataExchangeListener mOnReceiveRfmMifareDataExchangeListener;
    public onReceivePalTestChannelListener mOnReceivePalTestChannelListener;

    public final static byte  CARD_TYPE_DEFAULT = 0x00;           //卡片类型：未定义
    public final static byte  CARD_TYPE_ISO4443_A = 0x01;        //卡片类型ISO14443-A
    public final static byte  CARD_TYPE_ISO4443_B = 0x02;        //卡片类型ISO14443-B
    public final static byte  CARD_TYPE_FELICA = 0x03;           //卡片类型Felica
    public final static byte  CARD_TYPE_MIFARE = 0x04;           //卡片类型Mifare卡
    public final static byte  CARD_TYPE_ISO15693 = 0x05;        //卡片类型iso15693卡
    public final static byte  CARD_TYPE_ULTRALIGHT = 0x06;      //RF_TYPE_MF
    public final static byte  CARD_TYPE_DESFire = 0x07;         //DESFire卡

    public DeviceManager(Context context) {
        bleManager = new BleManager(context);
        bleManager.setOnReceiveDataListener(new BleManager.onReceiveDataListener() {
            @Override
            public void OnReceiverData(byte[] data) {
                comByteManager.rcvData(data);
            }
        });

        bleManager.setOnBledisconnectListener(new BleManager.onBleDisconnectListener() {
            @Override
            public void onBleDisconnect() {
                mDeviceManagerCallback.onReceiveDisConnectDevice(true);
                if (mOnReceiveDisConnectDeviceListener != null) {
                    mOnReceiveDisConnectDeviceListener.onReceiveDisConnectDevice(true);
                }
            }
        });
    }

    public  Object getCard() {
        switch (mCardType) {
            case CARD_TYPE_ISO4443_A:
                return cpuCard;
            case CARD_TYPE_ISO4443_B:
                return iso14443bCard;
            case CARD_TYPE_FELICA:
                return feliCa;
            case CARD_TYPE_MIFARE:
                return mifare;
            case CARD_TYPE_ISO15693:
                return iso15693Card;
            case CARD_TYPE_ULTRALIGHT:
                return ntag21x;
            case CARD_TYPE_DESFire:
                return desFire;
            default:
                return null;
        }
    }

    public void setCallBack(DeviceManagerCallback callBack) {
        mDeviceManagerCallback = callBack;
    }

    //获取设备电池电压（V）回调
    public interface onReceiveBatteryVoltageDeviceListener{
        public void onReceiveBatteryVoltageDevice(double voltage);
    }

    //获取设备版本回调
    public interface onReceiveVersionsDeviceListener{
        public void onReceiveVersionsDevice(byte versions);
    }

    //获取设备连接回调
    public interface onReceiveConnectBtDeviceListener {
        public void onReceiveConnectBtDevice(boolean blnIsConnectSuc);
    }

    //断开设备连接回调
    public interface onReceiveDisConnectDeviceListener {
        public void onReceiveDisConnectDevice(boolean blnIsDisConnectDevice);
    }

    //检测设备状态回调
    public interface onReceiveConnectionStatusListener {
        public void onReceiveConnectionStatus(boolean blnIsConnection);
    }

    //初始化密钥回调
    public interface onReceiveInitCiphyListener {
        public void onReceiveInitCiphy(boolean blnIsInitSuc);
    }

    //设备认证回调
    //authData：设备返回的8字节认证码
    public interface onReceiveDeviceAuthListener {
        public void onReceiveDeviceAuth(byte[] authData);
    }

    //非接寻卡回调
    public interface onReceiveRfnSearchCardListener {
        public void onReceiveRfnSearchCard(boolean blnIsSus, int cardType, byte[] bytCardSn, byte[] bytCarATS);
    }

    //发送APDU指令回调
    public interface onReceiveRfmSentApduCmdListener {
        public void onReceiveRfmSentApduCmd(boolean isCmdRunSuc, byte[] bytApduRtnData);
    }

    //发送BPDU指令回调
    public interface onReceiveRfmSentBpduCmdListener {
        public void onReceiveRfmSentBpduCmd(boolean isCmdRunSuc, byte[] bytBpduRtnData);
    }

    //关闭非接模块回调
    public interface onReceiveRfmCloseListener {
        public void onReceiveRfmClose(boolean blnIsCloseSuc);
    }

    //获取suica余额回调
    public interface onReceiveRfmSuicaBalanceListener {
        public void onReceiveRfmSuicaBalance(boolean blnIsSuc, byte[] bytBalance);
    }

    //读Felica回调
    public interface onReceiveRfmFelicaReadListener{
        public void onReceiveRfmFelicaRead(boolean blnIsReadSuc, byte[] bytBlockData);
    }

    //Felica指令通道回调
    public interface onReceiveRfmFelicaCmdListener {
        public void onReceiveRfmFelicaCmd(boolean isSuc, byte[] returnBytes);
    }

    //UL卡指令接口
    public interface onReceiveRfmUltralightCmdListener {
        public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData);
    }

    //Mifare卡验证密码回调
    public interface onReceiveRfmMifareAuthListener {
        public void onReceiveRfmMifareAuth(boolean isSuc);
    }

    //Mifare数据交换通道回调
    public interface onReceiveRfmMifareDataExchangeListener{
        public void onReceiveRfmMifareDataExchange(boolean isSuc, byte[] returnData);
    }

    public interface onReceivePalTestChannelListener{
        public void onReceivePalTestChannel(byte[] returnData);
    }

    //根据蓝牙MAC地址连接设备接口
    public void requestConnectBleDevice(String strBleAddr) {
        //todo make callback to UI
        boolean status = bleManager.connect(strBleAddr, new BleManager.onBleConnectListener() {
            @Override
            public void onBleConnect(boolean isConnectSucceed) {
                if (isConnectSucceed) {
                    mDeviceManagerCallback.onReceiveConnectBtDevice(true);
                }
                else {
                    mDeviceManagerCallback.onReceiveConnectBtDevice(false);
                }
            }
        });

        if (!status) {
            if (mDeviceManagerCallback != null)
                mDeviceManagerCallback.onReceiveConnectBtDevice(false);
        }
    }

    //根据蓝牙MAC地址连接设备接口
    public void requestConnectBleDevice(String strBleAddr, onReceiveConnectBtDeviceListener l) {
        mOnReceiveConnectBtDeviceListener = l;
        boolean status = bleManager.connect(strBleAddr, new BleManager.onBleConnectListener() {
            @Override
            public void onBleConnect(boolean isConnectSucceed) {
                if (isConnectSucceed) {
                    mDeviceManagerCallback.onReceiveConnectBtDevice(true);
                    if (mOnReceiveConnectBtDeviceListener != null) {
                        mOnReceiveConnectBtDeviceListener.onReceiveConnectBtDevice(true);
                    }
                }
                else {
                    mDeviceManagerCallback.onReceiveConnectBtDevice(false);
                    if (mOnReceiveConnectBtDeviceListener != null) {
                        mOnReceiveConnectBtDeviceListener.onReceiveConnectBtDevice(false);
                    }
                }
            }
        });

        if (!status) {
            if (mDeviceManagerCallback != null) {
                mDeviceManagerCallback.onReceiveConnectBtDevice(false);
            }

            if (mOnReceiveConnectBtDeviceListener != null) {
                mOnReceiveConnectBtDeviceListener.onReceiveConnectBtDevice(false);
            }
        }
    }

    //断开连接接口
    public void requestDisConnectDevice() {
        bleManager.cancelConnect();
    }
    public void requestDisConnectDevice(onReceiveDisConnectDeviceListener l) {
        mOnReceiveDisConnectDeviceListener = l;
        bleManager.cancelConnect(new BleManager.onBleDisconnectListener() {
            @Override
            public void onBleDisconnect() {
                mDeviceManagerCallback.onReceiveDisConnectDevice(true);
                if (mOnReceiveDisConnectDeviceListener != null) {
                    mOnReceiveDisConnectDeviceListener.onReceiveDisConnectDevice(true);
                }
            }
        });
    }

    //检测设备状态接口
    public void requestConnectionStatus() {
        mDeviceManagerCallback.onReceiveConnectionStatus(bleManager.mConnectionState == BleManager.STATE_CONNECTED);
        if (mOnReceiveConnectionStatusListener != null) {
            mOnReceiveConnectionStatusListener.onReceiveConnectionStatus(bleManager.mConnectionState == BleManager.STATE_CONNECTED);
        }
    }
    public void requestConnectionStatus(onReceiveConnectionStatusListener l) {
        mOnReceiveConnectionStatusListener = l;
        mDeviceManagerCallback.onReceiveConnectionStatus(bleManager.mConnectionState == BleManager.STATE_CONNECTED);
        if (mOnReceiveConnectionStatusListener != null) {
            mOnReceiveConnectionStatusListener.onReceiveConnectionStatus(bleManager.mConnectionState == BleManager.STATE_CONNECTED);
        }
    }
    public boolean isConnection(){
        return bleManager.mConnectionState == BleManager.STATE_CONNECTED;
    }

    //初始秘钥接口
    public void requestInitCiphy(byte bytKeyType, byte bytKeyVer, byte[] bytKeyValue) {
    }

//    //获取设备信息
//    public void requestGetDeviceVisualInfo(BleDeviceVisualInfo bleDeviceVisualInfo) {
//    }
//
//    //设置设备信息
//    public void requestSetDeviceInitInfo(BleDevInitInfo bleDevInitInfo) {
//    }

    //数据接收完成回调
    private ComByteManagerCallback comByteManagerCallback = new ComByteManagerCallback() {
        @Override
        public void onRcvBytes(boolean isSuc, byte[] rcvBytes) {
            super.onRcvBytes(isSuc, rcvBytes);
            switch ( (byte)(( (int)comByteManager.getCmd() & 0xff ) - 1) ) {
                case ComByteManager.GET_VERSIONS_COM:
                    if (mOnReceiveVersionsDeviceListener != null) {
                        if (rcvBytes != null && rcvBytes.length == 1) {
                            mOnReceiveVersionsDeviceListener.onReceiveVersionsDevice(rcvBytes[0]);
                        }
                        else {
                            mOnReceiveVersionsDeviceListener.onReceiveVersionsDevice((byte) 0);
                        }
                    }
                    break;
                case ComByteManager.GET_BT_VALUE_COM:
                    if (mOnReceiveBatteryVoltageDeviceListener != null) {
                        if (rcvBytes != null && rcvBytes.length == 2) {
                            double v = ( ((rcvBytes[0] & 0x00ff) << 8) | (rcvBytes[1] & 0x00ff) ) / 100.0;
                            mOnReceiveBatteryVoltageDeviceListener.onReceiveBatteryVoltageDevice(v);
                        }
                        else {
                            mOnReceiveBatteryVoltageDeviceListener.onReceiveBatteryVoltageDevice(0.0);
                        }
                    }
                    break;
                case ComByteManager.ANTENNA_OFF_COM:
                    mDeviceManagerCallback.onReceiveRfmClose(true);
                    if (mOnReceiveRfmCloseListener != null) {
                        mOnReceiveRfmCloseListener.onReceiveRfmClose(true);
                    }
                    break;
                case ComByteManager.ACTIVATE_PICC_COM:
                    cpuCard = null;
                    mifare = null;
                    iso15693Card = null;
                    iso14443bCard = null;
                    feliCa = null;
                    ntag21x = null;
                    ultralight = null;
                    desFire = null;
                    if (comByteManager.getCmdRunStatus()) {
                        byte uidBytes[];
                        byte atrBytes[];

                        int cardType = rcvBytes[0];
                        mCardType = cardType;

                        Log.d("DeviceManager", "Yo a card detected " + cardType);

                        if (cardType == CARD_TYPE_ISO4443_A) {
                            uidBytes = new byte[4];
                            System.arraycopy(rcvBytes, 1, uidBytes, 0, 4);
                            atrBytes = new byte[rcvBytes.length - 5];
                            System.arraycopy(rcvBytes, 5, atrBytes, 0, rcvBytes.length - 5);
                            cpuCard = new CpuCard(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else if (cardType == CARD_TYPE_MIFARE) {
                            uidBytes = new byte[4];
                            System.arraycopy(rcvBytes, 1, uidBytes, 0, 4);
                            atrBytes = new byte[rcvBytes.length - 5];
                            System.arraycopy(rcvBytes, 5, atrBytes, 0, rcvBytes.length - 5);
                            mifare = new Mifare(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else if (cardType == CARD_TYPE_ISO15693) {
                            uidBytes = new byte[4];
                            atrBytes = new byte[1];;
                            iso15693Card = new Iso15693Card(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else if (cardType == CARD_TYPE_ULTRALIGHT) {
                            uidBytes = new byte[7];
                            System.arraycopy(rcvBytes, 1, uidBytes, 0, 7);
                            atrBytes = new byte[rcvBytes.length - 8];
                            System.arraycopy(rcvBytes, 8, atrBytes, 0, rcvBytes.length - 8);
                            ultralight = new Ultralight(DeviceManager.this, uidBytes, atrBytes);
                            ntag21x = new Ntag21x(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else if (cardType == CARD_TYPE_DESFire) {
                            uidBytes = new byte[7];
                            System.arraycopy(rcvBytes, 1, uidBytes, 0, 7);
                            atrBytes = new byte[rcvBytes.length - 8];
                            System.arraycopy(rcvBytes, 8, atrBytes, 0, rcvBytes.length - 8);
                            desFire = new DESFire(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else if (cardType == CARD_TYPE_ISO4443_B){
                            uidBytes = new byte[4];
                            atrBytes = new byte[rcvBytes.length - 1];
                            System.arraycopy(rcvBytes, 1, atrBytes, 0, rcvBytes.length - 1);
                            iso14443bCard = new Iso14443bCard(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else if (cardType == CARD_TYPE_FELICA) {
                            uidBytes = new byte[4];
                            atrBytes = new byte[rcvBytes.length - 1];
                            System.arraycopy(rcvBytes, 1, atrBytes, 0, rcvBytes.length - 1);
                            feliCa = new FeliCa(DeviceManager.this, uidBytes, atrBytes);
                        }
                        else {
                            uidBytes = null;
                            atrBytes = null;
                            mDeviceManagerCallback.onReceiveRfnSearchCard(false, cardType, uidBytes, atrBytes);
                            if (mOnReceiveRfnSearchCardListener != null) {
                                mOnReceiveRfnSearchCardListener.onReceiveRfnSearchCard(false, cardType, uidBytes, atrBytes);
                            }
                        }

                        mDeviceManagerCallback.onReceiveRfnSearchCard(true, cardType, uidBytes, atrBytes);
                        if (mOnReceiveRfnSearchCardListener != null) {
                            mOnReceiveRfnSearchCardListener.onReceiveRfnSearchCard(true, cardType, uidBytes, atrBytes);
                        }
                    }
                    else {
                        mDeviceManagerCallback.onReceiveRfnSearchCard(false, 0, null, null);
                        if (mOnReceiveRfnSearchCardListener != null) {
                            mOnReceiveRfnSearchCardListener.onReceiveRfnSearchCard(false, 0, null, null);
                        }
                    }
                    break;
                case ComByteManager.APDU_COM:
                    mDeviceManagerCallback.onReceiveRfmSentApduCmd(rcvBytes);
                    if (mOnReceiveRfmSentApduCmdListener != null) {
                        mOnReceiveRfmSentApduCmdListener.onReceiveRfmSentApduCmd(isSuc,rcvBytes);
                    }
                    break;
                case ComByteManager.BPDU_COM:
                    mDeviceManagerCallback.onReceiveRfmSentBpduCmd(rcvBytes);
                    if (mOnReceiveRfmSentBpduCmdListener != null) {
                        mOnReceiveRfmSentBpduCmdListener.onReceiveRfmSentBpduCmd(isSuc,rcvBytes);
                    }
                    break;
                case ComByteManager.GET_SUICA_BALANCE_COM:
                    mDeviceManagerCallback.onReceiveRfmSuicaBalance(isSuc, rcvBytes);
                    if (mOnReceiveRfmSuicaBalanceListener != null) {
                        mOnReceiveRfmSuicaBalanceListener.onReceiveRfmSuicaBalance(isSuc, rcvBytes);
                    }
                    break;
                case ComByteManager.FELICA_READ_COM:
                    mDeviceManagerCallback.onReceiveRfmFelicaRead(isSuc, rcvBytes);
                    if (mOnReceiveRfmFelicaReadListener != null) {
                        mOnReceiveRfmFelicaReadListener.onReceiveRfmFelicaRead(isSuc, rcvBytes);
                    }
                    break;
                case ComByteManager.FELICA_COM:
                    if (mOnReceiveRfmFelicaCmdListener != null) {
                        mOnReceiveRfmFelicaCmdListener.onReceiveRfmFelicaCmd(isSuc, rcvBytes);
                    }
                    break;

                case ComByteManager.ULTRALIGHT_CMD:
                    mDeviceManagerCallback.onReceiveRfmUltralightCmd(rcvBytes);
                    if (mOnReceiveRfmUltralightCmdListener != null) {
                        mOnReceiveRfmUltralightCmdListener.onReceiveRfmUltralightCmd(isSuc, rcvBytes);
                    }
                    break;
                case ComByteManager.MIFARE_AUTH_COM:
                    if (mOnReceiveRfmMifareAuthListener != null) {
                        mOnReceiveRfmMifareAuthListener.onReceiveRfmMifareAuth(isSuc);
                    }
                    break;
                case ComByteManager.MIFARE_COM:
                    if (mOnReceiveRfmMifareDataExchangeListener != null) {
                        mOnReceiveRfmMifareDataExchangeListener.onReceiveRfmMifareDataExchange(isSuc, rcvBytes);
                    }
                    break;
                case ComByteManager.PAL_TEST_CHANNEL:
                    if (mOnReceivePalTestChannelListener != null) {
                        mOnReceivePalTestChannelListener.onReceivePalTestChannel(rcvBytes);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private ComByteManager comByteManager = new ComByteManager(comByteManagerCallback);


    public void requestBatteryVoltageDevice(onReceiveBatteryVoltageDeviceListener listener) {
        mOnReceiveBatteryVoltageDeviceListener = listener;
        bleManager.writeDataToCharacteristic(comByteManager.getBtValueComByte());
    }

    public void requestVersionsDevice(onReceiveVersionsDeviceListener listener) {
        mOnReceiveVersionsDeviceListener = listener;
        bleManager.writeDataToCharacteristic(comByteManager.getVersionsComByte());
    }

    //非接寻卡接口：（连接成功后收到操作类回调即可开始寻卡）
    //bytCardType 读卡类型
    //0x00：自动寻卡
    //0x01：寻Mifare卡或者Ul卡（CPU卡视为M1卡）
    public void requestRfmSearchCard(byte bytCardType) {
        bleManager.writeDataToCharacteristic(comByteManager.AActivityComByte(bytCardType));
    }
    public void requestRfmSearchCard(byte bytCardType, onReceiveRfnSearchCardListener l) {
        mOnReceiveRfnSearchCardListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.AActivityComByte(bytCardType));
    }

    //发送APDU指令
    public void requestRfmSentApduCmd(byte[] bytApduData) {
        bleManager.writeDataToCharacteristic(comByteManager.rfApduCmdByte(bytApduData));
    }
    public void requestRfmSentApduCmd(byte[] bytApduData, onReceiveRfmSentApduCmdListener l) {
        mOnReceiveRfmSentApduCmdListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.rfApduCmdByte(bytApduData));
    }

    //发送BPDU指令
    public void requestRfmSentBpduCmd(byte[] bytBpduData) {
        bleManager.writeDataToCharacteristic(comByteManager.rfBpduCmdByte(bytBpduData));
    }
    public void requestRfmSentBpduCmd(byte[] bytBpduData, onReceiveRfmSentBpduCmdListener l) {
        mOnReceiveRfmSentBpduCmdListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.rfBpduCmdByte(bytBpduData));
    }

    //关闭非接模块
    public void requestRfmClose() {
        bleManager.writeDataToCharacteristic(comByteManager.rfPowerOffComByte());
    }
    public void requestRfmClose(onReceiveRfmCloseListener l) {
        mOnReceiveRfmCloseListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.rfPowerOffComByte());
    }

    //读取Suica余额指令
    public void requestRfmSuicaBalance() {
        bleManager.writeDataToCharacteristic(comByteManager.getSuicaBalanceCmdByte());
    }
    public void requestRfmSuicaBalance(onReceiveRfmSuicaBalanceListener l) {
        mOnReceiveRfmSuicaBalanceListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.getSuicaBalanceCmdByte());
    }

    //读Felica指令
    //systemCode: 两字节，高位在前
    //blockAddr： 两字节，高位在前
    public void requestRfmFelicaRead(byte[] systemCode, byte[] blockAddr) {
        bleManager.writeDataToCharacteristic(comByteManager.readFeliCaCmdByte(systemCode, blockAddr));
    }
    //systemCode: 两字节，高位在前
    //blockAddr： 两字节，高位在前
    public void requestRfmFelicaRead(byte[] systemCode, byte[] blockAddr, onReceiveRfmFelicaReadListener l) {
        mOnReceiveRfmFelicaReadListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.readFeliCaCmdByte(systemCode, blockAddr));
    }

    //Felica指令通道
    public void requestRfmFelicaCmd(int wOption, int wN, byte[] cmdBytes) {
        bleManager.writeDataToCharacteristic(comByteManager.felicaCmdByte(wOption, wN, cmdBytes));
    }
    public void requestRfmFelicaCmd(int wOption, int wN, byte[] cmdBytes, onReceiveRfmFelicaCmdListener l) {
        mOnReceiveRfmFelicaCmdListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.felicaCmdByte(wOption, wN, cmdBytes));
    }

    //Ultralight指令通道
    public void requestRfmUltralightCmd(byte[] bytUlCmdData) {
        bleManager.writeDataToCharacteristic(comByteManager.ultralightCmdByte(bytUlCmdData));
    }
    public void requestRfmUltralightCmd(byte[] bytUlCmdData, onReceiveRfmUltralightCmdListener l) {
        mOnReceiveRfmUltralightCmdListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.ultralightCmdByte(bytUlCmdData));
    }

    //Mifare卡验证密码
    public void requestRfmMifareAuth(byte bBlockNo, byte bKeyType, byte[] pKey, byte[] pUid) {
        bleManager.writeDataToCharacteristic(comByteManager.rfMifareAuthCmdByte(bBlockNo, bKeyType, pKey, pUid));
    }
    public void requestRfmMifareAuth(byte bBlockNo, byte bKeyType, byte[] pKey, byte[] pUid, onReceiveRfmMifareAuthListener l) {
        mOnReceiveRfmMifareAuthListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.rfMifareAuthCmdByte(bBlockNo, bKeyType, pKey, pUid));
    }

    //Mifare卡数据交换通道
    public void requestRfmMifareDataExchange(byte[] dataByte) {
        bleManager.writeDataToCharacteristic(comByteManager.rfMifareDataExchangeCmdByte(dataByte));
    }
    public void requestRfmMifareDataExchange(byte[] dataByte, onReceiveRfmMifareDataExchangeListener l) {
        mOnReceiveRfmMifareDataExchangeListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.rfMifareDataExchangeCmdByte(dataByte));
    }

    //通讯协议测试通道
    public void requestPalTestChannel(byte[] dataBytes, onReceivePalTestChannelListener l) {
        mOnReceivePalTestChannelListener = l;
        bleManager.writeDataToCharacteristic(comByteManager.getTestChannelBytes(dataBytes));
    }

    //设置APDU传输模式
    //TransferMode Byte---
    //UNENCRYPTED: 明文传输APDU
    //ENCRYPTED: 密文传输APDU(3DES)
    //Keydata Byte[]---- 用于加密的3DES密钥（只在TransferMode = ENCRYPTED时有用）
    public void requestApduTransferMode(byte TransferMode, byte[] Keydata) {
    }

    //通用APDU指令处理
    //ApduCommand: 完整的APDU指令
    public void sendApduCmd (byte[] ApduCommand) {
    }
}

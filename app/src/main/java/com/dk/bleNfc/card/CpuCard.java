package com.dk.bleNfc.card;

import com.dk.bleNfc.DeviceManager;

/**
 * Created by Administrator on 2016/9/21.
 */
public class CpuCard extends Card {
    public onReceiveApduExchangeListener mOnReceiveApduExchangeListener;

    public CpuCard(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public CpuCard(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //APDU指令通道回调
    public interface onReceiveApduExchangeListener{
        public void onReceiveApduExchange(boolean isCmdRunSuc, byte[] bytApduRtnData);
    }

    //APDU指令通道
    public void apduExchange(byte[] apduBytes, onReceiveApduExchangeListener listener) {
        mOnReceiveApduExchangeListener = listener;
        deviceManager.requestRfmSentApduCmd(apduBytes, new DeviceManager.onReceiveRfmSentApduCmdListener() {
            @Override
            public void onReceiveRfmSentApduCmd(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                if (mOnReceiveApduExchangeListener != null) {
                    mOnReceiveApduExchangeListener.onReceiveApduExchange(isCmdRunSuc, bytApduRtnData);
                }
            }
        });
    }
}

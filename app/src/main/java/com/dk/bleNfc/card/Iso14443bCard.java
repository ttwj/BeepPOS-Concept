package com.dk.bleNfc.card;

import com.dk.bleNfc.DeviceManager;

/**
 * Created by Administrator on 2016/9/21.
 */
public class Iso14443bCard extends Card{
    public onReceiveBpduExchangeListener mOnReceiveBpduExchangeListener;

    public Iso14443bCard(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Iso14443bCard(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //BPDU指令通道回调
    public interface onReceiveBpduExchangeListener{
        public void onReceiveBpduExchange(boolean isCmdRunSuc, byte[] bytBpduRtnData);
    }

    //BPDU指令通道
    public void bpduExchange(byte[] apduBytes, onReceiveBpduExchangeListener listener) {
        mOnReceiveBpduExchangeListener = listener;
        deviceManager.requestRfmSentBpduCmd(apduBytes, new DeviceManager.onReceiveRfmSentBpduCmdListener() {
            @Override
            public void onReceiveRfmSentBpduCmd(boolean isCmdRunSuc, byte[] bytBpduRtnData) {
                if (mOnReceiveBpduExchangeListener != null) {
                    mOnReceiveBpduExchangeListener.onReceiveBpduExchange(isCmdRunSuc, bytBpduRtnData);
                }
            }
        });
    }
}

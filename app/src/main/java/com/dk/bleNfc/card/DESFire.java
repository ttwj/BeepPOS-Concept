package com.dk.bleNfc.card;

import com.dk.bleNfc.DeviceManager;

/**
 * Created by Administrator on 2016/9/21.
 */
public class DESFire extends Card {
    public onReceiveCmdListener mOnReceiveCmdListener;

    public DESFire(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public DESFire(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //DESFire卡指令通道回调
    public interface onReceiveCmdListener{
        public void onReceiveCmdExchange(boolean isCmdRunSuc, byte[] bytCmdRtnData);
    }

    //DESFire卡指令通道
    public void cmd(byte[] cmdBytes, onReceiveCmdListener listener) {
        mOnReceiveCmdListener = listener;
        deviceManager.requestRfmSentApduCmd(cmdBytes, new DeviceManager.onReceiveRfmSentApduCmdListener() {
            @Override
            public void onReceiveRfmSentApduCmd(boolean isCmdRunSuc, byte[] bytApduRtnData) {
                if (mOnReceiveCmdListener != null) {
                    mOnReceiveCmdListener.onReceiveCmdExchange(isCmdRunSuc, bytApduRtnData);
                }
            }
        });
    }
}

package com.dk.bleNfc.card;

import com.dk.bleNfc.DeviceManager;

/**
 * Created by Administrator on 2016/9/21.
 */
public class Card {
    public DeviceManager deviceManager;
    public byte[] uid;
    public byte[] atr;
    public onReceiveCloseListener mOnReceiveCloseListener;

    public Card(DeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public Card(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        this.deviceManager = deviceManager;
        this.uid = uid;
        this.atr = atr;
    }

    public String uidToString() {
        if ( (uid == null) || (uid.length == 0) ) {
            return null;
        }
        StringBuffer stringBuffer = new StringBuffer();
        for (int i=0; i<uid.length; i++) {
            stringBuffer.append(String.format("%02x", uid[i]));
        }
        return stringBuffer.toString();
    }

    //卡片掉电回调
    public interface onReceiveCloseListener {
        public void onReceiveClose(boolean isOk);
    }

    //卡片掉电
    public void close(onReceiveCloseListener listener) {
        mOnReceiveCloseListener = listener;
        deviceManager.requestRfmClose(new DeviceManager.onReceiveRfmCloseListener() {
            @Override
            public void onReceiveRfmClose(boolean blnIsCloseSuc) {
                if (mOnReceiveCloseListener != null) {
                    mOnReceiveCloseListener.onReceiveClose(blnIsCloseSuc);
                }
            }
        });
    }
}

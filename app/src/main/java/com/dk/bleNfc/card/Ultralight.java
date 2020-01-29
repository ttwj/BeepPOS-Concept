package com.dk.bleNfc.card;

import com.dk.bleNfc.DeviceManager;

/**
 * Created by Administrator on 2016/9/19.
 */
public class Ultralight extends Card{
    final static byte  UL_GET_VERSION_CMD = (byte)0x60;
    final static byte  UL_READ_CMD = (byte)0x30;
    final static byte  UL_FAST_READ_CMD = (byte)0x3A;
    final static byte  UL_WRITE_CMD = (byte)0xA2;
    final static byte  UL_READ_CNT_CMD = (byte)0x39;
    final static byte  UL_PWD_AUTH_CMD = (byte)0x1B;

    public final static int   UL_MAX_FAST_READ_BLOCK_NUM = 0x20;

    public onReceiveGetVersionListener mOnReceiveGetVersionListener;
    public onReceiveReadListener mOnReceiveReadListener;
    public onReceiveFastReadListener mOnReceiveFastReadListener;
    public onReceiveWriteListener mOnReceiveWriteListener;
    public onReceiveReadCntListener mOnReceiveReadCntListener;
    public onReceivePwdAuthListener mOnReceivePwdAuthListener;
    public onReceiveCmdListener mOnReceiveCmdListener;

    public Ultralight(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Ultralight(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //读取卡片版本回调
    public interface onReceiveGetVersionListener {
        public void onReceiveGetVersion(boolean isSuc, byte[] returnBytes);
    }
    //读块回调
    public interface onReceiveReadListener {
        public void onReceiveRead(boolean isSuc, byte[] returnBytes);
    }
    //快速读回调
    public interface onReceiveFastReadListener {
        public void onReceiveFastRead(boolean isSuc, byte[] returnBytes);
    }
    //写块回调
    public interface onReceiveWriteListener {
        public void onReceiveWrite(boolean isSuc, byte[] returnBytes);
    }
    //读次数回调
    public interface onReceiveReadCntListener {
        public void onReceiveReadCnt(byte[] returnBytes);
    }
    //验证密码回调
    public interface onReceivePwdAuthListener {
        public void onReceivePwdAuth(boolean isSuc);
    }
    //验证密码回调
    public interface onReceiveCmdListener {
        public void onReceiveCmd(byte[] returnBytes);
    }

    //读取卡片版本
    public void getVersion(onReceiveGetVersionListener listener) {
        mOnReceiveGetVersionListener = listener;
        byte[] cmdByte = {UL_GET_VERSION_CMD};
        deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
            @Override
            public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                if (mOnReceiveGetVersionListener != null) {
                    mOnReceiveGetVersionListener.onReceiveGetVersion(isCmdRunSuc, bytUlRtnData);
                }
            }
        });
    }

    //读块
    public void read(byte address, onReceiveReadListener listener) {
        mOnReceiveReadListener = listener;
        byte[] cmdByte = {UL_READ_CMD, address};
        deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
            @Override
            public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                if (mOnReceiveReadListener != null) {
                    mOnReceiveReadListener.onReceiveRead(isCmdRunSuc, bytUlRtnData);
                }
            }
        });
    }

    //快速读
    public void fastRead(byte startAddress, byte endAddress, onReceiveFastReadListener listener) {
        mOnReceiveFastReadListener = listener;
        if (startAddress > endAddress) {
            if (mOnReceiveFastReadListener != null) {
                mOnReceiveFastReadListener.onReceiveFastRead(false, null);
            }
            return;
        }
        byte[] cmdByte = {UL_FAST_READ_CMD, startAddress, endAddress};
        deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
            @Override
            public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                if (mOnReceiveFastReadListener != null) {
                    mOnReceiveFastReadListener.onReceiveFastRead(isCmdRunSuc, bytUlRtnData);
                }
            }
        });
    }

    //写块
    public void write(byte address, byte[] data, onReceiveWriteListener listener) {
        mOnReceiveWriteListener = listener;
        byte[] cmdByte = {UL_WRITE_CMD, address, data[0], data[1], data[2], data[3]};
        deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
            @Override
            public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                if (mOnReceiveWriteListener != null) {
                    mOnReceiveWriteListener.onReceiveWrite(isCmdRunSuc, bytUlRtnData);
                }
            }
        });
    }

    //读次数
    public void readCnt(onReceiveReadCntListener listener) {
        mOnReceiveReadCntListener = listener;
        byte[] cmdByte = {UL_READ_CNT_CMD, 0x02};
        deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
            @Override
            public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                if (mOnReceiveReadCntListener != null) {
                    mOnReceiveReadCntListener.onReceiveReadCnt(bytUlRtnData);
                }
            }
        });
    }

    //验证密码
    public void pwdAuth(byte[] password, onReceivePwdAuthListener listener) {
        mOnReceivePwdAuthListener = listener;
        if (password.length != 4) {
            if (mOnReceivePwdAuthListener != null) {
                mOnReceivePwdAuthListener.onReceivePwdAuth(false);
            }
        }
        else {
            byte[] cmdByte = {UL_PWD_AUTH_CMD, password[0], password[1], password[2], password[3]};
            deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
                @Override
                public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                    if (mOnReceivePwdAuthListener != null) {
                        mOnReceivePwdAuthListener.onReceivePwdAuth(isCmdRunSuc);
                    }
                }
            });
        }
    }

    //指令通道
    public void cmd(byte[] cmdByte, onReceiveCmdListener listener) {
        mOnReceiveCmdListener = listener;
        deviceManager.requestRfmUltralightCmd(cmdByte, new DeviceManager.onReceiveRfmUltralightCmdListener() {
            @Override
            public void onReceiveRfmUltralightCmd(boolean isCmdRunSuc, byte[] bytUlRtnData) {
                if (mOnReceiveCmdListener != null) {
                    mOnReceiveCmdListener.onReceiveCmd(bytUlRtnData);
                }
            }
        });
    }
}

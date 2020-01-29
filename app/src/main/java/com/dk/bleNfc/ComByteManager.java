package com.dk.bleNfc;

/**
 * Created by lochy on 16/1/21.
 */
public class ComByteManager {
    private ComByteManagerCallback comByteManagerCallback;

    private int RcvFrameType;
    private int RcvFrameNum;
    private byte RcvCommand;
    private byte[] RcvData;
    private int RcvDataLen;
    private byte[] RcvComRunStatus = new byte[2];
    private static int last_frame_num = 0;

    //Command define
    public final static byte PAL_TEST_CHANNEL = 0x00;       //通讯协议测试通道
    public final static byte MIFARE_AUTH_COM = 0x40;            //MIFARE卡验证密钥指令
    public final static byte MIFARE_COM = 0x41;            //Mifare卡指令通道
    public final static byte ACTIVATE_PICC_COM = 0x62;            //激活卡片指令
    public final static byte APDU_COM = 0x6F;                    //apdu指令
    public final static byte ANTENNA_OFF_COM = 0x6E;            //关闭天线指令
    public final static byte GET_BT_VALUE_COM = 0x70;            //获取电池电量
    public final static byte GET_VERSIONS_COM = 0x71;            //获取设备版本号指令
    public final static byte BPDU_COM = 0x7F;                     //BPFU指令
    public final static byte GET_SUICA_BALANCE_COM = (byte) 0xF0;  //获取SUICA余额指令
    public final static byte FELICA_READ_COM = (byte) 0xF1;         //读FeliCa指令
    public final static byte FELICA_COM = (byte) 0xF2;               //FeliCa指令通道
    public final static byte ULTRALIGHT_CMD = (byte) 0xD0;          //UL卡指令通道

    //Comand run result define
    public final static byte COMAND_RUN_SUCCESSFUL = (byte) 0x90;            //命令运行成功
    public final static byte COMAND_RUN_ERROR = 0x6E;            //命令运行出错

    //Error code defie
    public final static byte NO_ERROR_CODE = 0x00;            //运行正确时的错误码
    public final static byte DEFAULT_ERROR_CODE = (byte) 0x81;            //默认错误码

    public final static byte ISO14443_P3 = 1;
    public final static byte ISO14443_P4 = 2;
    public final static int  PH_EXCHANGE_DEFAULT = 0x0000;
    public final static int  PH_EXCHANGE_LEAVE_BUFFER_BIT = 0x4000;
    public final static int  PH_EXCHANGE_BUFFERED_BIT = 0x8000;
    public final static int PH_EXCHANGE_BUFFER_FIRST = PH_EXCHANGE_DEFAULT | PH_EXCHANGE_BUFFERED_BIT;
    public final static int PH_EXCHANGE_BUFFER_CONT = PH_EXCHANGE_DEFAULT | PH_EXCHANGE_BUFFERED_BIT | PH_EXCHANGE_LEAVE_BUFFER_BIT;
    public final static int PH_EXCHANGE_BUFFER_LAST = PH_EXCHANGE_DEFAULT | PH_EXCHANGE_LEAVE_BUFFER_BIT;


    public final static byte Start_Frame = 0;
    public final static byte Follow_Frame = 1;

    public final static byte MAX_FRAME_NUM = 63;
    public final static byte MAX_FRAME_LEN = 20;

    public final static byte Rcv_Status_Idle = 0;
    public final static byte Rcv_Status_Start = 1;
    public final static byte Rcv_Status_Follow = 2;
    public final static byte Rcv_Status_Complete = 3;

    public ComByteManager(ComByteManagerCallback callback) {
        comByteManagerCallback = callback;
    }

    public byte getCmd() {
        return RcvCommand;
    }

    //获取命令运行状态是否成功
    public boolean getCmdRunStatus() {
        return (RcvComRunStatus[0] == (byte) 0x90);
    }

    public int getRcvDataLen() {
        return RcvDataLen;
    }

    public byte[] getRcvData() {
        if (RcvData == null) {
            return null;
        }
        byte[] bytes = new byte[RcvDataLen];
        System.arraycopy(RcvData, 0, bytes, 0, RcvDataLen);
        return bytes;
    }

    //接收数据处理
    public boolean rcvData(final byte[] bytes) {
        int this_frame_num = 0;
        int status = 0;

        //提取帧类型是开始帧还是后续帧
        if ((bytes[0] & 0xC0) == 0x00) {     //开始帧
            //开始帧必须大于4位
            if (bytes.length < 4) {
                return false;
            }
            RcvFrameType = Start_Frame;
            //如果是开头帧，则提取后续帧个数和命令
            RcvFrameNum = bytes[0] & 0x3F;
            RcvCommand = bytes[1];
            RcvComRunStatus[0] = bytes[2];
            RcvComRunStatus[1] = bytes[3];
            RcvDataLen = bytes.length - 4;
            RcvData = new byte[RcvFrameNum * 19 + RcvDataLen];
            System.arraycopy(bytes, 4, RcvData, 0, RcvDataLen);
            last_frame_num = 0;

            if (RcvFrameNum > 0) {
                status = Rcv_Status_Follow;
            } else {
                status = Rcv_Status_Complete;
            }
        } else if ((bytes[0] & 0xC0) == 0xC0) {   //后续帧
            //后续帧必须大于2位
            if (bytes.length < 2) {
                last_frame_num = 0;
                RcvFrameType = 0;
                RcvFrameNum = 0;
                RcvCommand = 0;
                RcvData = null;
                RcvDataLen = 0;
                RcvComRunStatus[0] = 0;
                RcvComRunStatus[1] = 0;
                return false;
            }
            this_frame_num = bytes[0] & 0x3F;
            if (this_frame_num != (last_frame_num + 1)) {        //帧序号不对
                status = Rcv_Status_Idle;
            } else if (this_frame_num == RcvFrameNum) {  //接收完成
                if (RcvData.length < (RcvDataLen + bytes.length - 1)) {
                    status = Rcv_Status_Idle;
                } else {
                    System.arraycopy(bytes, 1, RcvData, RcvDataLen, bytes.length - 1);
                    RcvDataLen += bytes.length - 1;
                    status = Rcv_Status_Complete;
                }
            } else {                                               //接收中
                if (RcvData.length < (RcvDataLen + bytes.length - 1)) {
                    status = Rcv_Status_Idle;
                } else {
                    last_frame_num = this_frame_num;
                    System.arraycopy(bytes, 1, RcvData, RcvDataLen, bytes.length - 1);
                    RcvDataLen += bytes.length - 1;
                    status = Rcv_Status_Follow;
                }
            }
        } else {
            status = Rcv_Status_Idle;
        }

        //指令接收错误
        if (status == Rcv_Status_Idle) {
            last_frame_num = 0;
            RcvFrameType = 0;
            RcvFrameNum = 0;
            RcvCommand = 0;
            RcvData = null;
            RcvDataLen = 0;
            RcvComRunStatus[0] = 0;
            RcvComRunStatus[1] = 0;
            return false;
        }

        //指令接收完成
        if (status == Rcv_Status_Complete) {  //接收完成、执行命令
            last_frame_num = 0;
            comByteManagerCallback.onRcvBytes(getCmdRunStatus(), getRcvData());
        }

        return true;
//        System.arraycopy(bytes, 0, cmdRcvBytes, cmdRcvLen, bytes.length);
//        cmdRcvLen += bytes.length;
//        if (bytes.length < 20) {
//            byte[] rvcByte = new byte[cmdRcvLen];
//            System.arraycopy(cmdRcvBytes, 0, rvcByte, 0, cmdRcvLen);
//            cmdRcvLen = 0;
//            comByteManagerCallback.onRcvBytes(true, rvcByte);
//            return true;
//        }
//
//        if (mThread != null) {
//            mThread.interrupt();
//            mThread = null;
//        }
//        mThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Thread.sleep(300);
//                    byte[] rvcByte = new byte[cmdRcvLen];
//                    System.arraycopy(cmdRcvBytes, 0, rvcByte, 0, cmdRcvLen);
//                    cmdRcvLen = 0;
//                    comByteManagerCallback.onRcvBytes(true, rvcByte);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    //e.printStackTrace();
//                }
//            }
//        });
//        mThread.start();
//
//        return false;
    }

    //单帧组帧协议
    public byte[] data_framing_single(int frame_type,
                                      byte frame_num,
                                      byte command,
                                      byte[] original_data,
                                      byte original_data_len) {
        byte[] frame_temp;
        int index = 0;
        int i;

        //帧个数判断
        if (frame_num > MAX_FRAME_NUM) {
            return null;
        }

        if ((original_data == null)) {
            return null;
        }

        //起始数据帧
        if (frame_type == Start_Frame) {
            //数据长度过长判断
            if (original_data_len > (MAX_FRAME_LEN - 2)) {
                return null;
            }

            frame_temp = new byte[original_data_len + 2];
            frame_temp[index++] = frame_num;
            frame_temp[index++] = command;
        } else {   //后续数据帧
            //数据长度过长判断
            if (original_data_len > (MAX_FRAME_LEN - 1)) {
                return null;
            }

            frame_temp = new byte[original_data_len + 1];
            frame_temp[index++] = (byte) (0xC0 | frame_num);
        }

        //数据域
        for (i = 0; i < original_data_len; i++) {
            frame_temp[index++] = original_data[i];
        }

        return frame_temp;
    }

    //完整的组帧协议
    public byte[] data_framing_full(byte command, byte[] pSend_data, int send_data_len) {
        byte[] frame_temp = new byte[MAX_FRAME_LEN];
        byte[] returnFrame;
        int frame_num = 0;
        int frame_len = 0;
        int index = 0;
        int copy_data_len;
        int i = 0;

        //计算帧的个数
        if (send_data_len <= (MAX_FRAME_LEN - 2)) {
            frame_num = 0;
            returnFrame = new byte[send_data_len + 2];
        } else {
            frame_num = (send_data_len - (MAX_FRAME_LEN - 2)) / (MAX_FRAME_LEN - 1);
            if (((send_data_len - (MAX_FRAME_LEN - 2)) % (MAX_FRAME_LEN - 1)) > 0) {
                returnFrame = new byte[frame_num * 20/*中间帧*/ + 20/*第一帧*/ + ((send_data_len - (MAX_FRAME_LEN - 2)) % (MAX_FRAME_LEN - 1)) + 1/*最后一帧*/];
                frame_num++;
            } else {
                returnFrame = new byte[frame_num * 20/*后续帧*/ + 20/*第一帧*/];
            }
        }


        //发送第一帧数据
        for (index = 0; (index < send_data_len) && (index < (MAX_FRAME_LEN - 2)); index++) {
            frame_temp[index] = pSend_data[index];
        }

        byte[] frameSingleTemp = data_framing_single(Start_Frame,
                (byte) frame_num,
                command,
                frame_temp,
                (byte) index);
        //将组好的帧发送出去
        if ((frameSingleTemp != null) && (frameSingleTemp.length != 0) && (frameSingleTemp.length <= MAX_FRAME_LEN)) {
            if (frameSingleTemp.length > returnFrame.length) {
                return null;
            }
            frame_len = frameSingleTemp.length;
            System.arraycopy(frameSingleTemp, 0, returnFrame, 0, frameSingleTemp.length);
        } else {
            return null;
        }

        //如果还有后续帧
        if (frame_num > 0) {
            index = MAX_FRAME_LEN - 2;
            for (i = 0; (i < frame_num) && (index < send_data_len); i++) {
                if ((index + (MAX_FRAME_LEN - 1)) > send_data_len) {
                    copy_data_len = ((send_data_len - (MAX_FRAME_LEN - 2)) % (MAX_FRAME_LEN - 1));
                } else {
                    copy_data_len = MAX_FRAME_LEN - 1;
                }

                System.arraycopy(pSend_data, index, frame_temp, 0, copy_data_len);
                index += copy_data_len;
                //组帧
                byte[] frameSingleTemp1 = data_framing_single(Follow_Frame,
                        (byte) (i + 1),
                        (byte) 0,
                        frame_temp,
                        (byte) copy_data_len);
                //将组好的帧发送出去
                if ((frameSingleTemp1 != null) && (frameSingleTemp1.length != 0) && (frameSingleTemp1.length <= MAX_FRAME_LEN)) {
                    if ((frameSingleTemp1.length + frame_len) > returnFrame.length) {
                        return null;
                    }
                    System.arraycopy(frameSingleTemp1, 0, returnFrame, frame_len, frameSingleTemp1.length);
                    frame_len += frameSingleTemp1.length;
                } else {
                    return null;
                }
            }
        }
        return returnFrame;
    }

    //A卡激活指令
    public byte[] AActivityComByte() {
        return new byte[]{0x00, ACTIVATE_PICC_COM};
    }
    public byte[] AActivityComByte(byte protocolLayer) {
        return new byte[]{0x00, ACTIVATE_PICC_COM, protocolLayer};
    }

    //A卡去激活指令
    public byte[] rfPowerOffComByte() {
        return new byte[] {0x00, ANTENNA_OFF_COM};
    }

    //获取蓝牙读卡器电池电压指令
    public byte[] getBtValueComByte() {
        return new byte[] {0x00, GET_BT_VALUE_COM};
    }

    //获取设备版本号指令
    public byte[] getVersionsComByte() {
        return new byte[] {0x00, GET_VERSIONS_COM};
    }

    //非接接口Apdu指令
    public byte[] rfApduCmdByte(byte[] adpuCmd) {
        return data_framing_full(APDU_COM, adpuCmd, adpuCmd.length);
    }

    //Felica读余额指令通道
    public byte[] getSuicaBalanceCmdByte() {
        return new byte[] {0x00, GET_SUICA_BALANCE_COM};
    }

    //Felica读指令通道
    public byte[] readFeliCaCmdByte(byte[] systemCode, byte[] blockAddr) {
        return new byte[] {0x00, FELICA_READ_COM, systemCode[0], systemCode[1], blockAddr[0], blockAddr[1]};
    }

    //Felica指令通道
    public byte[] felicaCmdByte(int wOption, int wN, byte[] dataBytes) {
        byte[] bytesTem = new byte[dataBytes.length + 4];
        bytesTem[0] = (byte) ((wOption >> 8) & 0x00ff);
        bytesTem[1] = (byte) (wOption & 0x00ff);
        bytesTem[2] = (byte) ((wN >> 8) & 0x00ff);
        bytesTem[3] = (byte) (wN & 0x00ff);
        System.arraycopy(dataBytes, 0, bytesTem, 4, dataBytes.length );
        return data_framing_full(FELICA_COM, bytesTem, bytesTem.length);
    }

    //UL指令通道
    public byte[] ultralightCmdByte(byte[] ulCmd) {
        return data_framing_full(ULTRALIGHT_CMD, ulCmd, ulCmd.length);
    }

    //身份证指令接口
    public byte[] rfBpduCmdByte(byte[] bpuCmd) {
        return data_framing_full(BPDU_COM, bpuCmd, bpuCmd.length);
    }

    //Mifare卡验证密码指令
    public byte[] rfMifareAuthCmdByte(byte bBlockNo, byte bKeyType, byte[] pKey, byte[] pUid) {
        byte[] returnByte = new byte[2 + 1 + 1 + 6 + 4];
        returnByte[0] = 0x00;
        returnByte[1] = MIFARE_AUTH_COM;
        returnByte[2] = bBlockNo;
        returnByte[3] = bKeyType;
        System.arraycopy(pKey,0, returnByte, 4, 6);
        System.arraycopy(pUid, 0, returnByte, 10, 4);
        return returnByte;
    }

    //Mifarek卡数据交换指令
    public byte[] rfMifareDataExchangeCmdByte(byte[] dataBytes) {
        return data_framing_full(MIFARE_COM, dataBytes, dataBytes.length);
    }

    //通信协议测试通道指令
    public byte[] getTestChannelBytes(byte[] dataBytes) {
        return data_framing_full(PAL_TEST_CHANNEL, dataBytes, dataBytes.length);
    }
}

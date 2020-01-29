package com.dk.bleNfc.card;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;

import com.dk.bleNfc.DeviceManager;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

/**
 * Created by Administrator on 2016/9/19.
 */
public class Ntag21x extends Ultralight {
    public final static byte TYPE_NTAG213 = 0x0F;
    public final static byte TYPE_NTAG215 = 0x11;
    public final static byte TYPE_NTAG216 = 0x13;
    public final static int SIZE_NTAG213 = 144;
    public final static int SIZE_NTAG215 = 504;
    public final static int SIZE_NTAG216 = 888;

    public final static String ERR_MEMORY_OUT = "Data is too long for this tag!";
    public final static String ERR_WRITE_FAIL = "Write data fail!";
    public final static String ERR_NO_ERROR = "No error";

    public byte type = 0;
    public int size = 0;

    private volatile static boolean isWriteSuc = false;
    private volatile static byte[] readDataBytes;
    private volatile static int readDataLen = 0;
    private volatile static int writeFailCnt = 0;
    private volatile static boolean writeRcvFlag = false;

    public onReceiveLongWriteListener mOnReceiveLongWrite;
    public onReceiveNdefTextWriteListener mOnReceiveNdefTextWriteListener;
    public onReceiveNdefTextReadListener mOnReceiveNdefTextReadListener;
    public onReceiveLongReadListener mOnReceiveLongReadListener;

    public Ntag21x(DeviceManager deviceManager) {
        super(deviceManager);
    }

    public Ntag21x(DeviceManager deviceManager, byte[] uid, byte[] atr) {
        super(deviceManager, uid, atr);
    }

    //任意长度数据写回调
    public interface onReceiveLongWriteListener {
        public void onReceiveLongWrite(String error);
    }

    //任意长度数据读回调
    public interface onReceiveLongReadListener {
        public void onReceiveLongRead(boolean isSuc, byte[] returnBytes);
    }

    //写一个NDEF文本格式到标签回调
    public interface onReceiveNdefTextWriteListener {
        public void onReceiveNdefTextWrite(String error);
    }

    //从标签中读取一个NEDF文本格式的数据回调
    public interface onReceiveNdefTextReadListener {
        public void onReceiveNdefTextRead(String eer, String returnString);
    }

    //任意长度数据写
    public void longWrite(byte startAddress, byte[] writeBytes, onReceiveLongWriteListener listener) {
        mOnReceiveLongWrite = listener;
        final byte[] writeBytesTemp = writeBytes;
        final byte startAddressTemp = startAddress;

        getVersion(new onReceiveGetVersionListener() {
            @Override
            public void onReceiveGetVersion(boolean isSuc, byte[] returnBytes) {
                if (isSuc && returnBytes.length == 8) {
                    type = returnBytes[6];
                    switch (type) {
                        case TYPE_NTAG213:
                            size = SIZE_NTAG213;
                            break;
                        case TYPE_NTAG215:
                            size = SIZE_NTAG215;
                            break;
                        case TYPE_NTAG216:
                            size = SIZE_NTAG216;
                            break;
                        default:
                            break;
                    }

                    //写入数据长度超过卡片容量
                    if (writeBytesTemp.length + (startAddressTemp & 0x00ff)*4 > size) {
                        if (mOnReceiveLongWrite != null) {
                            mOnReceiveLongWrite.onReceiveLongWrite(ERR_MEMORY_OUT);
                        }
                        return;
                    }
                    else {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int currentWriteAddress = startAddressTemp & 0x00ff;
                                byte[] writeByte4 = new byte[4];
                                int i;
                                for (i = 0; i < writeBytesTemp.length / 4; i++, currentWriteAddress++) {
                                    System.arraycopy(writeBytesTemp, i * 4, writeByte4, 0, 4);
                                    isWriteSuc = false;

                                    writeFailCnt = 0;
                                    do {
                                        writeRcvFlag = false;
                                        write((byte) (currentWriteAddress & 0x00ff), writeByte4, new onReceiveWriteListener() {
                                            @Override
                                            public void onReceiveWrite(boolean isSuc, byte[] returnBytes) {
                                                writeRcvFlag = true;
                                                if (!isSuc) {
                                                    writeFailCnt++;
                                                } else {
                                                    isWriteSuc = true;
                                                }
                                            }
                                        });

                                        //等待写入反馈
                                        while (!writeRcvFlag);
                                    }while (!isWriteSuc && (writeFailCnt < 3));

                                    if (!isWriteSuc) {
                                        if (mOnReceiveLongWrite != null) {
                                            mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                        }
                                        return;
                                    }
                                }

                                final byte endAddress = (byte) (currentWriteAddress & 0x00ff);
                                if (writeBytesTemp.length % 4 > 0) {
                                    System.arraycopy(writeBytesTemp, i * 4, writeByte4, 0, writeBytesTemp.length % 4);
                                    isWriteSuc = false;
                                    write((byte) (currentWriteAddress & 0x00ff), writeByte4, new onReceiveWriteListener() {
                                        @Override
                                        public void onReceiveWrite(boolean isSuc, byte[] returnBytes) {
                                            if (!isSuc) {
                                                if (mOnReceiveLongWrite != null) {
                                                    mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                }
                                                return;
                                            } else {
                                                //将写入的数据读出验证是否正确
                                                longRead(startAddressTemp, endAddress, new onReceiveLongReadListener() {
                                                    @Override
                                                    public void onReceiveLongRead(boolean isSuc, byte[] returnBytes) {
                                                        if (!isSuc) {
                                                            if (mOnReceiveLongWrite != null) {
                                                                mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                            }
                                                            return;
                                                        } else if (returnBytes.length >= writeBytesTemp.length) {
                                                            for (int index = 0; index<writeBytesTemp.length; index++) {
                                                                if (returnBytes[index] != writeBytesTemp[index]) {
                                                                    if (mOnReceiveLongWrite != null) {
                                                                        mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                                    }
                                                                    return;
                                                                }
                                                                if (mOnReceiveLongWrite != null) {
                                                                    mOnReceiveLongWrite.onReceiveLongWrite(null);
                                                                }
                                                                return;
                                                            }
                                                        } else {
                                                            if (mOnReceiveLongWrite != null) {
                                                                mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                            }
                                                            return;
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    });
                                } else {
                                    //将写入的数据读出验证是否正确
                                    longRead(startAddressTemp, (byte) ((currentWriteAddress - 1) & 0x00ff), new onReceiveLongReadListener() {
                                        @Override
                                        public void onReceiveLongRead(boolean isSuc, byte[] returnBytes) {
                                            if (!isSuc) {
                                                if (mOnReceiveLongWrite != null) {
                                                     mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                }
                                                return;
                                            } else if (returnBytes.length >= writeBytesTemp.length) {
                                                for (int index = 0; index<writeBytesTemp.length; index++) {
                                                    if (returnBytes[index] != writeBytesTemp[index]) {
                                                        if (mOnReceiveLongWrite != null) {
                                                            mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                        }
                                                        return;
                                                    }
                                                    if (mOnReceiveLongWrite != null) {
                                                        mOnReceiveLongWrite.onReceiveLongWrite(null);
                                                    }
                                                    return;
                                                }
                                            } else {
                                                if (mOnReceiveLongWrite != null) {
                                                    mOnReceiveLongWrite.onReceiveLongWrite(ERR_WRITE_FAIL);
                                                }
                                                return;
                                            }
                                        }
                                    });
                                }
                            }
                        }).start();
                    }
                }
            }
        });
    }

    //任意长度读
    public void longRead(byte startAddress, byte endAddress, onReceiveLongReadListener listener) {
        mOnReceiveLongReadListener = listener;

        final byte startAddressTemp = startAddress;
        final byte endAddressTemp = endAddress;

        if ( (startAddress & 0x00ff) > (endAddress & 0x00ff) ) {
            if (mOnReceiveLongReadListener != null) {
                mOnReceiveLongReadListener.onReceiveLongRead(false, null);
            }
            return;
        }

        if ( UL_MAX_FAST_READ_BLOCK_NUM >= ((endAddress & 0x00ff) - (startAddress & 0x00ff) + 1) ) {
            fastRead(startAddress, endAddress, new onReceiveFastReadListener() {
                @Override
                public void onReceiveFastRead(boolean isSuc, byte[] returnBytes) {
                    if (mOnReceiveLongReadListener != null) {
                        mOnReceiveLongReadListener.onReceiveLongRead(isSuc, returnBytes);
                    }
                }
            });
        }
        else {
            readDataBytes = new byte[((endAddress & 0x00ff) - (startAddress & 0x00ff) + 1) * 4];
            readDataLen = 0;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int currentStartAddress = startAddressTemp & 0x00ff;
                    int currentEndAddress = currentStartAddress + UL_MAX_FAST_READ_BLOCK_NUM - 1;
                    int i;
                    for (i = 0; currentEndAddress < endAddressTemp; i++) {
                        isWriteSuc = false;
                        final int index = i;
                        fastRead((byte) (currentStartAddress & 0x00ff), (byte) (currentEndAddress & 0x00ff), new onReceiveFastReadListener() {
                            @Override
                            public void onReceiveFastRead(boolean isSuc, byte[] returnBytes) {
                                if (!isSuc) {
                                    if (mOnReceiveLongReadListener != null) {
                                        mOnReceiveLongReadListener.onReceiveLongRead(isSuc, returnBytes);
                                    }
                                    return;
                                }
                                isWriteSuc = true;
                                System.arraycopy(returnBytes, 0, readDataBytes, readDataLen, returnBytes.length);
                                readDataLen += UL_MAX_FAST_READ_BLOCK_NUM * 4;
                            }
                        });

                        //等待读取成功
                        while (!isWriteSuc) ;

                        currentStartAddress = (currentEndAddress & 0x00ff) + 1;
                        currentEndAddress += UL_MAX_FAST_READ_BLOCK_NUM;
                    }

                    final int surplusBlock = ( (endAddressTemp & 0x00ff) - (startAddressTemp & 0x00ff) + 1) % UL_MAX_FAST_READ_BLOCK_NUM;
                    if ( surplusBlock != 0 ) {
                        fastRead((byte)(currentStartAddress & 0x00ff), (byte)((currentStartAddress + surplusBlock - 1) & 0x00ff), new onReceiveFastReadListener() {
                            @Override
                            public void onReceiveFastRead(boolean isSuc, byte[] returnBytes) {
                                if (!isSuc) {
                                    if (mOnReceiveLongReadListener != null) {
                                        mOnReceiveLongReadListener.onReceiveLongRead(isSuc, returnBytes);
                                    }
                                    return;
                                }
                                if (mOnReceiveLongReadListener != null) {
                                    System.arraycopy(returnBytes, 0, readDataBytes, readDataLen, returnBytes.length);
                                    readDataLen += returnBytes.length;
                                    mOnReceiveLongReadListener.onReceiveLongRead(isSuc, readDataBytes);
                                }
                            }
                        });
                    }
                    else{
                        if (mOnReceiveLongReadListener != null) {
                            mOnReceiveLongReadListener.onReceiveLongRead(true, readDataBytes);
                        }
                    }
                }
            }).start();
        }
    }

    //写一个NDEF文本格式到标签
    public void NdefTextWrite(String text, onReceiveNdefTextWriteListener listener) {
        mOnReceiveNdefTextWriteListener = listener;

//        byte[] rececrdByte = createTextRecord(text).getPayload();
//        System.out.println(rececrdByte);
//        NdefMessage ndefMessage = new NdefMessage(new NdefRecord[] {createTextRecord(text)});

        NdefRecord m_id = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA,
                "text/plain".getBytes(), new byte[] {},
                text.getBytes());
        NdefRecord[] records = { m_id };
        NdefMessage ndefMessage = new NdefMessage(records);

        byte[] NDEFTextByte = ndefMessage.toByteArray();
        byte[] NDEFHandleByte;
        if (NDEFTextByte.length >= 0xff) {
            NDEFHandleByte = new byte[] {0x03, (byte) 0xff, (byte) ((NDEFTextByte.length >> 8) & 0x00ff), (byte) (NDEFTextByte.length & 0x00ff)};
        }
        else {
            NDEFHandleByte = new byte[] {0x03, (byte) NDEFTextByte.length};
        }

        byte[] writeBytes = new byte[NDEFHandleByte.length + NDEFTextByte.length + 1];

        int index = 0;
        System.arraycopy(NDEFHandleByte, 0, writeBytes, index, NDEFHandleByte.length);
        index += NDEFHandleByte.length;
        System.arraycopy(NDEFTextByte, 0, writeBytes, index, NDEFTextByte.length);
        writeBytes[writeBytes.length - 1] = (byte) 0xFE;

        longWrite((byte)4, writeBytes, new onReceiveLongWriteListener() {
            @Override
            public void onReceiveLongWrite(String error) {
                if (mOnReceiveNdefTextWriteListener != null) {
                    mOnReceiveNdefTextWriteListener.onReceiveNdefTextWrite(error);
                }
            }
        });
    }

    //从标签中读取一个NEDF文本格式的数据
    public void NdefTextRead(onReceiveNdefTextReadListener listener) {
        mOnReceiveNdefTextReadListener = listener;

        read((byte) 4, new onReceiveReadListener() {
            @Override
            public void onReceiveRead(boolean isSuc, byte[] returnBytes) {
                if (!isSuc || returnBytes.length != 16) {
                    if (mOnReceiveNdefTextReadListener != null) {
                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead("Read card fail", null);
                    }
                    return;
                }
                if (returnBytes[0] == 0x03) {
                    if (returnBytes[1] == (byte) 0xff) {
                        final int recordLen = ((returnBytes[2] & 0x00ff) << 8) | (returnBytes[3] & 0x00ff);
                        final byte recordEndAddress = (byte) ((recordLen + 4) / 4 + 4);
                        longRead((byte) 4, recordEndAddress, new onReceiveLongReadListener() {
                            @Override
                            public void onReceiveLongRead(boolean isSuc, byte[] returnBytes) {
                                if (!isSuc || (returnBytes.length < recordLen)) {
                                    if (mOnReceiveNdefTextReadListener != null) {
                                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead("Read card fail", null);
                                    }
                                    return;
                                }

                                byte[] payload = new byte[recordLen - 16];
                                System.arraycopy(returnBytes, 20, payload, 0, payload.length);
                                try {
                                    //解析出实际的文本数据
                                    String text = new String(payload, "UTF-8");

                                    if (mOnReceiveNdefTextReadListener != null) {
                                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead(null, text);
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    if (mOnReceiveNdefTextReadListener != null) {
                                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead("No NDEF text payload!", null);
                                    }
                                    // should never happen unless we get a malformed tag.
                                    throw new IllegalArgumentException(e);
                                }
                            }
                        });
                    } else {
                        final int recordLen = returnBytes[1] & 0x00ff;
                        final byte recordEndAddress = (byte) ((recordLen + 2) / 4 + 4);
                        longRead((byte) 4, recordEndAddress, new onReceiveLongReadListener() {
                            @Override
                            public void onReceiveLongRead(boolean isSuc, byte[] returnBytes) {
                                if (!isSuc || (returnBytes.length < recordLen)) {
                                    if (mOnReceiveNdefTextReadListener != null) {
                                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead("Read card fail", null);
                                    }
                                    return;
                                }

                                byte[] payload = new byte[recordLen - 13];
                                System.arraycopy(returnBytes, 15, payload, 0, payload.length);
                                try {
                                    //解析出实际的文本数据
                                    String text = new String(payload, "UTF-8");

                                    if (mOnReceiveNdefTextReadListener != null) {
                                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead(null, text);
                                    }
                                } catch (UnsupportedEncodingException e) {
                                    if (mOnReceiveNdefTextReadListener != null) {
                                        mOnReceiveNdefTextReadListener.onReceiveNdefTextRead("No NDEF text payload!", null);
                                    }
                                    // should never happen unless we get a malformed tag.
                                    throw new IllegalArgumentException(e);
                                }
                            }
                        });
                    }
                }
            }
        });
    }

    //创建一个封装要写入的文本的NdefRecord对象
    private NdefRecord createTextRecord(String text) {
        //生成语言编码的字节数组，中文编码
        byte[] langBytes = Locale.CHINA.getLanguage().getBytes(
                Charset.forName("US-ASCII"));
        //将要写入的文本以UTF_8格式进行编码
        Charset utfEncoding = Charset.forName("UTF-8");
        //由于已经确定文本的格式编码为UTF_8，所以直接将payload的第1个字节的第7位设为0
        byte[] textBytes = text.getBytes(utfEncoding);
        int utfBit = 0;
        //定义和初始化状态字节
        char status = (char) (utfBit + langBytes.length);
        //创建存储payload的字节数组
        byte[] data = new byte[1 + langBytes.length + textBytes.length];
        //设置状态字节
        data[0] = (byte) status;
        //设置语言编码
        System.arraycopy(langBytes, 0, data, 1, langBytes.length);
        //设置实际要写入的文本
        System.arraycopy(textBytes, 0, data, 1 + langBytes.length,
                textBytes.length);
        //根据前面设置的payload创建NdefRecord对象
        NdefRecord record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], data);
        return record;
    }
}

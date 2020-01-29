package com.beep.beepposconcept.CEPAS;

/**
 * Created by teren on 11/18/2016.
 */

public class HexString {
    private final byte[] mData;

    public HexString(byte[] data) {
        mData = data;
    }

    public HexString(String hex) {
        mData = hexStringToByteArray(hex);
    }

    public byte[] getData() {
        return mData;
    }

    public String toHexString() {
        return getHexString(mData);
    }


    public static String getHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static byte[] hexStringToByteArray(String s) {
        if ((s.length() % 2) != 0) {
            throw new IllegalArgumentException("Bad input string: " + s);
        }

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

}




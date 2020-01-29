package com.beep.beepposconcept.CEPAS;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by teren on 11/18/2016.
 */

public class CEPASResponse {
    public byte[] data;
    public byte SW1;
    public byte SW2;

    private static String byteToHex(byte b) {
        return String.format("0x%02x", b);
    }

    public CEPASResponse(byte[] response) {
        SW1 = response[response.length - 2];
        SW2 = response[response.length -1 ];
        data = Arrays.copyOfRange(response, 0, response.length -2);
        Log.d("CEPASResponse", "SW1: " + byteToHex(SW1) + " SW2: " + byteToHex(SW2) + " Data: " + CEPASCard.bytesToHex(data));
    }
}
package com.beep.beepposconcept.CEPAS;

import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by teren on 11/18/2016.
 */



enum CommandType {
   GET_CHALLENGE
}

public class CEPASCommand {
    public byte CLA = (byte)0x90; //default
    public byte INS;
    public byte P1;
    public byte P2;

    public int commandType;
    private boolean Lc_Present = false;
    private boolean data_Present = false;
    private boolean Le_present = false;

    private byte Lc;
    private byte[] data;
    private byte Le;


    public void setLc(byte set) {
        Lc = set;
        Lc_Present = true;
    }

    public void setData(byte[] set) {
        data = set;
        data_Present = true;
    }

    public void setLe(byte set) {
        Le = set;
        Le_present = true;
    }


    private static void smartWrite(ByteArrayOutputStream stream, byte stuff) {

            stream.write(stuff);

    }

    public byte[] getBytes() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {

            smartWrite(stream, CLA);
            smartWrite(stream, INS);
            smartWrite(stream, P1);
            smartWrite(stream, P2);
            if (Lc_Present)
                smartWrite(stream, Lc);
            if (data_Present)
                stream.write(data);
            if (Le_present)
                smartWrite(stream, Le);
        } catch (Exception e) {
            Log.e("CEPASProtocol", "Ugh error " + e.toString());
        }
        return stream.toByteArray();
    }



}
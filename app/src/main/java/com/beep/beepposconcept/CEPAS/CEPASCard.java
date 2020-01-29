package com.beep.beepposconcept.CEPAS;

import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by teren on 11/18/2016.
 */





public class CEPASCard {
    private String CAN;
    private ArrayList<CEPASPurse> purses;

    private IsoDep isoDep;


    public CEPASCard(Tag tag) throws UnsupportedCardException, IOException {
        boolean contains = false;
        String[] techList = tag.getTechList();
        for(int i = 0; i < tag.getTechList().length; i ++){
            contains = techList[i].equals("android.nfc.tech.NfcB");
            if (contains)
                break;
        }

        if (!contains) {
            throw new UnsupportedCardException();
        }

        isoDep = IsoDep.get(tag);
        isoDep.connect();
    }


    public static String bytesToHex(byte[] bytes) {

        String[] hexArray = new String[bytes.length];
        for(int index = 0; index < bytes.length; index++) {
            hexArray[index] = String.format("0x%02x", bytes[index]);
            // maybe you have to convert your byte to int before this can be done
            // (cannot check reight now)
        }

        return "["  + StringUtils.join(hexArray, ",") + "]";
    }

    public CEPASResponse sendCommand(CEPASCommand _command) throws IOException {
        byte[] command = _command.getBytes();
        Log.d("CEPASCard", "Command sent " + bytesToHex(command));
        return new CEPASResponse(isoDep.transceive(command));

    }

    public void getChallenge() throws IOException {
        CEPASCommand challenge = new CEPASCommand();
        challenge.CLA = (byte)0x00;
        challenge.INS = (byte)0x84;
        challenge.P1 = (byte)0x00;
        challenge.P2 = (byte)0x00;
        challenge.setLe((byte)0x08);
        sendCommand(challenge);

    }
    public CEPASPurse getPurse(int purseId) throws InvalidPurseException, IOException {
        CEPASCommand command = new CEPASCommand();
        command.INS = (byte)0x32;
        command.P1 = (byte)purseId;
        command.P2 = (byte)0x00;
        command.setLe((byte)0x00);
        command.setData(new byte[] { (byte) 0});
        CEPASResponse res = sendCommand(command);
        return new CEPASPurse(res.data);

    }
}

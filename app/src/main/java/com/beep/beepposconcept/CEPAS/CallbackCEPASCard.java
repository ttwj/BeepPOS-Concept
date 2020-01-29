package com.beep.beepposconcept.CEPAS;

import android.util.Log;

import com.abl.glopaylib.ByteUtil;
import com.beep.beepposconcept.CEPAS.CEPASCommand;
import com.beep.beepposconcept.CEPAS.CEPASResponse;
import com.dk.bleNfc.card.Iso14443bCard;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by teren on 11/18/2016.
 */



public class CallbackCEPASCard {
    private String CAN;
    private ArrayList<CEPASPurse> purses;

    public byte[] challengeRand;

    private byte[] termRand;
    public byte[] getTermRand() {
        Log.d(TAG, "Yo getting rand");
        if (termRand ==  null) {
            Log.d(TAG, "Generating Term-Rand");
            SecureRandom random = new SecureRandom();
            byte[] rand = new byte[8];
            random.nextBytes(rand);
            Log.d(TAG, "Term-rand " + rand.toString());
            termRand = rand;
        }
        return termRand;
    }

    private boolean commandActive = false;
    private CEPASCommand lastCommand;

    static String TAG = "CallbackCEPASCard";

    private Iso14443bCard card;
    public CallbackCEPASCard(Iso14443bCard _card) {
        card = _card;
    }

    public interface CardStatusListener {
        public void onCardReady();
    }

    public interface CEPASExchangeListener {
        public void onReceiveCEPASResponse(CEPASResponse response);

    }
    public interface GetChallengeExchangeListener {
        public void onCompleteGetChallenge();
    }

    public interface PurseExchangeListener {
        public void onReceivePurseData(CEPASPurse purse);
    }
    public interface DebitExchangeListener {
        public void onReceiveDebitReceipt(CEPASResponse response);
    }

    public interface SelectFileListener {
        public void onSelectFile();
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

    public void sendCommand(CEPASCommand _command, final CEPASExchangeListener listener)  {
        sendCommand(_command.getBytes(), listener);
    }

    public void sendCommand(final byte[]command, final CEPASExchangeListener listener)  {

        Log.d(TAG, "Command sent " + bytesToHex(command));

        card.bpduExchange(command, new Iso14443bCard.onReceiveBpduExchangeListener() {
            @Override
            public void onReceiveBpduExchange(boolean isCmdRunSuc, byte[] bytBpduRtnData) {
                if (!isCmdRunSuc) {
                    card.close(null);
                    Log.w(TAG, "Failed to run command");
                    return;

                }
                CEPASResponse response = new CEPASResponse(bytBpduRtnData);
                if ((response.SW1 == (byte) 0x90) && (response.SW2 == (byte) 0x00)) {
                    listener.onReceiveCEPASResponse(response);
                }
                else {
                    Log.d(TAG, "Warning: Invalid CEPAS response! " + ByteUtil.byteToHexStr(command));
                    //TODO insert some callback for error
                }

            }
        });
    }




    public void getChallenge(final GetChallengeExchangeListener listener) {
        final CEPASCommand challenge = new CEPASCommand();
        challenge.commandType = 1;
        challenge.CLA = (byte)0x00;
        challenge.INS = (byte)0x84;
        challenge.P1 = (byte)0x00;
        challenge.P2 = (byte)0x00;
        challenge.setLe((byte)0x08);
        sendCommand(challenge, new CEPASExchangeListener() {
            @Override
            public void onReceiveCEPASResponse(CEPASResponse response) {
                if (response.data.length != 8) {
                    Log.w(TAG, "Invalid length of challenge response!");
                }
                challengeRand = response.data;
                listener.onCompleteGetChallenge();
            }
        });
    }
    public void getPurse(final int purseId, final PurseExchangeListener listener) {
        CEPASCommand command = new CEPASCommand();
        command.INS = (byte)0x32;
        command.P1 = (byte)purseId;
        command.P2 = (byte)0x00;
        command.setLe((byte)0x00);
        command.setData(new byte[] { (byte) 0});
        sendCommand(command, new CEPASExchangeListener() {
            @Override
            public void onReceiveCEPASResponse(CEPASResponse response) {
                Log.d(TAG, "Recieved purse data for " + purseId);
                try {
                    listener.onReceivePurseData(new CEPASPurse(response.data));
                }
                catch (InvalidPurseException e) {
                    Log.d(TAG, "Warning: Invalid purse detected");
                }
            }
        });
    }

    public void selectFileCommand(final SelectFileListener listener) {
        Log.d(TAG, "Selecting file!");
        CEPASCommand command = new CEPASCommand();
        command.CLA = (byte)0x00;
        command.INS = (byte)0xA4;
        command.P1 = (byte)0x00;
        command.P2 = (byte)0x00;
        command.setLc((byte)0x02);
        command.setData(new byte[] {(byte)0x3F, (byte)0x00});
        sendCommand(command, new CEPASExchangeListener() {
            @Override
            public void onReceiveCEPASResponse(CEPASResponse response) {
                Log.d(TAG, "Received select file command!");
                listener.onSelectFile();
            }
        });

    }

    public void selectMF(final CEPASExchangeListener listener) {
        Log.d(TAG, "Selecting master file");
        byte[] command = ByteUtil.hexStrToByte("00A40000023F00");
        sendCommand(command, listener);
    }

    public void selectEF(final CEPASExchangeListener listener) {
        Log.d(TAG, "Selecting elementary file");
        byte[] command = ByteUtil.hexStrToByte("00A40000024000");
        sendCommand(command, listener);
    }

    public void getPurseWithAuthentication(final int purseId, final PurseExchangeListener listener) {
        Log.d(TAG, "Getting purse!");
        selectMF(new CEPASExchangeListener() {
            @Override
            public void onReceiveCEPASResponse(CEPASResponse response) {
                selectEF(new CEPASExchangeListener() {
                    @Override
                    public void onReceiveCEPASResponse(CEPASResponse response) {
                        getChallenge(new GetChallengeExchangeListener() {
                            @Override
                            public void onCompleteGetChallenge() {
                                CEPASCommand command = new CEPASCommand();
                                command.INS = (byte)0x32;
                                command.P1 = (byte)purseId;
                                command.P2 = (byte)0x00;
                                command.setLc((byte)0x0A);
                                ByteArrayOutputStream data = new ByteArrayOutputStream();
                                try {
                                    data.write((byte)0x15);
                                    data.write((byte)0x02);
                                    data.write(getTermRand());
                                    command.setData(data.toByteArray());
                                    command.setLe((byte)0x71);
                                    sendCommand(command, new CEPASExchangeListener() {
                                        @Override
                                        public void onReceiveCEPASResponse(CEPASResponse response) {
                                            Log.d(TAG, "Recieved purse data for " + purseId);
                                            try {
                                                listener.onReceivePurseData(new CEPASPurse(response.data));
                                            }
                                            catch (InvalidPurseException e) {
                                                Log.d(TAG, "Warning: Invalid purse detected");
                                            }

                                            //TODO: Implement check to determine if purse data is valid
                                        }
                                    });
                                }
                                catch (Exception e) {
                                    Log.e(TAG, "Ugh error " + e.toString());
                                }
                            }
                        });

                    }
                });
            }
        });

    }

    public void debitGetReceipt(byte debitOption, byte Pf, byte debitKf, byte debitKn, byte skf, byte skn, byte[] debitCryptogram, byte[]txData, final DebitExchangeListener listener) {
        CEPASCommand command = new CEPASCommand();
        command.INS = (byte) 0x34; //debit command
        // command.P1 RFU
        command.P2 = debitOption;
        command.setLc((byte) 0x25);
        command.setLe((byte) 0x18);
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            data.write(Pf);
            data.write(debitKf);
            data.write(debitKn);
            data.write(skf);
            data.write(skn);
            data.write(getTermRand());
            data.write(debitCryptogram);
            data.write(txData);
            command.setData(data.toByteArray());
            sendCommand(command, new CEPASExchangeListener() {
                @Override
                public void onReceiveCEPASResponse(CEPASResponse response) {
                    Log.d(TAG, "Received debit receipt!");
                    listener.onReceiveDebitReceipt(response);
                }
            });
        }
        catch (Exception e) {
            Log.e(TAG, "Ugh error " + e.toString());
        }

    }
}

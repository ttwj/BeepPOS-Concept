package com.beep.beepposconcept.CEPAS;

import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Created by teren on 11/18/2016.
 */

public class CEPASPurse {
    public int mAutoLoadAmount;
    public HexString mCAN;
    public byte mCepasVersion;
    public HexString mCSN;
    public int mId;
    public int mIssuerDataLength;
    public HexString mIssuerSpecificData;
    public HexString mLastCreditTransactionHeader;
    public int mLastCreditTransactionTRP;
    public byte mLastTransactionDebitOptionsByte;
    public int mLastTransactionTRP;
    public byte mLogfileRecordCount;
    public int mPurseBalance;
    public int mPurseExpiryDate;
    public byte mPurseStatus;
    public int mPurseCreationDate;
    public boolean mIsValid;
    public HexString mBIN;
    public byte mKsi;

    public byte[] responseData;


    public double getPurseBalance() {
        return (double) mPurseBalance/100.0;
    }

    public CEPASPurse( byte[] purseData) throws InvalidPurseException {
        int tmp;
        if (purseData == null) {
            purseData = new byte[128];
            mIsValid = false;
            throw new InvalidPurseException();
        } else {
            mIsValid = true;
        }

        ByteArrayOutputStream _data = new ByteArrayOutputStream();
        try {
            _data.write(purseData);
            _data.write((byte)0x90);
            _data.write((byte)0x00);
            responseData = _data.toByteArray();
        }

        catch (Exception e) {
            Log.d("CEPASPurse", "Error: can't read purse");
        }



        mCepasVersion = purseData[0];
        mPurseStatus  = purseData[1];

        tmp = (0x00ff0000 & ((purseData[2])) << 16) | (0x0000ff00 & (purseData[3] << 8)) | (0x000000ff & (purseData[4]));
        /* Sign-extend the value */
        if (0 != (purseData[2] & 0x80))
            tmp |= 0xff000000;
        mPurseBalance = tmp;

        tmp = (0x00ff0000 & ((purseData[5])) << 16) | (0x0000ff00 & (purseData[6] << 8)) | (0x000000ff & (purseData[7]));
        /* Sign-extend the value */
        if (0 != (purseData[5] & 0x80))
            tmp |= 0xff000000;
        mAutoLoadAmount = tmp;

        byte[] can = new byte[8];
        for (int i=0; i<can.length; i++) {
            can[i] = purseData[8 + i];
        }
        mCAN = new HexString(can);

        byte[] bin = new byte[3];
        for (int i=0; i<bin.length; i++) {
            bin[i] = purseData[8 + i];
        }

        mBIN = new HexString(bin);

        Log.d("CEPASPurse", "mBIN " + mBIN.toHexString());

        byte[] csn = new byte[8];
        for (int i=0; i<csn.length; i++) {
            csn[i] = purseData[16 + i];
        }

        mCSN = new HexString(csn);

        /* Epoch begins January 1, 1995 */
        mPurseExpiryDate   = 788947200 + (86400 * ((0xff00 & (purseData[24] << 8)) | (0x00ff & (purseData[25] << 0))));
        mPurseCreationDate = 788947200 + (86400 * ((0xff00 & (purseData[26] << 8)) | (0x00ff & (purseData[27] << 0))));

        mLastCreditTransactionTRP = ((0xff000000 & (purseData[28] << 24))
                | (0x00ff0000 & (purseData[29] << 16))
                | (0x0000ff00 & (purseData[30] << 8))
                | (0x000000ff & (purseData[31] << 0)));

        byte[] lastCreditTransactionHeader = new byte[8];

        for (int i = 0; i < 8; i++) {
            lastCreditTransactionHeader[i] = purseData[32 + i];
        }

        mLastCreditTransactionHeader = new HexString(lastCreditTransactionHeader);

        mLogfileRecordCount = purseData[40];

        mIssuerDataLength = 0x00ff & purseData[41];

        mLastTransactionTRP = ((0xff000000 & (purseData[42] << 24))
                | (0x00ff0000 & (purseData[43] << 16))
                | (0x0000ff00 & (purseData[44] << 8))
                | (0x000000ff & (purseData[45] << 0))); {
            byte[] tmpTransaction = new byte[16];
            for (int i = 0; i < tmpTransaction.length; i++)
                tmpTransaction[i] = purseData[46+i];
            //mLastTransactionRecord = new CEPASTransaction(tmpTransaction);
        }

        byte[] issuerSpecificData = new byte[mIssuerDataLength];
        for (int i = 0; i < issuerSpecificData.length; i++) {
            issuerSpecificData[i] = purseData[62+i];
        }
        mIssuerSpecificData = new HexString(issuerSpecificData);

        mLastTransactionDebitOptionsByte = purseData[62+mIssuerDataLength];

        mKsi = purseData[71];

        Log.d("CEPASPurse", "BIN: " + mBIN.toHexString() + " CAN " + mCAN.toHexString() + " CSN " + mCSN.toHexString());
    }

}

package com.beep.beepposconcept.MGG;

/**
 * Created by ttwj on 6/12/16.
 */

import android.util.Log;

import com.abl.glopaylib.ByteUtil;
import com.abl.glopaylib.CryptoUtil;
import com.abl.glopaylib.KeyManager;
import com.abl.glopaylib.LoginState;
import com.abl.glopaylib.QRData;
import com.beep.beepposconcept.Bluetooth.BluetoothManager;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.exception.ExceptionContext;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

import com.beep.beepposconcept.CEPAS.*;

public class MGGTransaction {

    public static String TAG = MGGTransaction.class.getSimpleName();


    //params for first step of transaction
    public String MGGMerchantID;
    public String MGGMerchantSubID;
    public double txAmount;

    private int debitAmount;

    //results from first step of transaction


    private String MGGtxnID;

    private String jwt_data;
    private String token_data;

    private MGGTransactionHandler handler;


    private BluetoothManager bluetoothManager;


    public MGGTransaction(String merchantID, String merchantSubID, double amount, BluetoothManager manager, final MGGTransactionHandler _handler) {
        bluetoothManager = manager;
        MGGMerchantID = merchantID;
        MGGMerchantSubID = merchantSubID;
        txAmount = amount;
        handler = _handler;

    }

    public void initTransaction() {

        MGGClient.initializeTransaction(MGGMerchantID, MGGMerchantSubID, txAmount, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.getBoolean("notFound") == false) {
                        jwt_data = response.getString("data");
                        token_data = response.getString("token");
                        /*Claims dataJwt = (Claims) Jwts.parser().parseClaimsJws(jwt_data);
                        data_iss = dataJwt.getIssuer();
                        //tokenJwt = Jwts.parser().parse(response.getString("token"));*/
                        MGGtxnID = Integer.toString(response.getInt("MGGtxnID"));
                        Log.d(TAG, "Step 1 ok");
                        handler.onCreateTransaction();
                        getPayData();
                    } else {
                        Log.d(TAG, "Warning: Failed to initialize transaction, notFound = true?!");
                        return;
                    }
                }
                catch (JSONException e) {
                    Log.d(TAG, "Warning: JSON Exception occured " + e.toString());
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                Log.d("Failed: ", ""+statusCode);
                Log.d("Error : ", "" + throwable);
            }
        });
    }

    //results from step 2 of transaction
    private String paydata;
    private QRData qrData;

    public void getPayData() {
        MGGClient.getPayData(jwt_data, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    if (response.getString("error").length() > 0) {
                        Log.d(TAG, "Error: Failed to initalize step 2 of transaction!");
                        return;
                    }
                    else {
                        paydata = StringEscapeUtils.unescapeJava(response.getString("paydata"));
                        qrData = new QRData(paydata);
                        Log.d(TAG, "Got QR qrData!");
                        debitAmount = Integer.valueOf(qrData.getAmount());

                        getPurseData();

                    }
                }
                catch (JSONException e) {
                    Log.d(TAG, "Warning: JSON Exception occured " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public void getCEPASCard() {
        bluetoothManager.getCEPASCard(new BluetoothManager.getCEPASCardListener() {
            @Override
            public void onReceive(CallbackCEPASCard card) {
                cepasCard = card;
            }

            @Override
            public void onCardNotFound() {
                Log.d(TAG, "Warning: Card not found!");
                handler.onCardNotFound();
            }
        });

    }

    private CallbackCEPASCard cepasCard;
    public void getPurseData() {
        bluetoothManager.getCEPASCard(new BluetoothManager.getCEPASCardListener() {
            @Override
            public void onReceive(CallbackCEPASCard card) {
                cepasCard = card;
                card.getPurseWithAuthentication(3, new CallbackCEPASCard.PurseExchangeListener() {
                    @Override
                    public void onReceivePurseData(CEPASPurse purse) {

                        if (!purse.mBIN.toHexString().startsWith("100")) {
                            Log.d(TAG, "Warning! Unsupported card");
                            handler.onError(MGGTransactionHandler.MGGTransactionError.PURSE_INSUFFICIENT_BALANCE);
                        }
                        else if (txAmount > cepasPurse.getPurseBalance()) {
                            Log.d(TAG, "Warnining: Insufficient balance in card!");
                            handler.onError(MGGTransactionHandler.MGGTransactionError.PURSE_INSUFFICIENT_BALANCE);
                        }
                        else {
                            getDebitCryptogram(purse);
                        }
                    }
                });
            }
        });

    }


    private static KeyManager keyManager = new KeyManager("uy62dorl10es[ar#42dbd!th");


    private CEPASPurse cepasPurse;

    private byte[] transdt;
    private byte[] transAmt;

    public void getDebitCryptogram(final CEPASPurse purse) {
        cepasPurse = purse;
        transAmt = new byte[3];
        ByteUtil.convertIntToByteArray(-1 * debitAmount, transAmt);
        String macInString = "500000";
        macInString = macInString + "1.0"
                + ByteUtil.byteToHexStr(transType)
                + ByteUtil.byteToHexStr(transAmt)
                //+ ByteUtil.byteToHexStr(purse.mKsi)
                + ByteUtil.byteToHexStr(skn)
                + ByteUtil.byteToHexStr(skf)
                + ByteUtil.byteToHexStr(Pf)
                + ByteUtil.byteToHexStr(cepasCard.getTermRand())
                + ByteUtil.byteToHexStr(cepasCard.challengeRand)
                + ByteUtil.byteToHexStr(debitOptions)
                + qrData.getRefNo()
                + ByteUtil.byteToHexStr(purse.responseData)
                + qrData.getSignature()
                + false //retapflag
                + "B"
                + ByteUtil.byteToHexStr(debitKn);


        try {
            Log.d(TAG, "macInString " + macInString);
            byte[] mac = CryptoUtil.retailMac(keyManager.getKey(), macInString.getBytes());
            MGGClient.getDebitCryptogram(ByteUtil.byteToHexStr(transAmt), cepasCard.getTermRand(),
                    cepasCard.challengeRand, debitOptions, qrData.getRefNo(), purse.responseData,
                    qrData.getSignature(), false, mac, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            super.onSuccess(statusCode, headers, response);
                            try {
                                byte[] debitCryptogram = ByteUtil.hexStrToByte(response.getString("debitcryptogram"));
                                transdt = ByteUtil.hexStrToByte(response.getString("transdt"));
                                byte[] transactionUserData = ByteUtil.hexStrToByte(response.getString("txnuserdata"));
                                sendDebitCommand(debitCryptogram, transactionUserData);
                            }
                            catch (Exception e) {
                                Log.d(TAG, "Error oh no");
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            super.onFailure(statusCode, headers, throwable, errorResponse);
                        }
                    });
        }
        catch (Exception e) {
            Log.d(TAG, "Error oh no");
            e.printStackTrace();
        }

    }

    private static final byte debitKf = (byte) 0x15;
    private static final byte debitKn = (byte) 0x02;
    private static final byte skf = (byte) 0x14;
    private static final byte skn = (byte) 0x03;
    private static final byte Pf = (byte) 0x03;
    private static final byte debitOptions = (byte) 0x00;
    private static final byte transType = (byte)0xA0;



    public void sendDebitCommand(byte[] debitCryptogram, byte[] transactionUserData) {
        Log.d(TAG, "Send debit command");

        cepasCard.debitGetReceipt(debitOptions, Pf, debitKf, debitKn, skf, skn, debitCryptogram, transactionUserData, new CallbackCEPASCard.DebitExchangeListener() {
                    @Override
                    public void onReceiveDebitReceipt(CEPASResponse response) {
                        Log.d(TAG, "Recevied debit receipt");
                        byte[] debitReceiptCryptogram = response.data;
                        verifyDebitCryptogram(debitReceiptCryptogram);
                    }
                });


    }

    public void beginSearchCard() {

    }
    public void verifyDebitCryptogram(byte[] debitReceiptCryptogram) {
        String macInString = "300000";


        macInString = macInString + "1.0"
                + qrData.getUserId()
                + cepasPurse.mBIN.toHexString()
                + cepasPurse.mCAN.toHexString()
                + cepasPurse.mCSN.toHexString()
                + ByteUtil.byteToHexStr(transType)
                + ByteUtil.byteToHexStr(transdt)
                + ByteUtil.byteToHexStr(cepasCard.getTermRand())
                + ByteUtil.byteToHexStr(cepasCard.challengeRand)
                + ByteUtil.byteToHexStr(transAmt)
                + ByteUtil.byteToHexStr(debitOptions)
                + ByteUtil.byteToHexStr(debitReceiptCryptogram)
                + ""
                + qrData.getRefNo()
                + ByteUtil.byteToHexStr(debitKn);



        try {
            byte[] mac = CryptoUtil.retailMac(keyManager.getKey(), macInString.getBytes());
            // public static void verifyDebitReceipt(String userID, HexString bin, HexString can, HexString csn, byte transType, byte[] transdt, byte[] termRandom, byte[] cardRandom, byte[] transamt, byte[] debitReceiptCryptogram, String refno, byte[] mac, AsyncHttpResponseHandler responseHandler) {

            MGGClient.verifyDebitReceipt(qrData.getUserId(), cepasPurse.mBIN, cepasPurse.mCAN, cepasPurse.mCSN, transType, transdt, cepasCard.getTermRand(), cepasCard.challengeRand, transAmt, debitReceiptCryptogram, qrData.getRefNo(), mac, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    super.onSuccess(statusCode, headers, response);
                    Log.d(TAG, "Got debit receipt!");
                    try {
                        byte[] debitReceipt = ByteUtil.hexStrToByte((String)response.get("debitreceipt"));
                        finalizeTransaction(debitReceipt);
                    }
                    catch (Exception e) {
                        Log.d(TAG, "Error: JSON Error ");
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    super.onFailure(statusCode, headers, throwable, errorResponse);
                }
            });
        }
        catch (Exception e) {
            Log.d(TAG, "Warning: Failed to generate MAC!");
            e.printStackTrace();
        }

    }

    public void finalizeTransaction(byte[] debitReceipt) {
       String finalAmount = getFormattedAmount(debitReceipt, 0, 3);
        String originalAmount = Double.toString(cepasPurse.getPurseBalance());
        MGGClient.finalizePayment(token_data, finalAmount, "success", qrData.getRefNo(), cepasPurse.mCAN.toString(), originalAmount, MGGtxnID, "001", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
               try {
                   if (response.getString("result") == "success") {
                       Log.d(TAG, "IT WORKS!!!");
                   }
                   else {
                       Log.d(TAG, "Error: Unknown response " + response.getString("result"));
                   }

               }
               catch (JSONException e) {
                    Log.d(TAG, "Error: JSON Exception occured");
                   e.printStackTrace();
               }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    private static String getFormattedAmount(byte[] bytes, int offset, int len) {
        double amount = Integer.parseInt(ByteUtil.byteToHexStr(bytes, offset, len), 16) / 100.0;
        return Double.toString(amount);
    }

}

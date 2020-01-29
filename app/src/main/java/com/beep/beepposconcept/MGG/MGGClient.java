package com.beep.beepposconcept.MGG;

/**
 * Created by ttwj on 5/12/16.
 */


import android.util.Log;

import com.abl.glopaylib.ByteUtil;
import com.beep.beepposconcept.CEPAS.HexString;
import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.entity.StringEntity;

public class MGGClient {
        private static final String INIT_URL = "https://prd.ezlmerchant.mggsoftware.com:1337/init";
    private static final String GET_PAYDATA_URL = "https://payment.mggsoftware.com:9443/pgweb/1/initpay/ezlinknfc";
    private static final String DEBIT_URL = "https://payment.mggsoftware.com:9449/";
    private static final String PAYMENT_FINALIZE_URL = "https://prd.ezlmerchant.mggsoftware.com:1337/transactions/step2";

    private static AsyncHttpClient client = new AsyncHttpClient();
    private static SyncHttpClient syncHttpClient = new SyncHttpClient();

    public static void initializeTransaction(String merchantID, String merchantSubID, double amount, AsyncHttpResponseHandler responseHandler) {
        client.setProxy("192.168.1.224", 8888);
        syncHttpClient.setProxy("192.168.1.224", 8888);
        RequestParams params = new RequestParams();
        params.add("MGGMerchantID", merchantID);
        params.add("MGGMerchantSubID", merchantSubID);
        params.add("txAmount", Double.toString(amount));
        client.get(INIT_URL, params, responseHandler);
    }

    public static void getPayData(String jwt_token, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.add("jwt", jwt_token);
        client.post(GET_PAYDATA_URL, params, responseHandler);

    }

    public static void getDebitCryptogram(String transamt, byte[] termRandom, byte[] cardRandom, byte debitOptions, String refNo, byte[] purseData, String signature, Boolean retapFlag, byte[] mac, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        JSONObject object = new JSONObject();

        try {
            object.put("type", "500000");
            object.put("version", "1.0");
            object.put("transtype", "A0");
            object.put("transamt", transamt);
            object.put("skn", "03");
            object.put("skf", "14");
            object.put("pf", "03");
            object.put("termrandom", ByteUtil.byteToHexStr(termRandom));
            object.put("cardrandom", ByteUtil.byteToHexStr(cardRandom));
            object.put("debitoptions", "00");
            object.put("refno", refNo);
            object.put("pursedata", ByteUtil.byteToHexStr(purseData));
            object.put("signature", signature);
            object.put("retapflag", retapFlag);
            object.put("cardtype", "B");
            object.put("debitKn", "02");
            object.put("mac", ByteUtil.byteToHexStr(mac));

            StringEntity entity = new StringEntity(object.toString());
            syncHttpClient.post(null, DEBIT_URL, entity, "application/json", responseHandler);
        }
        catch (Exception e) {
            Log.d("MGGClient", "Exception occured while generating debit cryptogram");
            e.printStackTrace();
        }

    }

    public static void verifyDebitReceipt(String userID, HexString bin, HexString can, HexString csn, byte transType, byte[] transdt, byte[] termRandom, byte[] cardRandom, byte[] transamt, byte[] debitReceiptCryptogram, String refno, byte[] mac, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        JSONObject object = new JSONObject();
        try {
            object.put("type", "300000");
            object.put("version", "1.0");
            object.put("userid", userID);
            object.put("bin", bin.toHexString());
            object.put("can", can.toHexString());
            object.put("csn", csn.toHexString());
            object.put("transtype", ByteUtil.byteToHexStr(transType));
            object.put("transdt", ByteUtil.byteToHexStr(transdt));
            object.put("termrandom", ByteUtil.byteToHexStr(termRandom));
            object.put("cardrandom", ByteUtil.byteToHexStr(cardRandom));
            object.put("transamt", ByteUtil.byteToHexStr(transamt));
            object.put("debitoptions", "00");
            object.put("debitreceiptcryptogram", ByteUtil.byteToHexStr(debitReceiptCryptogram));
            object.put("paymentid", "");
            object.put("refno", refno);
            object.put("debitKn", "02");
            object.put("mac", ByteUtil.byteToHexStr(mac));

            StringEntity entity = new StringEntity(object.toString());
            syncHttpClient.post(null, DEBIT_URL, entity, "application/json", responseHandler);
        }
        catch (Exception e) {
            Log.d("MGGClient", "Exception occured while generating debit cryptogram");
            e.printStackTrace();
        }
    }
    public static void finalizePayment(String token, String finalAmountInCard, String ABLtxnStatus, String ABLtxnID, String CANID, String originalAmountInCard, String MGGtxnID, String MerchantTxnID, AsyncHttpResponseHandler responseHandler) {
        RequestParams params = new RequestParams();
        params.add("FinalAmountDeducted", finalAmountInCard);
        params.add("ABLtxnStatus", ABLtxnStatus);
        params.add("ABLtxnID", ABLtxnID);
        params.add("CANID", CANID);
        params.add("OriginalAmount", originalAmountInCard);
        params.add("MGGtxnID", MGGtxnID);
        params.add("MerchanttxID", MerchantTxnID);
        syncHttpClient.addHeader("Authorization", "Bearer " + token);
        syncHttpClient.get( PAYMENT_FINALIZE_URL, params, responseHandler);

    }


}

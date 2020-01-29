package com.beep.beepposconcept.MGG;

import com.beep.beepposconcept.CEPAS.CEPASCard;

/**
 * Created by ttwj on 10/12/16.
 */



public interface MGGTransactionHandler {

    public enum MGGTransactionError {
        PURSE_INSUFFICIENT_BALANCE
    }

    public void onCreateTransaction();

    public void onError(MGGTransactionError error);


    public void onBeginSearchCard();

    public void onCardFound(CEPASCard card);

    public void onCardNotFound();

    public void onStatus(String status);

    public void onTransactionComplete();
}

package com.beep.beepposconcept;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.abl.glopaylib.ByteUtil;
import com.beep.beepposconcept.Bluetooth.BluetoothManager;
import com.beep.beepposconcept.CEPAS.CEPASCard;
import com.beep.beepposconcept.CEPAS.CEPASPurse;
import com.beep.beepposconcept.CEPAS.CEPASResponse;
import com.beep.beepposconcept.CEPAS.CallbackCEPASCard;
import com.beep.beepposconcept.MGG.MGGTransaction;
import com.beep.beepposconcept.MGG.MGGTransactionHandler;

/**
 * Created by ttwj on 7/12/16.
 */

public class SessionPOSScreenFragment extends Fragment {
    private View mView;
    private GridView buttonInputGridView;
    private SessionPOSButtonCustomAdapter buttonAdapter;
    private Button chargeButton;
    private BluetoothManager bluetoothManager;
    private TextView priceToCharge;
    static String inputReader = "";


    private static String TAG = SessionPOSScreenFragment.class.getSimpleName();
    private final Fragment thisFragment = this;




    private ProgressDialog showProgress;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        bluetoothManager = PrimaryWelcomeFragment.getConfirmedManager();
       //bluetoothManager.read_card();

        mView = inflater.inflate(R.layout.activity_session_welcome_fragment, container, false);
        buttonInputGridView = (GridView) mView.findViewById(R.id.session_posscreen_buttons_gridview);
        priceToCharge = (TextView) mView.findViewById(R.id.session_posscreen_price_to_charge_text_view);
        chargeButton = (Button) mView.findViewById(R.id.session_posscreen_button_to_charge);


        byte[] transamt = ByteUtil.hexStrToByte("FFFFF6");
        Log.d(TAG, "Transamt " + CEPASCard.bytesToHex(transamt));


        chargeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Charge pressed!");

                showProgress = new ProgressDialog(getActivity());
                showProgress.setMessage("Place card..");
                showProgress.setCancelable(false);
                showProgress.setIndeterminate(true);


                final MGGTransaction transaction = new MGGTransaction("60003", "0", Double.parseDouble(inputReader), bluetoothManager, new MGGTransactionHandler() {

                    //yo the steps
                    /*
                    * 1. onBeginSearchCard()
                    * 2. If card is not found, onCardNotFound(), end step 2. Else, continue to step 3
                    * 3. onCreateTransaction()
                    * 4. onStatus(message)
                    * 5. onTransactionComplete
                    *
                    * Status updates: onStatus(status)
                    * Error messages: onError(error)
                    * */

                    @Override
                    public void onCreateTransaction() {
                        //TODO: update progress bar

                    }

                    @Override
                    public void onError(MGGTransactionError error) {
                        //TODO: show some error
                    }

                    @Override
                    public void onBeginSearchCard() {
                        showProgress.show();
                    }

                    @Override
                    public void onCardFound(CEPASCard card) {
                        showProgress.setMessage("Processing transaction.. ");
                        transaction.initTransaction();
                    }

                    @Override
                    public void onCardNotFound() {
                        showProgress.setMessage("Transaction failed");
                        showProgress.setCancelable(true);
                    }

                    @Override
                    public void onStatus(String status) {

                    }

                    @Override
                    public void onTransactionComplete() {
                        showProgress.setMessage("Transaction complete");
                        showProgress.setCancelable(true);

                    }
                });
                transaction.beginSearchCard();
                //transaction.initTransaction();
                //transaction.getPurseData();

            }
        });
        buttonAdapter = new SessionPOSButtonCustomAdapter(getActivity(),
                R.layout.session_pos_screen_custom_button, new Object[]{}, priceToCharge);
        buttonInputGridView.setAdapter(buttonAdapter);
        buttonInputGridView.setNumColumns(3);
        buttonInputGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("Clicked Button", position + "");
                if (position < 9) {
                    SessionPOSScreenFragment.inputReader = SessionPOSScreenFragment.
                            inputReader.concat("" + (position + 1));
                    //checkValidAmount();
                    priceToCharge.setText(SessionPOSScreenFragment.inputReader);
                } else {
                    switch (position) {
                        case 9:
                            SessionPOSScreenFragment.inputReader = SessionPOSScreenFragment
                                    .inputReader.concat("0");
                            //checkValidAmount();
                            priceToCharge.setText(SessionPOSScreenFragment.inputReader);
                            break;
                        case 10:
                            if (!checkForPeriod()) {
                                SessionPOSScreenFragment.inputReader = SessionPOSScreenFragment.
                                        inputReader.concat(".");
                            }
                            //checkValidAmount();
                            priceToCharge.setText(SessionPOSScreenFragment.inputReader);
                            break;
                        case 11:
                            if (SessionPOSScreenFragment.inputReader.length() > 0) {
                                SessionPOSScreenFragment.inputReader = SessionPOSScreenFragment.
                                        inputReader.substring(0,
                                        SessionPOSScreenFragment.inputReader.length() - 1);
                            }
                            //Log.i("Double Values", Double.parseDouble(inputReader) + " " + balanceInDollars);
                            //checkValidAmount();
                            priceToCharge.setText(SessionPOSScreenFragment.inputReader);
                            break;
                    }
                }
            }
        });

        return mView;
    }

    private boolean checkForPeriod() {
        return inputReader.contains(".");
    }
}

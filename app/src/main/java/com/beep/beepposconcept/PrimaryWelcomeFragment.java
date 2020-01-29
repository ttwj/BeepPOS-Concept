package com.beep.beepposconcept;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.beep.beepposconcept.Bluetooth.BluetoothManager;
import com.beep.beepposconcept.Bluetooth.BluetoothManagerCallback;
import com.beep.beepposconcept.CEPAS.CallbackCEPASCard;

/**
 * Created by Jacek on 3/12/16.
 */

public class PrimaryWelcomeFragment extends Fragment implements BluetoothManagerCallback {
    private TextView userNameTextField;
    private AppCompatActivity parentActivity;
    private ProgressDialog showProgress;
    private Button pairButton;
    private Button readCardButton;

    private static BluetoothManager confirmedManager;
    private BluetoothManager bluetoothManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothManager = new BluetoothManager(this, getActivity(), getActivity());
    }

    @Override
    public void onAttach(Context context) {
        parentActivity = (AppCompatActivity) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_primary_welcome_fragment, container, false);
        userNameTextField = (TextView) v.findViewById(R.id.primary_welcome_username_textfield);
        pairButton = (Button) v.findViewById(R.id.primary_welcome_pair_button);
        readCardButton = (Button) v.findViewById(R.id.primary_welcome_read_card_button);
        userNameTextField.setText("Welcome, " + UserSessionManager.CURRENT_SESSION_USER_NAME);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Toolbar myToolbar = (Toolbar) parentActivity.findViewById(R.id.my_toolbar);
        parentActivity.setSupportActionBar(myToolbar);
        parentActivity.getSupportActionBar().setTitle("Welcome");
        super.onActivityCreated(savedInstanceState);

        readCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothManager.read_card();
            }
        });

        pairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean bluetooth = Utils.checkBluetoothConnection();
                if(!bluetooth) {
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    bluetoothAdapter.enable();
                }

                //bluetoothManager.requestSinglePermission();
                bluetoothManager.searchAndConnect();
                showProgress = new ProgressDialog(parentActivity);
                showProgress.setMessage("Attempting To Connect");
                showProgress.setCancelable(false);
                showProgress.setIndeterminate(true);
                showProgress.show();
            }
        });
    }

    private void getPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                       10);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    /* functions for BlueoothManagerCallback */
    @Override
    public void onReceiveCEPASCard(CallbackCEPASCard card) {
        Log.d("PrimaryWelcomeFragment", "CEPASCard received");
    }

    @Override
    public void onError(BluetoothManagerError error) {
        final BluetoothManagerError getError = error;
        final ProgressDialog showProg = showProgress;
        Log.d("PrimaryWelcomeFragment", "Error! " + getError.toString());
        new Thread() {
            public void run(){
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(parentActivity, "Error Connecting To Reader: " +
                                        getError.toString(),
                                Toast.LENGTH_LONG).show();

                        showProg.cancel();
                    }
                });
            }
        }.start();
    }


    //Called when paired with the reader
    @Override
    public void onStatus(BluetoothManagerStatus status) {
        Log.d("PrimaryWelcomeFragment", "Status :-)");
        switch (status){
            case DEVICE_CONNECTED:
                showProgress.dismiss();
                confirmedManager = bluetoothManager;
                Intent intent = new Intent(parentActivity, SessionMainActivity.class);
                startActivity(intent);
                break;
        }
    }

    public static BluetoothManager getConfirmedManager(){
        return confirmedManager;
    }

}

package com.beep.beepposconcept;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Jacek on 3/12/16.
 */

public class LogInActivity extends AppCompatActivity{
    private EditText merchantID, userName, passWord;
    private Button logIn;
    private UserSessionManager currentSessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        currentSessionManager = UserSessionManager.
                createOrGetUserSessionManager(this);
        currentSessionManager.setContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        merchantID = (EditText) findViewById(R.id.log_in_merchant_id_edit_text);
        userName = (EditText) findViewById(R.id.log_in_username_edit_text);
        passWord = (EditText) findViewById(R.id.log_in_password_edit_text);
        logIn = (Button) findViewById(R.id.log_in_button);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentSessionManager.bypass(userName.getText().toString(),
                        passWord.getText().toString(),
                        merchantID.getText().toString());
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

}

package com.beep.beepposconcept;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by ttwj on 7/12/16.
 */

public class SessionPOSButtonCustomAdapter extends ArrayAdapter {
    private TextView textViewToUpdate;
    public SessionPOSButtonCustomAdapter(Context context, int resource, Object[] objects,
                                         TextView textView) {
        super(context, resource, objects);
        textViewToUpdate = textView;
    }

    @Override
    public int getCount() {
        return 12;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = View.inflate(getContext(), R.layout.session_pos_screen_custom_button, null);
        }
        Button getButtonView = (Button) convertView.findViewById(R.id.button_for_custom_number);
        if(position < 9) {
            getButtonView.setText("" + (position + 1));
        } else {
            switch(position){
                case 9:
                    getButtonView.setText("0");
                    break;
                case 10:
                    getButtonView.setText(".");
                    break;
                case 11:
                    getButtonView.setText("Del");
                    break;
            }
        }
        return convertView;
    }
}

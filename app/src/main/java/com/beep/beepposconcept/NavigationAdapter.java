package com.beep.beepposconcept;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by ttwj on 4/12/16.
 */

public class NavigationAdapter extends ArrayAdapter{
    private String[] listToPut;


    public NavigationAdapter(Context context, int resource, String[] objects){
        super(context, resource);
        listToPut = objects;
    }

    @Override
    public int getCount() {
        return listToPut.length;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            convertView = View.inflate(getContext(), R.layout.navigation_drawer_normal_item, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.primaryTextView = (TextView) convertView.findViewById
                    (R.id.navigation_drawer_primary_textview);
            convertView.setTag(viewHolder);
        }
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.primaryTextView.setText(listToPut[position]);

        return convertView;
    }

    private class ViewHolder{
        TextView primaryTextView;
    }
}

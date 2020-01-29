package com.beep.beepposconcept;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by Jacek on 3/12/16.
 */

public class PrimaryAccountFragment extends Fragment {
    private AccountLogs[] accountLogs;
    private ListView accountHistory;
    private AppCompatActivity parentActivity;

    @Override
    public void onAttach(Context context) {
        parentActivity = (AppCompatActivity) context;
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_primary_accounts, container, false);
        accountHistory = (ListView) v.findViewById(R.id.activity_primary_accounts_list_view_history);
        accountHistory.setAdapter(new SimpleCustomListView(accountLogs));
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Toolbar myToolbar = (Toolbar) parentActivity.findViewById(R.id.my_toolbar);
        parentActivity.setSupportActionBar(myToolbar);
        parentActivity.getSupportActionBar().setTitle("Account Information");
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountLogs = new AccountLogs[]{new AccountLogs("12/2/2012", 102), new AccountLogs("4/3/2014", 430),
                new AccountLogs("23/3/2014", 291), new AccountLogs("13/9/2016", 42),
                new AccountLogs("4/12/1903", 1121), new AccountLogs("5/8/2012", 120),
                new AccountLogs("12/7/2004", 3012), new AccountLogs("14/10/2214", 1021)};

    }


    private class AccountLogs {
        private float minutes;
        private String date;

        public String getDate() {
            return date;
        }

        public float getMinutes() {
            return minutes;
        }

        public AccountLogs(String date, float minutes){
            this.date = date;
            this.minutes = minutes;
        }

    }

    private class SimpleCustomListView extends BaseAdapter{
        private AccountLogs[] accountLogses;

        public SimpleCustomListView(AccountLogs[] accountLogses){
            this.accountLogses = accountLogses;
        }

        @Override
        public int getCount() {
            return accountLogses.length;
        }

        @Override
        public Object getItem(int i) {
            return accountLogses[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(view == null) {
                view = view.inflate(getContext(), R.layout.account_history_list_view, null);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.timeTextView = (TextView)
                        view.findViewById(R.id.account_history_list_view_time_spent_view);
                viewHolder.dateTextView = (TextView)
                        view.findViewById(R.id.account_history_list_view_date_view);
                view.setTag(viewHolder);
            }
            ViewHolder viewHolder = (ViewHolder) view.getTag();
            viewHolder.dateTextView.setText(accountLogses[i].getDate());
            viewHolder.timeTextView.setText(accountLogses[i].getMinutes() + "");

            return view;
        }
    }

    static class ViewHolder{
        TextView dateTextView, timeTextView;
    }
}

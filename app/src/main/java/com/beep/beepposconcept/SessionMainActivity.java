package com.beep.beepposconcept;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by ttwj on 7/12/16.
 */

public class SessionMainActivity extends AppCompatActivity {
    private final static String SESSION_POS_FRAGMENT_TAG = "pos";
    private String[] tempArray;
    private ActionBarDrawerToggle toggleNavigationDrawer;
    private DrawerLayout primaryDrawer;
    private ListView drawerListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_main);
        UserSessionManager mSessionManager = UserSessionManager.
                createOrGetUserSessionManager(this);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag(SESSION_POS_FRAGMENT_TAG) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.session_main_fragment_holder, new SessionPOSScreenFragment(),
                    SESSION_POS_FRAGMENT_TAG);
            fragmentTransaction.commit();
        }

        Toolbar myToolbar = (Toolbar) findViewById(R.id.session_my_toolbar);
        setSupportActionBar(myToolbar);

        tempArray = new String[]{mSessionManager.CURRENT_SESSION_USER_MERCHANT_ID + " " +
                mSessionManager.CURRENT_SESSION_USER_NAME, "POS Screen",
                "Log Out"};

        drawerListView = (ListView) findViewById(R.id.session_main_drawer_list_view);
        primaryDrawer = (DrawerLayout) findViewById(R.id.session_drawer_layout);
        drawerListView.setAdapter(new NavigationAdapter(this, R.layout.navigation_drawer_normal_item,
                tempArray));
        drawerListView.setOnItemClickListener(new SessionMainActivity.DrawerItemClickListener());
        drawerListView.setItemChecked(1, true);

        toggleNavigationDrawer = new ActionBarDrawerToggle(this, primaryDrawer, myToolbar,
                R.string.drawer_open, R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();

            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        toggleNavigationDrawer.setHomeAsUpIndicator(R.drawable.ic_navigation_launcher);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        primaryDrawer.addDrawerListener(toggleNavigationDrawer);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggleNavigationDrawer.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleNavigationDrawer.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (toggleNavigationDrawer.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position){
                case 0:
                    break;
                case 1:
                    if(!new  SessionMainActivity.DrawerItemClickListener().checkIfExistingFragment(SESSION_POS_FRAGMENT_TAG)){
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.session_main_fragment_holder, new PrimaryWelcomeFragment(),
                                SESSION_POS_FRAGMENT_TAG);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                    } else {
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                    }
                    break;
                case 2:
                    boolean logOut = UserSessionManager.createOrGetUserSessionManager(
                            getApplicationContext()).logOut();
                    if (logOut) {
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }

        private boolean checkIfExistingFragment(String fragmentTag){
            Fragment myFragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
            if (myFragment != null && myFragment.isVisible()) {
                return true;
            }
            return false;
        }
    }
}


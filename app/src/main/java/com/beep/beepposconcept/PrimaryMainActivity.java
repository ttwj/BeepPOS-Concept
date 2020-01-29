package com.beep.beepposconcept;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

public class PrimaryMainActivity extends AppCompatActivity {
    private final static String PRIMARY_WELCOME_FRAGMENT_TAG = "welcome";
    private final static String PRIMARY_ACCOUNT_FRAGMENT_TAG = "account";
    private String[] tempArray;
    private ActionBarDrawerToggle toggleNavigationDrawer;
    private DrawerLayout primaryDrawer;
    private ListView drawerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UserSessionManager mSessionManager = UserSessionManager.
                createOrGetUserSessionManager(this);
        if (!mSessionManager.checkIfLoggedIn()) {
            Toast.makeText(getApplicationContext(), "In LoginActivity", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LogInActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.findFragmentByTag(PRIMARY_WELCOME_FRAGMENT_TAG) == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.primary_main_fragment_holder, new PrimaryWelcomeFragment(),
                    PRIMARY_WELCOME_FRAGMENT_TAG);
            fragmentTransaction.commit();
        }
            setContentView(R.layout.activity_primary_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        tempArray = new String[]{mSessionManager.CURRENT_SESSION_USER_MERCHANT_ID + " " +
                mSessionManager.CURRENT_SESSION_USER_NAME, "Connect", "Transaction History", "Settings",
                "Account",
                "Log Out"};

        drawerListView = (ListView) findViewById(R.id.primary_main_drawer_list_view);
        primaryDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerListView.setAdapter(new NavigationAdapter(this, R.layout.navigation_drawer_normal_item,
                tempArray));
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
        drawerListView.setItemChecked(1, true);
        //TODO: Research on Navigation Drawer.


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
                    if(!new DrawerItemClickListener().checkIfExistingFragment(PRIMARY_WELCOME_FRAGMENT_TAG)){
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.primary_main_fragment_holder, new PrimaryWelcomeFragment(),
                                PRIMARY_WELCOME_FRAGMENT_TAG);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                    } else {
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                    }
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    if(!new DrawerItemClickListener().checkIfExistingFragment(PRIMARY_ACCOUNT_FRAGMENT_TAG)) {
                        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.primary_main_fragment_holder, new PrimaryAccountFragment(),
                                PRIMARY_ACCOUNT_FRAGMENT_TAG);
                        fragmentTransaction.addToBackStack(null);
                        fragmentTransaction.commit();
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                    } else {
                        primaryDrawer.closeDrawer(Gravity.LEFT, true);
                    }
                    break;
                case 5:
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
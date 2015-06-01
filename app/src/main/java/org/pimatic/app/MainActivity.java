package org.pimatic.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.support.v7.app.AppCompatActivity;

import org.pimatic.connection.Connection;
import org.pimatic.model.AccountManager;
import org.pimatic.model.ConnectionOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, DevicePagesFragment.DevicePagesFragmentCallbacks {
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private DevicePagesFragment mDevicePagesFragment;
    private String activeMainFragmentTag;
    private int activeGroupPosition;
    private int activeChildPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        final AccountManager accountManager = new AccountManager(this);
        final String[] accounts = accountManager.getAllAccountNames();
        ConnectionCache.init(this, accounts);

        if (accounts.length > 0) {
            ConnectionOptions conOpts = accountManager.getConnectionFor(accounts[0]);
            Connection.setup(this, conOpts);
            ConnectionCache.loadFromCache(this, conOpts);
            Connection.connect();
        }


        if (savedInstanceState != null) {
            activeGroupPosition = savedInstanceState.getInt("activeGroupPosition");
            activeChildPosition = savedInstanceState.getInt("activeChildPosition");
            onNavigationDrawerItemSelected(activeGroupPosition, activeChildPosition);
            mNavigationDrawerFragment.setItemChecked(activeGroupPosition,activeChildPosition);
        }
        else {
            activeGroupPosition = 0;
            activeChildPosition = 0;
            mDevicePagesFragment = new DevicePagesFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,
                            mDevicePagesFragment, "PAGES").commit();
            activeMainFragmentTag = "PAGES";
            mNavigationDrawerFragment.setItemChecked(activeGroupPosition,activeChildPosition);
        }
//        Log.v("Test", Formater.formatValue(1, "B").toString());
//        Log.v("Test", Formater.formatValue(1100, "B").toString());
//        Log.v("Test", Formater.formatValue(10001, "B").toString());
//        Log.v("Test", Formater.formatValue(10000001, "B").toString());


        ActionBar actionBar = getSupportActionBar();
        final Spinner accountSelector = (Spinner) getLayoutInflater().inflate(R.layout.accounts_spinner, null, false);
        AccountAdapter adapter = new AccountAdapter(this, new ArrayList<String>());
        accountSelector.setAdapter(adapter);
        accountSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ConnectionOptions conOpts = accountManager.getConnectionFor(accountSelector.getSelectedItem().toString());
                if (conOpts == null) {
                    Log.v("sad", "Error getting connection");
                    return;
                }
                ConnectionCache.saveToCache(MainActivity.this, Connection.getOptions());
                Connection.switchConnection(MainActivity.this, conOpts);
                ConnectionCache.loadFromCache(MainActivity.this, conOpts);
                Connection.connect();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        actionBar.setCustomView(accountSelector);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Connection.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConnectionOptions conOpts = Connection.getOptions();
        if(conOpts != null) {
            ConnectionCache.saveToCache(this, conOpts);
            Connection.disconnect();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int groupPosition, int childPosition){
            // bind the main content by replacing fragments
            activeGroupPosition = groupPosition;
            activeChildPosition = childPosition;
            if (groupPosition == 0) {
                mDevicePagesFragment = new DevicePagesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,
                                mDevicePagesFragment, "PAGES").commit();
                mDevicePagesFragment.setPage(childPosition);
                activeMainFragmentTag = "PAGES";
            } else if (groupPosition == 3) {
                if (childPosition == 0) {
                //mDevicePagesFragment = new DevicePagesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,
                                new AccountsActivity(), "SETTINGS").commit();
                activeMainFragmentTag = "SETTINGS";
                }
            }
        }

    @Override
    public void onPageSelected(int position) {
        activeGroupPosition = 0;
        activeChildPosition = position;
        mNavigationDrawerFragment.setItemChecked(activeGroupPosition, activeChildPosition);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activeGroupPosition", activeGroupPosition);
        outState.putInt("activeChildPosition", activeChildPosition);
//        getSupportFragmentManager().putFragment(outState, "mSavedState", getSupportFragmentManager().findFragmentByTag(activeMainFragmentTag));
    }


}

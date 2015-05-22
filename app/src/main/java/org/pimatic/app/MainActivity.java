package org.pimatic.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import org.pimatic.connection.Connection;
import org.pimatic.model.ConnectionOptions;


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

    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        ConnectionOptions cOpts = ConnectionOptions.fromSettings(
                getResources(),
                getSharedPreferences(SettingsFragment.PREFERENCE_FILENAME, 0)
        );

        Connection.setup(this, cOpts);
        Connection.connect();

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        Connection.connect();
    }

    @Override
    public void onNavigationDrawerItemSelected(int groupPosition, int childPosition) {
        // bind the main content by replacing fragments
        activeGroupPosition = groupPosition;
        activeChildPosition = childPosition;
        if(groupPosition == 0)
        {
            mDevicePagesFragment = new DevicePagesFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,
                            mDevicePagesFragment, "PAGES").commit();
            mDevicePagesFragment.setPage(childPosition);
            activeMainFragmentTag = "PAGES";
        }
        else if (groupPosition == 3)
        {
            if(childPosition == 0)
            {
                //mDevicePagesFragment = new DevicePagesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container,
                                new SettingsFragment(), "SETTINGS").commit();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activeGroupPosition", activeGroupPosition);
        outState.putInt("activeChildPosition", activeChildPosition);
//        getSupportFragmentManager().putFragment(outState, "mSavedState", getSupportFragmentManager().findFragmentByTag(activeMainFragmentTag));

    }

}

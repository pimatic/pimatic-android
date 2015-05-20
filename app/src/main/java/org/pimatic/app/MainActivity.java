package org.pimatic.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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

import org.pimatic.connection.Connection;
import org.pimatic.connection.SocketClient;
import org.pimatic.format.Formater;
import org.pimatic.model.ConnectionOptions;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, DevicePagesFragment.DevicePagesFragmentCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private DevicePagesFragment mDevicePagesFragment;
    private String activeMainFragmentTag;
    private int activeGroupPosition;
    private int activeChildPosition;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
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
                getSharedPreferences(SettingsActivity.PREFERENCE_FILENAME, 0)
        );

        Connection.setup(this, cOpts);
        Connection.connect();

        if (savedInstanceState != null) {
            activeGroupPosition = savedInstanceState.getInt("activeGroupPosition");
            activeChildPosition = savedInstanceState.getInt("activeChildPosition");
            onNavigationDrawerItemSelected(activeGroupPosition, activeChildPosition);
        }
        else {
            activeGroupPosition = 0;
            activeChildPosition = 0;
            mDevicePagesFragment = new DevicePagesFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container,
                            mDevicePagesFragment, "PAGES").commit();
            activeMainFragmentTag = "PAGES";
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

    }

    @Override
    public void onPageSelected(int position) {
        activeGroupPosition = 0;
        activeChildPosition = position;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("activeGroupPosition", activeGroupPosition);
        outState.putInt("activeChildPosition", activeChildPosition);
//        getSupportFragmentManager().putFragment(outState, "mSavedState", getSupportFragmentManager().findFragmentByTag(activeMainFragmentTag));

    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.devices);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DevicePageFragment extends Fragment {

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static DevicePageFragment newInstance(int sectionNumber) {
            DevicePageFragment fragment = new DevicePageFragment();
            Bundle args = new Bundle();
            args.putInt("page_index", sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public DevicePageFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt("page_index"));
        }
    }

}

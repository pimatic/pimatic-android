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
import android.widget.AdapterView;
import android.widget.Spinner;

import org.pimatic.connection.Connection;
import org.pimatic.model.AccountManager;
import org.pimatic.model.ConnectionOptions;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private DevicePagePagerAdapter devicePageAdapter;

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


        final AccountManager accountManager = new AccountManager(this);
        final String[] accounts = accountManager.getAllAccountNames();
        ConnectionCache.init(this, accounts);

        ConnectionOptions conOpts = null;
        if (accounts.length > 0) {
            conOpts = accountManager.getConnectionFor(accounts[0]);
            Connection.setup(this, conOpts);
            ConnectionCache.loadFromCache(this, conOpts);
            Connection.connect();
        }


//        Log.v("Test", Formater.formatValue(1, "B").toString());
//        Log.v("Test", Formater.formatValue(1100, "B").toString());
//        Log.v("Test", Formater.formatValue(10001, "B").toString());
//        Log.v("Test", Formater.formatValue(10000001, "B").toString());

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        devicePageAdapter = new DevicePagePagerAdapter(getSupportFragmentManager());
        ViewPager mViewPager = (ViewPager) findViewById(R.id.devie_page_pager);
        mViewPager.setAdapter(devicePageAdapter);

        tabs.setViewPager(mViewPager);

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
        devicePageAdapter.destroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Connection.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ConnectionCache.saveToCache(this, Connection.getOptions());
        Connection.disconnect();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // bind the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, DevicePageFragment.newInstance(position + 1))
                .commit();
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
            Intent intent = new Intent(MainActivity.this, AccountsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DevicePageFragment extends Fragment {

        public DevicePageFragment() {
        }

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

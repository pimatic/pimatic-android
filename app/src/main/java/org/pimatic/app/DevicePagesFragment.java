package org.pimatic.app;

import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DevicePagesFragment extends Fragment {

    private LinearLayout mLinearLayoutView;

    public DevicePagesFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mLinearLayoutView = (LinearLayout) inflater.inflate(
                R.layout.fragment_pages, container, false);

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) mLinearLayoutView
                .findViewById(R.id.tabs);

        DevicePagePagerAdapter devicePageAdapter =
                new DevicePagePagerAdapter(
                        getChildFragmentManager());


        ViewPager mViewPager = (ViewPager) mLinearLayoutView
                .findViewById(R.id.device_page_pager);


        mViewPager.setAdapter(devicePageAdapter);

        tabs.setViewPager(mViewPager);

        return mLinearLayoutView;
    }

}


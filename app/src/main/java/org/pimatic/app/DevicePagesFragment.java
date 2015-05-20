package org.pimatic.app;

import android.app.Activity;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DevicePagesFragment extends Fragment {

    private LinearLayout mLinearLayoutView;
    private ViewPager mViewPager;
    private int Page;
    private DevicePagesFragmentCallbacks mCallbacks;

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

    public void setPage(int item)
    {
        if(mViewPager != null) {
            mViewPager.setCurrentItem(item);
        }
        else
        {
            Page = item;
        }
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


        mViewPager = (ViewPager) mLinearLayoutView
                .findViewById(R.id.device_page_pager);


        mViewPager.setAdapter(devicePageAdapter);


        tabs.setViewPager(mViewPager);

        tabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                mCallbacks.onPageSelected(position);
            }
            @Override
            public void onPageScrollStateChanged(int arg0){
            }
            @Override
            public void onPageScrolled(int arg0,float arg1,int arg2){
            }
        });

        if(Page >= 0)
        {
            mViewPager.setCurrentItem(Page);
        }

        return mLinearLayoutView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (DevicePagesFragmentCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement DevicePagesFragmentCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    public static interface DevicePagesFragmentCallbacks {
        void onPageSelected(int position);
    }

}


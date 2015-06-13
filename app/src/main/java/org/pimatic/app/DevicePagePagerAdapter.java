package org.pimatic.app;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonicartos.superslim.LayoutManager;

import org.pimatic.model.DevicePage;
import org.pimatic.model.DevicePageManager;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class DevicePagePagerAdapter  extends FragmentStatePagerAdapter {

    private DevicePageManager.UpdateListener listener;

    public DevicePagePagerAdapter(final FragmentManager fm) {
        super(fm);

        DevicePageManager.getInstance().getInstance().onChange(listener = new DevicePageManager.UpdateListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();

            }
        });
    }

    public void destroy() {
        DevicePageManager.getInstance().getInstance().removeListener(listener);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new DevicePageFragment();
        Bundle args = new Bundle();
        args.putInt(DevicePageFragment.ARG_OBJECT, i);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getCount() {
        return DevicePageManager.getInstance().getDevicePages().size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        DevicePage page = DevicePageManager.getInstance().getDevicePages().get(position);
        if(page == null) {
            return "";
        }
        return page.getName();
    }

    // Instances of this class are fragments representing a single
// object in our collection.
    public static class DevicePageFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {




            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(
                    R.layout.device_page_layout, container, false);
            Bundle args = getArguments();
            final int index = args.getInt(ARG_OBJECT);
            final RecyclerView listview = (RecyclerView) rootView.findViewById(R.id.devciesListView);
            LayoutManager mLayoutManager = new LayoutManager(getActivity());
            listview.setLayoutManager(mLayoutManager);

            //Needed because the DeviceAdapter adds a listener to the DevicePageManager,
            //but this method if caled in the loop of DevicePageManager.didChange and and
            //DevicePageManager.onChange modifies the listener array, what leads to a violation
            //exception
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    final DeviceAdapter adapter = new DeviceAdapter(getActivity(), index);
                    listview.setAdapter(adapter);
                }
            });



//        ((TextView) rootView.findViewById(android.R.id.text1)).setText(
//                Integer.toString());
            return rootView;
        }
    }
}


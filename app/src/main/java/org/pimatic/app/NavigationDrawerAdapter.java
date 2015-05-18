package org.pimatic.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import org.pimatic.model.DevicePage;
import org.pimatic.model.DevicePageManager;

/**
 * Created by Kevin Schreiber on 18.05.2015.
 */
public class NavigationDrawerAdapter extends BaseExpandableListAdapter {
    public ArrayList<String> groupItem, tempChild;
    public ArrayList<Object> Childtem = new ArrayList<Object>();
    public LayoutInflater minflater;
    public Activity activity;
    private final Context context;

    public NavigationDrawerAdapter(Context context) {
        this.context = context;
        ArrayList<String> groupItems = new ArrayList<String>();
        Resources res = context.getResources();
        for ( String group : res.getStringArray(R.array.navigation_drawer_groups) ) {

            groupItems.add(group);
            ArrayList<String> child = new ArrayList<String>();

            for (String item : res.getStringArray(res.getIdentifier("navigation_drawer_group_" + group.toString(), "array", "org.pimatic.app"))) {
                child.add(item);
            }
            this.Childtem.add(child);
        }

        groupItem = groupItems;

        DevicePageManager.onChange(new DevicePageManager.UpdateListener() {
            @Override
            public void onChange() {
                rebuildDataset();

            }
        });

    }

    private void rebuildDataset() {
        ArrayList<String> child = new ArrayList<String>();

        for (DevicePage item : DevicePageManager.getDevicePages())
        {
            child.add(item.getName());
        }
        this.Childtem.set(0, child);
        notifyDataSetChanged();
    }

    public void setInflater(LayoutInflater mInflater, Activity act) {
        this.minflater = mInflater;
        activity = act;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return null;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        tempChild = (ArrayList<String>) Childtem.get(groupPosition);

        final String childText = (String) tempChild.get(childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navigation_drawer_item_layout, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.NavigationDrawerItem);

        txtListChild.setText(childText);

        return convertView;
    }



    @Override
    public int getChildrenCount(int groupPosition) {
        return ((ArrayList<String>) Childtem.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return null;
    }

    @Override
    public int getGroupCount() {
        return groupItem.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) groupItem.get(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.navigation_drawer_group_layout, null);
        }

        TextView ListGroup = (TextView) convertView
                .findViewById(R.id.NavigationDrawerGroup);
        ListGroup.setTypeface(null, Typeface.BOLD);
        ListGroup.setText(headerTitle);

        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}

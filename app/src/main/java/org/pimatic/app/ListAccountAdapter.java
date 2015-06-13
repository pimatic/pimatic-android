package org.pimatic.app;

import android.app.Activity;

import java.util.ArrayList;

public class ListAccountAdapter extends AccountAdapter {

    public ListAccountAdapter(Activity context, ArrayList<String> names) {
        super(context, names);
    }

    protected void viewHolderAddExtras(AccountAdapter.ViewHolder viewHolder, boolean isDropdown) {
        // do nothing
    }
}

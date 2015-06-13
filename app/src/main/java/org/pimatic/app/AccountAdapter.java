package org.pimatic.app;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.pimatic.model.AccountManager;

import java.util.ArrayList;

/**
 * Created by Oliver Schneider <oliverschneider89+sweetpi@gmail.com>
 */
public class AccountAdapter extends ArrayAdapter<String> {
    private final Activity context;
    ArrayList<String> names;

    public AccountAdapter(Activity context, ArrayList<String> names) {
        super(context, R.layout.accounts_spinner_item, names);
        this.context = context;
        this.names = names;
        AccountManager accountManager = new AccountManager(context);
        String[] accounts = accountManager.getAllAccountNames();
        for (int i = 0; i < accounts.length; i++) {
            names.add(accounts[i]);
        }
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, true);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent, false);
    }


    private View getCustomView(int position, View convertView, ViewGroup parent, boolean isDropdown) {
        View rowView = convertView;
        // reuse views
        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            rowView = inflater.inflate(R.layout.accounts_spinner_item, null);
            // configure view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.name = (TextView) rowView.findViewById(R.id.accountName);
            viewHolder.title = (TextView) rowView.findViewById(R.id.accountTitle);
            rowView.setTag(viewHolder);
            if (!isDropdown) {
                viewHolder.name.setTextColor(0xFFFFFFFF);
                viewHolder.title.setTextColor(0xFFFFFFFF);
            }
        }

        // fill data
        ViewHolder holder = (ViewHolder) rowView.getTag();
        String name = names.get(position);
        holder.name.setText(name);
        holder.title.setText("pimatic");
        return rowView;
    }

    static class ViewHolder {
        public TextView title;
        public TextView name;
    }

}

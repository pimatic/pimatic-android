package org.pimatic.app;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import org.pimatic.model.Device;
import org.pimatic.model.DeviceVisitor;
import org.pimatic.model.SwitchDevice;
import org.w3c.dom.Text;

public class DeviceArrayAdapter extends ArrayAdapter<Device> {

    private final List<Device> list;
    private final Activity context;
    private final DeviceViewHolderCreator viewHolderCreater;

    public DeviceArrayAdapter(Activity context, List<Device> list) {
        super(context, R.layout.device_layout, list);
        this.context = context;
        this.list = list;
        this.viewHolderCreater = new DeviceViewHolderCreator();
    }

    private static class DeviceViewHolder<D extends Device> {
        protected TextView deviceName;

        public void update(D d) {
            deviceName.setText(d.getName());
        }

    }

    private static class SwitchDeviceHolder extends DeviceViewHolder<SwitchDevice>{
        protected Switch stateSwitch;

        public void update(SwitchDevice d) {
            super.update(d);
            stateSwitch.setChecked(d.getState());
        }
    }


    private class DeviceViewHolderCreator implements DeviceVisitor<View> {
        public View createViewHolder(Device d) {
            return d.visit(this);
        }

        @Override
        public View visitDevice(Device d) {
            LayoutInflater inflator = context.getLayoutInflater();
            View view = inflator.inflate(R.layout.device_layout, null);
            DeviceViewHolder viewHolder = new DeviceViewHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
            LinearLayout attrsLayout = (LinearLayout)view.findViewById(R.id.attributesLayout);
            for(Device.Attribute attr : d.getAttributes()) {
                TextView attrView = new TextView(view.getContext());
                attrView.setText(attr.getFormatedValue());
                attrsLayout.addView(attrView);
            }
            viewHolder.update(d);
            view.setTag(viewHolder);
            return view;
        }
        @Override
        public View visitSwitchDevice(SwitchDevice d) {
            LayoutInflater inflator = context.getLayoutInflater();
            View view = inflator.inflate(R.layout.switch_device_layout, null);
            SwitchDeviceHolder viewHolder = new SwitchDeviceHolder();
            viewHolder.deviceName = (TextView) view.findViewById(R.id.deviceName);
            viewHolder.stateSwitch = (Switch) view.findViewById(R.id.switchDeviceSwitch);
            viewHolder.update(d);
            view.setTag(viewHolder);
            return view;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = null;
        Device d = list.get(position);
        if (convertView == null) {
            view = viewHolderCreater.createViewHolder(d);
        } else {
            DeviceViewHolder holder = (DeviceViewHolder) convertView.getTag();
            holder.update(d);
            view = convertView;
        }
        return view;
    }
}
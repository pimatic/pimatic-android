package org.pimatic.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.tonicartos.superslim.LayoutManager;
import com.tonicartos.superslim.LinearSLM;

import org.apmem.tools.layouts.FlowLayout;
import org.json.JSONException;
import org.json.JSONObject;
import org.pimatic.connection.Connection;
import org.pimatic.format.Formater;
import org.pimatic.helpers.Debouncer;
import org.pimatic.model.ButtonsDevice;
import org.pimatic.model.Device;
import org.pimatic.model.DeviceManager;
import org.pimatic.model.DevicePage;
import org.pimatic.model.DevicePageManager;
import org.pimatic.model.DeviceVisitor;
import org.pimatic.model.Group;
import org.pimatic.model.GroupManager;
import org.pimatic.model.SwitchDevice;
import org.pimatic.model.ThermostatDevice;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

    private final Activity context;
    private int devicePageIndex;
    private ArrayList<Item> items;
    private ViewTypeVisiter typeResolver;
    private DeviceManager.UpdateListener deviceUpdateListener;
    private DevicePageManager.UpdateListener devicePageListener;
    private GroupManager.UpdateListener groupListener;
    private ViewTypes[] intToType = ViewTypes.values();
    public DeviceAdapter(final Activity context, int devicePageIndex) {
        this.context = context;
        this.devicePageIndex = devicePageIndex;
        this.items = new ArrayList<>();
        this.typeResolver = new ViewTypeVisiter();

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        updateItemsList();

        DeviceManager.onChange(deviceUpdateListener = new DeviceManager.UpdateListener() {
            @Override
            public void onChange() {
                updateItemsList();
                notifyDataSetChanged();
            }

            @Override
            public void onAttributeValueChange(Device d, Device.Attribute attr) {
                for (int i = 0; i < items.size(); i++) {
                    Item item = items.get(i);
                    if(item instanceof DeviceItem) {
                        DeviceItem deviceItem = (DeviceItem)item;
                        if(deviceItem.getDevice() == d) {
                            DeviceViewHolder dvh = deviceItem.getViewholder();
                            if(dvh != null) {
                                dvh.attributeValueChanged(d, attr);
                            }
                        }
                    }
                }
            }
        });

        DevicePageManager.onChange(devicePageListener = new DevicePageManager.UpdateListener() {
            @Override
            public void onChange() {
                updateItemsList();
                notifyDataSetChanged();
            }
        });

        GroupManager.onChange(groupListener = new GroupManager.UpdateListener() {
            @Override
            public void onChange() {
                updateItemsList();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        DeviceManager.removeListener(deviceUpdateListener);
        DevicePageManager.removeListener(devicePageListener);
        GroupManager.removeListener(groupListener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TODO cashing
        switch (intToType[viewType]) {
            case HEADER:
                return new HeaderViewHolder(parent);
            case SWITCH:
                return new SwitchDeviceHolder(parent);
            case BUTTONS:
                return new ButtonsDeviceHolder(parent);
            case THERMOSTAT:
                return new ThermostatDeviceHolder(parent);
            case DEVICE:
                //fallthtough
            default:
                return new GenericDeviceHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int i) {
        Item item = getItem(i);
        if (item == null) {
            return;
        }

        LayoutManager.LayoutParams lp = LayoutManager.LayoutParams.from(holder.itemView.getLayoutParams());
        lp.setSlm(LinearSLM.ID);
        if (item.getType() == ViewTypes.HEADER) {
            HeaderItem hi = (HeaderItem) item;
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            hi.setViewholder(headerHolder);
            headerHolder.update(hi.getGroup());
            lp.setFirstPosition(i);
        } else {
            DeviceItem di = (DeviceItem) item;
            DeviceViewHolder deviceHolder = (DeviceViewHolder) holder;
            di.setViewholder(deviceHolder);
            deviceHolder.bind(di.getDevice());
            lp.setFirstPosition(di.getHeaderPos());
        }
        holder.itemView.setLayoutParams(lp);
    }

    @Override
    public int getItemViewType(int position) {
        Item item = getItem(position);
        if (item == null) {
            return 0;
        }
        return item.getType().ordinal();
    }

    public void updateItemsList() {
        items.clear();
        List<DevicePage> pages = DevicePageManager.getDevicePages();
        if (devicePageIndex >= pages.size()) {
            return;
        }
        List<DevicePage.GroupDevicePair> pairs = pages.get(devicePageIndex).getDevicesInGroups();
        for (DevicePage.GroupDevicePair pair : pairs) {
            int headerPos = items.size();
            items.add(new HeaderItem(pair.group));
            for (Device d : pair.devices) {
                items.add(new DeviceItem(d, headerPos));
            }
        }
        Log.v("DeviceAdapter", "Updated items list with: " + items.size() + " items (pairs: " + pairs.size() + ")");
    }

    public Item getItem(int position) {
        if (position >= items.size()) {
            return null;
        }
        //Log.v("DeviceAdapter","PageIndex" + devicePageIndex +  " devices: " + Arrays.toString(devices.toArray()));
        return items.get(position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private enum ViewTypes {
        SWITCH, BUTTONS, DEVICE, THERMOSTAT, HEADER
    }

    public static abstract class Item {
        public abstract ViewTypes getType();
    }

    public static class HeaderItem extends Item {
        private Group group;
        private WeakReference<HeaderViewHolder> viewholder;

        public HeaderItem(Group g) {
            this.group = g;
        }

        @Override
        public ViewTypes getType() {
            return ViewTypes.HEADER;
        }

        public Group getGroup() {
            return group;
        }

        public HeaderViewHolder getViewholder() {
            if (viewholder != null) {
                return viewholder.get();
            }
            return null;
        }

        public void setViewholder(HeaderViewHolder viewholder) {
            this.viewholder = new WeakReference<HeaderViewHolder>(viewholder);
        }
    }

    private static class ViewTypeVisiter implements DeviceVisitor<ViewTypes> {

        @Override
        public ViewTypes visitDevice(Device d) {
            return ViewTypes.DEVICE;
        }

        @Override
        public ViewTypes visitSwitchDevice(SwitchDevice d) {
            return ViewTypes.SWITCH;
        }


        @Override
        public ViewTypes visitButtonsDevice(ButtonsDevice buttonsDevice) {
            return ViewTypes.BUTTONS;
        }

        @Override
        public ViewTypes visitThermostatDevice(ThermostatDevice thermostatDevice) {
            return ViewTypes.THERMOSTAT;
        }
    }

    public abstract class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class HeaderViewHolder extends ViewHolder {
        protected TextView groupName;

        public HeaderViewHolder(ViewGroup parent) {
            super(context.getLayoutInflater().inflate(R.layout.header_item, parent, false));
            this.groupName = (TextView) itemView.findViewById(R.id.header_item);
        }

        public void update(Group group) {
            if (group != null) {
                groupName.setText(group.getName());
            } else {
                groupName.setText("Ungrouped");
            }
        }
    }

    public abstract class DeviceViewHolder<D extends Device> extends ViewHolder {
        protected TextView deviceName;
        protected FlowLayout attrsLayout;
        protected D device;

        protected DeviceViewHolder(ViewGroup parent, int layout) {
            super(context.getLayoutInflater().inflate(layout, parent, false));
            this.deviceName = (TextView) itemView.findViewById(R.id.deviceName);
            this.attrsLayout = (FlowLayout) itemView.findViewById(R.id.attributesLayout);
            itemView.setTag(this);
        }

        public void bind(D d) {
            this.device = d;
            deviceName.setText(d.getName());
            attrsLayout.removeAllViews();
            List<Device.Attribute> attributes = d.getAttributes();
            // iterate in reverse, because rtl layout
            for (int i = attributes.size() - 1; i >= 0; i--) {
                Device.Attribute attr = attributes.get(i);
                bindAttribute(d, attr);
            }

        }

        protected void bindAttribute(D d, Device.Attribute attr) {
            if (!attr.isHidden()) {
                LinearLayout attrLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.device_attribute, attrsLayout, false);
                TextView valueTV = (TextView) attrLayout.findViewById(R.id.value);
                TextView acronymTV = (TextView) attrLayout.findViewById(R.id.acronym);
                TextView unitTV = (TextView) attrLayout.findViewById(R.id.unit);
                String acronym = attr.getAcronym();
                if (acronym == null || acronym.length() == 0) {
                    attrLayout.removeView(acronymTV);
                } else {
                    acronymTV.setText(acronym);
                }
                updateAttributeValue(attr, valueTV, unitTV);
                if (!(attr instanceof Device.NumberAttribute)) {
                    attrLayout.removeView(unitTV);
                }
                attrLayout.setTag(attr);
                attrsLayout.addView(attrLayout);
            }
        }

        public abstract ViewTypes getViewType();

        private void updateAttributeValue(Device.Attribute attr, TextView valueTV, TextView unitTV) {
            if (attr instanceof Device.NumberAttribute) {
                Device.NumberAttribute numAttr = ((Device.NumberAttribute) attr);
                double value = numAttr.getValue();
                String unit = numAttr.getUnit();
                if (unit == null) {
                    unit = "";
                }
                Formater.ValueWithUnit info = Formater.formatValue(value, unit);
                valueTV.setText(info.getFormatedValue());
                unitTV.setText(info.getPrefixedUnit());
            } else {
                valueTV.setText(attr.getFormatedValue());
            }
        }

        public void attributeValueChanged(Device d, Device.Attribute attr) {
            for (int i = 0; i < attrsLayout.getChildCount(); i++) {
                View child = attrsLayout.getChildAt(i);
                if (child.getTag() == attr) {
                    LinearLayout attrLayout = (LinearLayout) child;
                    TextView valueTV = (TextView) attrLayout.findViewById(R.id.value);
                    TextView unitTV = (TextView) attrLayout.findViewById(R.id.unit);
                    updateAttributeValue(attr, valueTV, unitTV);
                }
            }
        }
    }

    private class GenericDeviceHolder extends DeviceViewHolder<Device> {

        protected GenericDeviceHolder(ViewGroup parent) {
            super(parent, R.layout.device_layout);
        }

        @Override
        public ViewTypes getViewType() {
            return ViewTypes.DEVICE;
        }
    }

    private class SwitchDeviceHolder extends DeviceViewHolder<SwitchDevice> {
        protected Switch stateSwitch;

        protected SwitchDeviceHolder(ViewGroup parent) {
            super(parent, R.layout.device_layout);
            LinearLayout nameLayout = (LinearLayout) itemView.findViewById(R.id.nameLayout);
            this.stateSwitch = (Switch) context.getLayoutInflater().inflate(R.layout.switch_device_switch, nameLayout, false);
            nameLayout.addView(this.stateSwitch);
            stateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                    SwitchDevice d = SwitchDeviceHolder.this.device;
                    if (d != null) {
                        if (isChecked != d.getState()) {
                            if (d.getConfig().optBoolean("xConfirm", false)) {
                                new AlertDialog.Builder(context)
                                        .setMessage("Do you really want to turn "
                                                + device.getName() + " "
                                                + (isChecked ? "on" : "off") + "?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                changeStateTo(isChecked);
                                            }
                                        })
                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                stateSwitch.setChecked(!isChecked);
                                            }
                                        })
                                        .show();
                            } else {
                                changeStateTo(isChecked);
                            }
                        }
                    }
                }
            });
        }

        private void changeStateTo(final boolean state) {
            HashMap<String, String> params = new HashMap<String, String>();
            String action = (state ? "turnOn" : "turnOff");
            stateSwitch.setEnabled(false);
            Connection.getRest().callDeviceAction(device, action, params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            stateSwitch.setEnabled(true);
                            try {
                                if (jsonObject.getBoolean("success")) {
                                    Toast.makeText(context.getApplicationContext(),
                                            "Done", Toast.LENGTH_SHORT).show();
                                } else {
                                    stateSwitch.setChecked(!state);
                                    Toast.makeText(context.getApplicationContext(),
                                            "Error: " + jsonObject.getString("message"),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            stateSwitch.setEnabled(true);
                            stateSwitch.setChecked(!state);
                            Toast.makeText(context.getApplicationContext(),
                                    "Error: " + volleyError.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }

        public void bind(SwitchDevice d) {
            super.bind(d);
        }

        protected void bindAttribute(SwitchDevice d, Device.Attribute attr) {
            if (attr.getName().equals("state")) {
                stateSwitch.setChecked(d.getState());
            } else {
                super.bindAttribute(d, attr);
            }
        }

        @Override
        public void attributeValueChanged(Device d, Device.Attribute attr) {
            if (attr.getName().equals("state")) {
                stateSwitch.setChecked(((SwitchDevice) d).getState());
            } else {
                super.attributeValueChanged(d, attr);
            }
        }


        @Override
        public ViewTypes getViewType() {
            return ViewTypes.SWITCH;
        }
    }

    private class ButtonsDeviceHolder extends DeviceViewHolder<ButtonsDevice> {

        protected ButtonsDeviceHolder(ViewGroup parent) {
            super(parent, R.layout.device_layout);
        }


        public void bind(ButtonsDevice d) {
            super.bind(d);
            List<ButtonsDevice.Button> buttons = d.getButtons();
            for (int i = buttons.size() - 1; i >= 0; i--) {
                final ButtonsDevice.Button button = buttons.get(i);
                Button buttonW = (Button) context.getLayoutInflater().inflate(R.layout.buttons_device_button, attrsLayout, false);
                buttonW.setText(button.text);
                buttonW.setTag(button.id);
                buttonW.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ButtonsDeviceHolder.this.buttonPressed(button);
                    }
                });
                attrsLayout.addView(buttonW);
            }
        }

        private void buttonPressed(ButtonsDevice.Button button) {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put("buttonId", button.id);
            Connection.getRest().callDeviceAction(device, "buttonPressed", params,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject jsonObject) {
                            try {
                                if (jsonObject.getBoolean("success")) {
                                    Toast.makeText(context.getApplicationContext(),
                                            "Done", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context.getApplicationContext(),
                                            "Error: " + jsonObject.getString("error"),
                                            Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            Toast.makeText(context.getApplicationContext(),
                                    "Error: " + volleyError.getLocalizedMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        }



        @Override
        protected void bindAttribute(ButtonsDevice d, Device.Attribute attr) {
            // crete no views for attributes
            return;
        }

        @Override
        public ViewTypes getViewType() {
            return ViewTypes.BUTTONS;
        }
    }

    private class ThermostatDeviceHolder extends DeviceViewHolder<ThermostatDevice> {
        protected LinearLayout controls;
        protected EditText setpointET;
        protected Button plusButton;
        protected Button minusButton;
        protected ToggleButton presetComfy;
        protected ToggleButton presetEco;
        protected Spinner mode;
        protected Debouncer<Double> callSetTemperatureAction;
        protected String[] modes = new String[]{"manu", "auto", "boost"};
        protected boolean isUpdating = false;

        protected ThermostatDeviceHolder(ViewGroup parent) {
            super(parent, R.layout.device_layout);
        }


        public void bind(final ThermostatDevice d) {

            controls = (LinearLayout) context.getLayoutInflater().inflate(R.layout.thermostat_device_controls, attrsLayout, false);

            setpointET = (EditText) controls.findViewById(R.id.thermostat_device_settemperature);
            plusButton = (Button) controls.findViewById(R.id.thermostat_device_settemperature_plus);
            minusButton = (Button) controls.findViewById(R.id.thermostat_device_settemperature_minus);
            presetComfy = (ToggleButton) controls.findViewById(R.id.thermostat_device_preset_comfy);
            presetEco = (ToggleButton) controls.findViewById(R.id.thermostat_device_preset_eco);
            mode = (Spinner) controls.findViewById(R.id.thermostat_device_mode);

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double value = Double.parseDouble(setpointET.getText().toString()) + 0.5;
                    setpointET.setText("" + value);
                    callSetTemperatureAction.call(value);
                }
            });

            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double value = Double.parseDouble(setpointET.getText().toString()) - 0.5;
                    if (value > 0) {
                        setpointET.setText("" + value);
                        callSetTemperatureAction.call(value);
                    }
                }
            });

            presetComfy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double value = device.getPresetTemp("comfy");
                    callSetTemperatureAction.callImmediate(value);
                }
            });

            presetEco.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double value = device.getPresetTemp("eco");
                    callSetTemperatureAction.callImmediate(value);
                }
            });

            setpointET.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!isUpdating) {
                        double value = Double.parseDouble(s.toString());
                        callSetTemperatureAction.call(value);
                    }

                }
            });

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mode.getContext(), android.R.layout.simple_spinner_item, modes);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mode.setAdapter(spinnerArrayAdapter);
            mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String mode = ThermostatDeviceHolder.this.mode.getSelectedItem().toString();
                    Log.v("DeviceAdapter", "mode changed to: " + mode + " " + position);
                    if (mode.equals(device.getMode())) {
                        return;
                    }
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("mode", mode);
                    Connection.getRest().callDeviceAction(device, "changeModeTo", params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    try {
                                        if (jsonObject.getBoolean("success")) {
                                            Toast.makeText(context.getApplicationContext(),
                                                    "Done", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context.getApplicationContext(),
                                                    "Error: " + jsonObject.getString("error"),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(context.getApplicationContext(),
                                            "Error: " + volleyError.getLocalizedMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    Log.v("DeviceAdapter", "Nothing selected");
                }
            });

            callSetTemperatureAction = new Debouncer<Double>(new Debouncer.Function<Double>() {
                @Override
                public void call(Double setpoint) {
                    HashMap<String, String> params = new HashMap<String, String>();
                    params.put("temperatureSetpoint", setpoint.toString());
                    Connection.getRest().callDeviceAction(device, "changeTemperatureTo", params,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject jsonObject) {
                                    try {
                                        if (jsonObject.getBoolean("success")) {
                                            Toast.makeText(context.getApplicationContext(),
                                                    "Done", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(context.getApplicationContext(),
                                                    "Error: " + jsonObject.getString("message"),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    Toast.makeText(context.getApplicationContext(),
                                            "Error: " + volleyError.getLocalizedMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                }
            }, 1000);

            super.bind(d);
            attrsLayout.addView(controls);
            controls.requestLayout();
        }

        @Override
        protected void bindAttribute(ThermostatDevice d, Device.Attribute attr) {
            attributeValueChanged(d, attr);
        }


        @Override
        public void attributeValueChanged(Device d, Device.Attribute attr) {
            isUpdating = true;
            ThermostatDevice td = (ThermostatDevice) d;
            switch (attr.getName()) {
                case "temperatureSetpoint":
                    setpointET.setText("" + td.getTemperatureSetpoint());
                    break;
//                case "mode":
//                    String mode = td.getMode();
//                    switch (mode) {
//                        case "comfy":
//                            presetComfy.setChecked(true);
//                            presetEco.setChecked(false);
//                            break;
//                        case "eco":
//                            presetEco.setChecked(true);
//                            presetComfy.setChecked(false);
//                            break;
//                    }
                case "mode":
                    String mode = td.getMode();
                    int index = Arrays.asList(modes).indexOf(mode);
                    Log.v("DeviceAdapter", "Change " + mode + " " + index);
                    this.mode.setSelection(index, true);
                    break;
            }
            isUpdating = false;
        }


        @Override
        public ViewTypes getViewType() {
            return ViewTypes.BUTTONS;
        }
    }

    public class DeviceItem extends Item {
        private Device device;
        private WeakReference<DeviceViewHolder> viewholder;
        private int headerPos;

        public DeviceItem(Device device, int headerPos) {
            this.device = device;
            this.headerPos = headerPos;
        }

        @Override
        public ViewTypes getType() {
            return device.visit(typeResolver);
        }

        public Device getDevice() {
            return device;
        }

        public int getHeaderPos() {
            return headerPos;
        }

        public DeviceViewHolder getViewholder() {
            if(viewholder != null) {
                return viewholder.get();
            }
            return null;
        }

        public void setViewholder(DeviceViewHolder viewholder) {
            this.viewholder = new WeakReference<DeviceViewHolder>(viewholder);
        }
    }

}
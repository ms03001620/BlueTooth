package org.mark.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by mazhenjin on 2017/12/5.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder>{
    private Context mContext;
    private List<BluetoothDevice> mData;
    private AdapterView.OnItemClickListener mListener;

    public DeviceListAdapter(Context context, AdapterView.OnItemClickListener listener){
        mContext = context;
        mListener = listener;
        mData = new ArrayList<>();

    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        DeviceViewHolder holder = new DeviceViewHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_device_item, parent,false));
        return holder;
    }

    public void add(Set<BluetoothDevice> bluetoothDevices){
        mData.addAll(bluetoothDevices);
        notifyDataSetChanged();
    }

    public void add(BluetoothDevice bluetoothDevice){
        mData.add(bluetoothDevice);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, final int position) {
        final BluetoothDevice device = mData.get(position);
        holder.mac.setText(device.getAddress());
        holder.name.setText(device.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(null,v, position, device.hashCode());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public BluetoothDevice getItem(int position) throws Exception{
        return mData.get(position);
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView mac;
        TextView name;

        public DeviceViewHolder(View view) {
            super(view);
            mac = view.findViewById(R.id.mMac);
            name = view.findViewById(R.id.mName);
        }
    }
}

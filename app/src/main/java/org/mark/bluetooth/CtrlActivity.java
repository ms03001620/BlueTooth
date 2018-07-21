package org.mark.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.gcssloop.widget.RockerView;

/**
 * Created by Mark on 2018/7/20
 */
public class CtrlActivity extends AppCompatActivity {

    private static final String TAG = "CtrlActivity";
    private BluetoothDevice mDevice;
    @Nullable
    private BluetoothLeService mBluetoothLeService;
    RockerView mRockerView;

    String KEY_CLEAR = "$0,0,0,0,0,0,0,0,0#";


    String KEY_GO = "$1,0,0,0,0,0,0,0,0#";
    String KEY_BACK = "$2,0,0,0,0,0,0,0,0#";
    String KEY_RIGHT = "$3,0,0,0,0,0,0,0,0#";
    String KEY_LEFT = "$4,0,0,0,0,0,0,0,0#";

    String key_t1 = "$5,0,0,0,0,0,0,0,0#";
    String key_t2 = "$6,0,0,0,0,0,0,0,0#";
    String key_t3 = "$7,0,0,0,0,0,0,0,0#";
    String key_t4 = "$8,0,0,0,0,0,0,0,0#";


    String key_a8 = "$0,0,0,0,8,0,0,0,0#";
    String key_a3 = "$0,0,0,0,3,0,0,0,0#";
    String key_a4 = "$0,0,0,0,4,0,0,0,0#";
    String key_a6 = "$0,0,0,0,6,0,0,0,0#";
    String key_a7 = "$0,0,0,0,7,0,0,0,0#";
    String key_b1 = "$0,0,0,0,0,0,1,0,0#";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ctrl);
        mDevice = getIntent().getParcelableExtra("device");
        bindService(new Intent(this, BluetoothLeService.class), mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


        mRockerView = findViewById(R.id.rocker);
/*        rockerView.setListener(new RockerView.RockerListener() {

            @Override
            public void callback(int eventType, int currentAngle, float currentDistance) {
                switch (eventType) {
                    case RockerView.EVENT_ACTION:
                        // 触摸事件回调
                        Log.e("EVENT_ACTION-------->", "angle="+currentAngle+" - distance"+currentDistance);
                        break;
                    case RockerView.EVENT_CLOCK:
                        // 定时回调
                        Log.e("EVENT_CLOCK", "angle="+currentAngle+" - distance"+currentDistance);
                        break;
                }
            }
        });*/


        findViewById(R.id.btn_clear1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.WriteValue(KEY_CLEAR);
            }
        });

        findViewById(R.id.btn_clear2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.WriteValue(key_a8);
            }
        });



        findViewById(R.id.btn_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.WriteValue(KEY_GO);
            }
        });

        findViewById(R.id.btn_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.WriteValue(KEY_BACK);
            }
        });


        findViewById(R.id.btn_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.WriteValue(KEY_RIGHT);
            }
        });

        findViewById(R.id.btn_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.WriteValue(KEY_LEFT);
            }
        });


    }

    public static final int ACTION_DOWN = 0;
    public static final int ACTION_UP = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_CANCEL = 3;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            mBluetoothLeService.connect(mDevice);
            Log.e(TAG, "BluetoothLeService is onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "BluetoothLeService is onServiceDisconnected");
            mBluetoothLeService = null;
        }
    };

    public static Intent createIntent(Context context, BluetoothDevice device){
        Intent intent = new Intent(context, CtrlActivity.class);
        intent.putExtra("device", device);
        return intent;
    }

    @Override
    protected void onDestroy() {
        mBluetoothLeService.disconnect();
        unbindService(mServiceConnection);
        this.unregisterReceiver(mGattUpdateReceiver);
        super.onDestroy();
    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                Log.d("CtrlActivity", "ACTION_GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                Log.d("CtrlActivity", "ACTION_GATT_DISCONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d("CtrlActivity", "ACTION_GATT_SERVICES_DISCOVERED");
                mRockerView.setRockerColor(context.getResources().getColor(android.R.color.holo_red_dark));

                DeviceHelper deviceHelper = new DeviceHelper(context);
                deviceHelper.saveToDefault(mDevice);

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d("CtrlActivity", "ACTION_DATA_AVAILABLE");
                String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }
}

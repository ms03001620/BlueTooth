package org.mark.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_splash);

        DeviceHelper deviceHelper = new DeviceHelper(this);
        BluetoothAdapter adapter = deviceHelper.getDefaultAdapter();

        if (adapter != null && adapter.isEnabled()) {
            BluetoothDevice device = deviceHelper.getDefaultDevice();
            if (device != null) {
                Intent intent = CtrlActivity.createIntent(this, device);
                startActivity(intent);
                return;
            }
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

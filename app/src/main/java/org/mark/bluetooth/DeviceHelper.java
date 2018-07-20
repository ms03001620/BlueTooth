package org.mark.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by mazhenjin on 2017/12/5.
 */

public class DeviceHelper {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private BluetoothSocket mBluetoothSocketClient;

    private ConnectedThread mServer;

    private final String TAG = "test";
    private final String ID = "00001101-0000-1000-8000-00805F9B34FB";

    public DeviceHelper(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mBluetoothAdapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        }else{
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
    }

    @Nullable
    public BluetoothAdapter getDefaultAdapter(){
        return mBluetoothAdapter;
    }

    public void startServer(){
        Thread waitAcceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BluetoothServerSocket bluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(TAG, getUuid());
                    mBluetoothSocket =  bluetoothServerSocket.accept();
                    bluetoothServerSocket.close();

                    if(mBluetoothSocket!=null){
                        BluetoothDevice device = mBluetoothSocket.getRemoteDevice();
                        Log.d("DeviceHelper", "接受客户连接 , 远端设备名字:" + device.getName() + " , 远端设备地址:" + device.getAddress());

                        if (mBluetoothSocket.isConnected()) {
                            Log.d("DeviceHelper", "已建立与客户连接.");

                            mServer = new ConnectedThread(mBluetoothSocket);
                            mServer.start();
                        }
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        waitAcceptThread.start();
    }

    public void connect(@NotNull final BluetoothDevice bluetoothDevice, final ConnectEvent event) {
        if(mBluetoothAdapter.isDiscovering()){
            event.onDiscovering();
            return;
        }

        event.onStart();
        Thread connectToServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(mBluetoothSocketClient!=null && mBluetoothSocketClient.isConnected()){
                        mBluetoothSocketClient.close();
                    }

                    mBluetoothSocketClient = bluetoothDevice.createRfcommSocketToServiceRecord(getUuid());
                    //在调用 connect() 时，应始终确保设备未在执行设备发现。 如果正在进行发现操作，则会大幅降低连接尝试的速度，并增加连接失败的可能性。
                    mBluetoothSocketClient.connect();


                } catch (IOException e) {
                    event.onException(e);
                }
            }
        });

        connectToServerThread.start();
    }

    public interface ConnectEvent {
        void onDiscovering();

        void onStart();

        void onException(Exception e);
    }

    private UUID getUuid(){
        return UUID.fromString(ID);
    }
}

package org.mark.bluetooth

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    companion object {
        val REQUEST_ENABLE_BT = 10010
        val REQUEST_PERMISSION_COARSE_LOCATION = 20010
    }

    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mDeviceListAdatper: DeviceListAdapter

    private lateinit var mDeviceHelper: DeviceHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDeviceListAdatper = DeviceListAdapter(this, AdapterView.OnItemClickListener { adapterView: AdapterView<*>?, view: View, position: Int, id: Long ->
            val device = mDeviceListAdatper.getItem(position)
            val intent = CtrlActivity.createIntent(this, device);
            startActivity(intent)

/*            mDeviceHelper.connect(device, object : DeviceHelper.ConnectEvent {
                override fun onDiscovering() {
                    Toast.makeText(applicationContext, "扫描中。请稍后进行连接", Toast.LENGTH_SHORT).show()
                }

                override fun onStart() {
                    Toast.makeText(applicationContext, "连接该设备", Toast.LENGTH_LONG).show()
                }

                override fun onException(e: Exception) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            })*/

        })
        mRecyclerView.itemAnimator = DefaultItemAnimator()
        mRecyclerView.layoutManager = LinearLayoutManager(this)
        mRecyclerView.adapter = mDeviceListAdatper

        checkAll();
    }

    private fun checkAll() {
        var pass = checkHardware();

        if (pass) {
            pass = checkLocationPermission()
        }

        if (pass) {
            pass = checkSwitch();
        }

        if (pass) {
            Log.v("MainActivity", "loadDeviceInfo:")
            loadDeviceInfo()
        }
    }

    private fun checkHardware(): Boolean {
        mDeviceHelper = DeviceHelper(this);
        if (mDeviceHelper.defaultAdapter == null) {
            Toast.makeText(this, "设备没有蓝牙硬件", Toast.LENGTH_LONG).show()
            finish()
            return false;
        }
        mBluetoothAdapter = mDeviceHelper.defaultAdapter!!
        return true;
    }

    private fun checkSwitch(): Boolean {
        if (!mBluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return false;
        }
        return true;
    }

    /**
     * 检查权限
     */
    private fun checkLocationPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            AlertDialog.Builder(this)
                    .setMessage("本应用需要使用蓝牙通信。开启蓝牙后，蓝牙信号可被用来定位用户位置。请知悉")
                    .setPositiveButton("授权", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_COARSE_LOCATION)
                        }
                    })
                    .setNegativeButton("退出", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface?, which: Int) {
                            finish()
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show()

            return false;
        } else {
            return true;
        }
    }


    private fun loadDeviceInfo() {
        mDeviceListAdatper.clear()
        val bonded = mBluetoothAdapter.bondedDevices
        if (bonded.size > 0) {
            mDeviceListAdatper.add(bonded)
        }

        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
        }

        val isRun = mBluetoothAdapter.startDiscovery()
        if (!isRun) {
            Toast.makeText(this, "startDiscovery失败", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onStart() {
        super.onStart()
        registerReceiver(mBluetoothStateListener, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
        registerReceiver(mFoundReceiver, IntentFilter(BluetoothDevice.ACTION_FOUND))
    }

    override fun onStop() {
        unregisterReceiver(mBluetoothStateListener)
        unregisterReceiver(mFoundReceiver)
        super.onStop()
    }

    /**
     * 开启扫描后发现蓝牙设备后的回调
     */
    private val mFoundReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                mDeviceListAdatper.add(device)
            }
        }
    }

    private val mBluetoothStateListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
            var msg: String? = null
            when (state) {
                BluetoothAdapter.STATE_TURNING_ON -> {
                    msg = "turning on"
                }
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    msg = "turning off"
                }
                BluetoothAdapter.STATE_ON -> {
                    msg = "on"
                    checkAll()
                }
                BluetoothAdapter.STATE_OFF -> {
                    msg = "off"
                    mDeviceListAdatper.clear()

                }
            }
            Log.v("MainActivity", "BluetoothStateListener onReceive:" + msg)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_COARSE_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkAll()
                } else {
                    finish()
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ENABLE_BT -> {
                if (!mBluetoothAdapter.isEnabled) {
                    finish()
                }
            }
        }
    }
}

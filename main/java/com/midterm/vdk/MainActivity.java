package com.midterm.vdk;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.midterm.vdk.databinding.ActivityMainBinding;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    BluetoothAdapter bluetoothAdapter;
    ArrayAdapter<String> arrayAdapter;
    ArrayList<String> arrayList;
    BluetoothSocket btSocket = null;
    ActivityMainBinding binding;
    Boolean isBtConnected = false;
    int count = 1;
    MyBluetoothService bluetoothService = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private final static int TEM_STATE = 3;
    private final static int HUM_STATE = 4;
    private final static int FAN_STATE = 1;
    private final static int MODE_STATE = 2;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN};

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

            }
            startActivityForResult(enableBtIntent, 1);
        }
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.e("device", deviceName);
            }
        }


        binding.btnComBlu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                }
                if(isBtConnected) {
                    resetConnection();
                }else{

                    bluetoothAdapter.startDiscovery();
                }
            }
        });
        binding.btnNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg("3-0");
                sendMsg("1");
            }
        });
        binding.btnAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMsg("3-1");
                sendMsg("1");
            }
        });
        binding.btnZero.setOnClickListener(this);
        binding.btnOne.setOnClickListener(this);
        binding.btnTwo.setOnClickListener(this);
        binding.btnThree.setOnClickListener(this);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                isBtConnected = true;
                binding.btnComBlu.setText(R.string.title_disconnect);
                binding.btnComBlu.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.red));
                sendMsg("1");
            }
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Toast.makeText(getApplicationContext(), "Device is Disconnect", Toast.LENGTH_SHORT).show();

                isBtConnected = false;
                binding.btnComBlu.setText(R.string.title_btn_connect);
                binding.btnComBlu.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.purple_500));
                resetConnection();
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    checkPermissions();

                }


                if (device.getName() != null) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    if(deviceHardwareAddress.equals("20:16:08:29:97:87")){
                        ConnectDevice(deviceHardwareAddress);
                    }
                    Log.e("Device", deviceName + " " + deviceHardwareAddress);
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(receiver, filter);
    }
    private void sendMsg(String msg)
    {
        if (btSocket != null) {

            try { // Converting the string to bytes for transferring

                btSocket.getOutputStream().write((msg+"/").toString().getBytes());

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    void ConnectDevice(String macAddr) {
        try {
            if (btSocket == null || !isBtConnected) {


                // This will connect the device with address as passed
                BluetoothDevice hc = bluetoothAdapter.getRemoteDevice(macAddr);
                if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                }
                btSocket = hc.createRfcommSocketToServiceRecord(myUUID);
                bluetoothAdapter.cancelDiscovery();
                btSocket.connect();

                bluetoothService = new MyBluetoothService(mHandler,btSocket);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
    private void resetConnection() {


        if (btSocket != null) {
            try {btSocket.close();

            } catch (Exception e) {

            }
            btSocket = null;
            Button[] buttons = {binding.btnZero,binding.btnOne,binding.btnTwo,binding.btnThree};


            for(int i = 0 ; i < buttons.length ; i++){
                    buttons[i].setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.purple_500));
            }
            binding.tvHum.setText("No data");
            binding.tvTem.setText("No data");
            binding.btnAuto.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.untimate_gray));
            binding.btnNormal.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.untimate_gray));

        }

    }
    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted

                break;
        }
    }
    private final Handler mHandler;

    {
        mHandler = new Handler() {
            @SuppressLint({"HandlerLeak", "ResourceAsColor"})
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
//                    case Constants.MESSAGE_STATE_CHANGE1:
//                        switch (msg.arg1) {
//                            case MyBluetoothService.STATE_CONNECTED:
//                                tv_state.setText(getString(R.string.title_connecting_to));
//
//                                break;
//                            case MyBluetoothService.STATE_CONNECTING:
//                                tv_state.setText(R.string.title_connecting);
//                                break;
//                            case MyBluetoothService.STATE_LISTEN:
//                            case MyBluetoothService.STATE_NONE:
//                                tv_state.setText(R.string.title_not_connected);
//                                break;
//                        }
//                        break;
//                case Constants.MESSAGE_WRITE:
//                    byte[] writeBuf = (byte[]) msg.obj;
//                    // construct a string from the buffer
//                    String writeMessage = new String(writeBuf);
//                    mConversationArrayAdapter.add("Me:  " + writeMessage);
//                    break;
                    case Constants.MESSAGE_READ:
                        byte[] readBuf = (byte[]) msg.obj;
                        // construct a string from the valid bytes in the buffer
                        String readMessage = new String(readBuf, 0, msg.arg1);

                        String [] res = readMessage.split("/");
                        for( String r : res){
                            String[] parts = r.split("-");
                            Log.e("Meow",readMessage);


                            try {

                                switch (Integer.parseInt(parts[0])) {

                                    case FAN_STATE:
                                        Button[] buttons = {binding.btnZero,binding.btnOne,binding.btnTwo,binding.btnThree};
                                        int actived = Integer.parseInt(parts[1]);

                                        for(int i = 0 ; i < buttons.length ; i++){
                                            if(i == actived){
                                                buttons[i].setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.mint));
                                            }else{
                                                buttons[i].setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.purple_500));
                                            }
                                        }

                                        break;

                                    case MODE_STATE :

                                        if(Integer.parseInt(parts[1]) == 1){
                                            binding.btnAuto.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.mint));
                                            binding.btnNormal.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.untimate_gray));
                                        }else{
                                            binding.btnNormal.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.mint));
                                            binding.btnAuto.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),R.color.untimate_gray));
                                        }
                                        break;
                                    case TEM_STATE:
                                        binding.tvTem.setText(parts[1] + " °C");
                                        break;
                                    case HUM_STATE:
                                        binding.tvHum.setText(parts[1] + " %");
                                        break;
                                    default:
                                        break;
                                }
                            }catch(Exception e){

                            }
                        }

                        break;


                }
            }
        };
    }
    @Override
    public void onClick(View view) {
        Button b = (Button)view;
        sendMsg("2-" + b.getText());
        sendMsg("1");
    }
}
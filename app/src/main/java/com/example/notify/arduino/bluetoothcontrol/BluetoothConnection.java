package com.example.notify.arduino.bluetoothcontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by root on 1/23/18.
 */

public class BluetoothConnection extends AsyncTask<Void, Void, Void> {

    private Context context;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean connectSuccess = true;
    private String address = null;
    private String TAG = this.getClass().getSimpleName();
    private static final UUID statUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public static final String BLUETOOTH_CONNECTED_ACTION
            = "com.example.notify.arduino.bluetoothcontrol" +
            ".BLUETOOTH_CONNECTED_ACTION";


    public BluetoothConnection(Context context, final String address) {
        this.address = address;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i(TAG, "Initializing BT...");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try
        {
            if (btSocket == null || !isBtConnected) {
                // Get the mobile bluetooth device
                btAdapter = BluetoothAdapter.getDefaultAdapter();
                if (btAdapter == null) {
                    connectSuccess = false;
                    return null;
                }

                // Connects to the device's address and checks if it's available
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                if (device == null) {
                    connectSuccess = false;
                    return null;
                }
                // Create a RFCOMM (SPP) connection
                btSocket = device.createInsecureRfcommSocketToServiceRecord(statUUID);
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                // Start connection
                btSocket.connect();
            }
        }
        catch (IOException e)
        {
            connectSuccess = false; //if the try failed, you can check the exception here
            Log.e(TAG, "BT Connection Failed");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        if (!connectSuccess) {
            Log.e(TAG, "Connection Failed");
        } else {
            Log.i(TAG, "Connection Successful");
            isBtConnected = true;
        }
        Intent intent = new Intent(BLUETOOTH_CONNECTED_ACTION);
        intent.putExtra("success", connectSuccess);
        context.sendBroadcast(intent);
    }

    public void disconnect() {
        try {
            btSocket.close();
        } catch (IOException e) {
            Log.i(TAG, "Failed to disconnect bluetooth");
            e.printStackTrace();
        }
    }

    public void send(String s) {
        if (btSocket == null) {
            Log.e(TAG, "Socket is not initialized.");
            throw new RuntimeException("Socket is not initialized");
        }
        try {
            btSocket.getOutputStream().write(s.getBytes());
        } catch (IOException e) {
            Log.e(TAG, "Couldn't send data");
        }
    }
}

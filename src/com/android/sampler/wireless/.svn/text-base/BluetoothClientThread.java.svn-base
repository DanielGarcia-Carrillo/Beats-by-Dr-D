package com.android.sampler.wireless;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import com.android.sampler.R;

import java.io.IOException;
import java.util.UUID;

public class BluetoothClientThread extends Thread {
    private final BluetoothSocket socket;
    private final BluetoothAdapter adapter;
    private final SocketManager manager;

    public BluetoothClientThread(Context context, BluetoothDevice device, BluetoothAdapter adapter, SocketManager manager) {
        // Use a temporary object that is later assigned to socket,
        // because socket is final
        BluetoothSocket tmp = null;
        this.manager = manager;
        this.adapter = adapter;
        // Get a BluetoothSocket to connect with the given BluetoothDevice
        try {
            // Get UUID from app
            String uuid = context.getResources().getString(R.string.uuid);
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
        } catch (IOException exception) {

        }
        socket = tmp;
    }

    public void run() {
        // Cancel discovery because it will slow down the connection
        adapter.cancelDiscovery();

        try {
            // Connect the device through the socket. This will block
            // until it succeeds or throws an exception
            socket.connect();
        } catch (IOException connectException) {
            // Unable to connect; close the socket and get out
            try {
                socket.close();
            } catch (IOException closeException) { }
            return;
        }

        // Do work to manage the connection (in a separate thread)
        manager.manageConnectedSocket(socket);
    }

    /** Will cancel an in-progress connection, and close the socket */
    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) { }
    }
}

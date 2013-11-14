package com.android.sampler.wireless;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import com.android.sampler.R;

import java.io.IOException;
import java.util.UUID;

public class BluetoothServerThread extends Thread {
    private final BluetoothServerSocket serverSocket;
    private final SocketManager manager;

    public BluetoothServerThread(Context context, BluetoothAdapter adapter, SocketManager manager) {
        // Use a temporary object that is later assigned to serverSocket,
        // because serverSocket is final
        BluetoothServerSocket tmp = null;
        this.manager = manager;
        try {
            // Using this app's name for connection validation and the app's UUID
            String uuid = context.getResources().getString(R.string.uuid);
            tmp = adapter.listenUsingRfcommWithServiceRecord(context.getResources().getString(R.string.app_name), UUID.fromString(uuid));
        } catch (IOException exception) {
        }
        serverSocket = tmp;
    }

    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned
        while (true) {
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                break;
            }
            // If a connection was accepted
            if (socket != null) {
                // We have the connection, deal with it in other thread
                manager.manageConnectedSocket(socket);

                // Since we only care about creating one connection, we close this server
                try {
                serverSocket.close();
                } catch (IOException exception) {
                }
                break;
            }
        }
    }

    /** Will cancel the listening socket, and cause the thread to finish */
    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException exception) {
        }
    }
}
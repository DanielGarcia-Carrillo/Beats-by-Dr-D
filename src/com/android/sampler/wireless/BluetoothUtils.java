package com.android.sampler.wireless;

import android.bluetooth.BluetoothAdapter;

public class BluetoothUtils {
    public static boolean isBluetoothSupported() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null;
    }
}

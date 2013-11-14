package com.android.sampler.wireless;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import com.android.sampler.SampleTracker;
import com.android.sampler.yaySerialization.messages.PayloadTerminalMessage;
import com.android.sampler.yaySerialization.messages.RequestMessage;
import com.android.sampler.yaySerialization.messages.RequestType;
import com.android.sampler.yaySerialization.messages.Terminals;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectedSocketManager extends Thread implements SocketManager {
    private BluetoothSocket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean readingData;
    private RequestType currentTypeReading;
    private AtomicBoolean shouldStop;

    /**
     * Spawns a new thread so that we may handle input/output streams for the socket connection
     * as these streams are blocking and would destroy UI responsiveness
     * @param socket the bluetooth socket that we had established after the server/client business
     */
    @Override
    public void manageConnectedSocket(BluetoothSocket socket) {
        this.socket = socket;
        shouldStop.set(false);
        readingData = false;
        currentTypeReading = RequestType.NULL;
        try {
            in = new ObjectInputStream(socket.getInputStream());
            out = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException exception) {
            Log.e("Connection Socket Manager", "Error getting input/output streams");
            return;
        }

        run();
    }

    @Override
    public void run() {
        while(!shouldStop.get()) {
            Object input = null;
            try {
                input = in.readObject();
            } catch (Exception exception) {continue;}

            if (readingData) {
                if (currentTypeReading.equals(RequestType.AUDIOFILES)) {
                    // Stub
                } else if (currentTypeReading.equals(RequestType.FILEMAP)) {
                    // http://stubbsaustin.com/
                }
            } else {
                /* Checks for reading information or if we should send something */
                if (input instanceof RequestMessage) {
                    RequestMessage request = (RequestMessage) input;
                    if (request.getType().equals(RequestType.AUDIOFILES)) {
                        sendFiles();
                    } else if (request.getType().equals(RequestType.FILEMAP)) {
                        sendTrackerInfo();
                    }
                } else if (input instanceof PayloadTerminalMessage) {
                    PayloadTerminalMessage payloadMessage = (PayloadTerminalMessage) input;
                    // We are now beginning to read data or not...
                    readingData = payloadMessage.getTerminal().equals(Terminals.BEGINNING);
                }
            }
        }

    }

    /**
     * Will close any connections and kill the thread
     */
    public void cancel() {
        try {
            in.close();
            out.close();
            socket.close();
            shouldStop.set(true);
        } catch (IOException exception) {
            Log.e("Android Sampler Bluetooth Connection Manager", "Error closing socket/streams");
        }
    }

    /**
     * Asks the other user for all of their files, ASYNC retrieval
     * @return true if request sent
     */
    public boolean requestFiles() {
        return getSome(new RequestMessage(RequestType.AUDIOFILES));
    }

    /**
     * Asks connected user for their map of start times to files
     * @return true if request sent
     */
    public boolean requestAudioMap() {
        return getSome(new RequestMessage(RequestType.FILEMAP));
    }

    public void sendFiles() {
        PayloadTerminalMessage begin = new PayloadTerminalMessage(RequestType.AUDIOFILES, Terminals.BEGINNING);
        while (!sendSome(begin)){/* Make sure these terminals get sent */}

        PayloadTerminalMessage end = new PayloadTerminalMessage(RequestType.AUDIOFILES, Terminals.END);
        while (!sendSome(end)){}
    }

    public void sendTrackerInfo() {
        SampleTracker.getInstance().getSampleSequence();
        PayloadTerminalMessage begin = new PayloadTerminalMessage(RequestType.FILEMAP, Terminals.BEGINNING);
        while (!sendSome(begin)){/* Make sure these terminals get sent */}

        PayloadTerminalMessage end = new PayloadTerminalMessage(RequestType.FILEMAP, Terminals.END);
        while (!sendSome(end)){}
    }

    /**
     * Because it makes more sense semantically to 'sendSome' when sending a payload
     */
    private boolean sendSome(Object some) {
        return getSome(some);
    }

    /**
     * Because I was tired of putting try/catches everywhere
     * @param something whatever
     */
    private boolean getSome(Object something) {
        try {
            out.writeObject(something);
        } catch (IOException exception) {
            return false;
        }
        return true;
    }
}

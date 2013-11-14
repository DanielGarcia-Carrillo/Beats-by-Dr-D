package com.android.sampler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.android.sampler.audio.Decode;
import com.android.sampler.audio.FileTypeUtils;
import com.android.sampler.wireless.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MusicSamplerActivity extends Activity {
    private static final String PREFERENCES = "samplerActivityPreferences";
    private static final int NUM_SAMPLES = 9;
    private final Map<Integer, Sample> buttonSamples = new HashMap<Integer, Sample>(NUM_SAMPLES);

    private final SoundPool songsPlaying = new SoundPool(NUM_SAMPLES + 1, AudioManager.STREAM_MUSIC, 0);
    public static final int REQUEST_SAMPLE_CHOOSE = 2;

    private int selectedButtonID;

    private SampleTracker tracker;
    private SamplerActivityState state;
    private Context context = this;
    private boolean jammin = false;

    private ArrayAdapter<String> deviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;

    private BluetoothServerThread server = null;
    private BluetoothClientThread client = null;
    private ConnectedSocketManager connectionSocketManager = null;

    /**
     * Setup button responsible for allowing button
     */
    private void initEditButton() {
        final Button edit = (Button) findViewById(R.id.edit);
        edit.setOnClickListener(new View.OnClickListener() {

            // Change text of edit button on press and set editmode
            public void onClick(View v) {
                if (!state.inEditMode()) {
                    state.setEditMode();
                    edit.setText(R.string.done);
                } else {
                    state.disableAllModes();
                    edit.setText(R.string.edit);
                }
            }
        });

        Log.d("Sampler Edit", "Edit button successfully initialized");
    }

    private void initializeRecordButton() {
        final Button record = (Button) findViewById(R.id.record);
        record.setOnTouchListener(new View.OnTouchListener() {
            Looper backgroundLooper;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (state.isRecording()) {
                        if (backgroundLooper != null) {
                            backgroundLooper.stopExecution();
                            backgroundLooper = null;
                        }
                        tracker.stop(event.getEventTime());
                        state.disableAllModes();
                        record.setText(R.string.record);
                    } else {
                        if (!tracker.isFirstRecording()) {
                            backgroundLooper = new Looper(songsPlaying);
                            backgroundLooper.start();
                        }
                        tracker.start(event.getEventTime());
                        state.setRecording();
                        record.setText(R.string.done);
                    }
                    return true;
                }
                // We don't care about nonclicks so don't consume this operation
                return false;
            }
        });
    }

    private void initializeLoopButton() {
        final Button loop = (Button) findViewById(R.id.loop);
        loop.setOnClickListener(new View.OnClickListener() {
            Looper backgroundLooper;

            @Override
            public void onClick(View v) {
                if (state.inLoopMode()) {
                    backgroundLooper.stopExecution();
                    backgroundLooper = null;
                    loop.setText(R.string.loop);
                    state.disableAllModes();
                } else {
                    backgroundLooper = new Looper(songsPlaying);
                    backgroundLooper.start();
                    loop.setText(R.string.stop);
                    state.setLoopMode();
                }
            }
        });
    }

    private void initializeResetButton() {
        final Button reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tracker.resetTracker();
                state.reset();
            }
        });

    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    private void initializeCoopSessionButtons() {
        final Button hostJam = (Button) findViewById(R.id.jam);
        final Button joinJam = (Button) findViewById(R.id.joinjam);

        if (BluetoothUtils.isBluetoothSupported()) {
            deviceListAdapter = new ArrayAdapter<String>(getApplication(), android.R.layout.simple_list_item_1);
            hostJam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    state.hostJam();
                    // Register the BroadcastReceiver
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_FOUND);
                    context.registerReceiver(bluetoothReceiver, filter);

                    // Called in case we are at the end of a prior discovery session
                    bluetoothAdapter.cancelDiscovery();
                    bluetoothAdapter.startDiscovery();
                    // Make dialog for choosing our bluetooth pair
                    showBluetoothDeviceDialog();
                }
            });

            joinJam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    state.joinJam();
                    // Despite this being the join option, we spawn a server thread...
                    connectionSocketManager = new ConnectedSocketManager();
                    server = new BluetoothServerThread(context, bluetoothAdapter, connectionSocketManager);

                    // Joining simply has us make the current device discoverable
                    Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 30);
                    startActivity(discoverableIntent);
                    server.run();
                    jammin = true;
                }
            });
        } else {
            // Make jam button invisible and disabled
            hostJam.setEnabled(false);
            hostJam.setVisibility(View.GONE);
            joinJam.setEnabled(false);
            joinJam.setVisibility(View.GONE);
        }
    }

    private void showBluetoothDeviceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose android device");

        ListView foundDevices = new ListView(context);
        foundDevices.setAdapter(deviceListAdapter);
        foundDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Any discovery that we are currently doing should be stopped because we're about to choose something
                bluetoothAdapter.cancelDiscovery();

                String deviceInfo = ((TextView) view).getText().toString();
                // The info is newline delimited so I get the second line
                String macAddress = deviceInfo.split("\n")[1];

                // Awkwardly enough, this host will now spawn a "client" thread to connect to the joining "server"
                BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(macAddress);
                connectionSocketManager = new ConnectedSocketManager();
                client = new BluetoothClientThread(context, remoteDevice, bluetoothAdapter, connectionSocketManager);
                client.run();
                jammin = true;
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                state.disableAllModes();
            }
        });
        builder.setView(foundDevices);

        final Dialog dialog = builder.create();
        dialog.show();
    }


    private void initializeSaveButton() {
        final Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                openNameDialog();
            }
        });
    }

    public void openNameDialog() {

        AlertDialog.Builder nameDialog = new AlertDialog.Builder(this);
        nameDialog.setTitle("Save project");
        nameDialog.setMessage("Name dem fat beats");
        final EditText input = new EditText(this);
        nameDialog.setView(input);

        nameDialog.setPositiveButton("Save", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                String projName = input.getText().toString();

                // Figure out if we can even write to the external storage
                String state = Environment.getExternalStorageState();
                boolean mExternalStorageWritable = Environment.MEDIA_MOUNTED.equals(state);
                if (!mExternalStorageWritable) {
                    Toast.makeText(getApplication(), "Storage not writable!", Toast.LENGTH_LONG).show();
                    return;
                }

                File soundFile = new File(getApplication().getExternalFilesDir(null), projName+".wav");
                SampleSplicer splicer = new SampleSplicer(getApplication(), buttonSamples, soundFile);
                try {
                    splicer.iterativelyWriteSamples();
                } catch (IOException ioException) {
                    Toast.makeText(getApplication(), "Error writing out samples", Toast.LENGTH_LONG).show();
                }

                Toast.makeText(getApplication(), "File successively written to " + soundFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }

        });

        nameDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                return;
            }
        });
        nameDialog.show();

    }

    /**
     * Adds button to the current view, takes uri from last session and adds them or sets new ones
     */
    private void initializeSampleButtons() {
        // Get preferences to find which samples were set on the last session
        final SharedPreferences sessionSamples = getSharedPreferences(PREFERENCES, 0);

        for (int i = 0; i < 9; i++) {
            String button_name = "Sample_" + i;
            int button_ID = getResources().getIdentifier(button_name, "id", getPackageName());

            final String sampleName = sessionSamples.getString("" + button_ID, null);

            final int starting_id = R.raw.a;
            final int defaultSampleId = starting_id + i;
            // If user set sample not found, put default
            if (sampleName == null) {
                changeButtonSampleByResource(button_ID, defaultSampleId);
            } else {
                // Change button's audio from null to sampleFile
                changeButtonSampleByName(button_ID, sampleName);
            }

            Log.d("button id: ", "" + button_ID);
            final Button soundButton = (Button) findViewById(button_ID);

            // Ontouch rather than onclick because I need the event time
            soundButton.setOnTouchListener(new Button.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    final int buttonId = soundButton.getId();

                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        if (state.inEditMode()) {
                            editCurrentButton(buttonId);
                            return true;
                        } else {
                            // Play file at button
                            final Sample sample = buttonSamples.get(buttonId);
                            int soundId = sample.getSoundID();
                            songsPlaying.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
                            if (state.isRecording()) {
                                tracker.setSampleStart(buttonId, soundId, motionEvent.getEventTime(), sample.getDuration());
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });

        }
    }

    private void editCurrentButton(int buttonId) {
        // Change file uri
        // Start intent for android to show options for selecting audio files
        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Save the current buttonId for the result
        selectedButtonID = buttonId;
        startActivityForResult(Intent.createChooser(intent, getText(R.string.select_file)), REQUEST_SAMPLE_CHOOSE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tracker = SampleTracker.getInstance();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initEditButton();
        initializeRecordButton();
        initializeLoopButton();
        initializeSaveButton();
        initializeResetButton();
        initializeCoopSessionButtons();
        initializeSampleButtons();

        state = new SamplerActivityState(
                findViewById(R.id.edit),
                findViewById(R.id.record),
                findViewById(R.id.loop),
                findViewById(R.id.save),
                findViewById(R.id.reset),
                findViewById(R.id.joinjam),
                findViewById(R.id.jam)
        );
        state.cleanState();
    }

    @Override
    public void onRestart() {
        super.onRestart();

        songsPlaying.autoResume();
    }

    @Override
    protected void onStop() {
        super.onStop();

        songsPlaying.autoPause();

        // Save which samples were used on the buttons
        final SharedPreferences sessionSamples = getSharedPreferences(PREFERENCES, 0);
        SharedPreferences.Editor editor = sessionSamples.edit();
        for (Map.Entry<Integer, Sample> buttonSampleEntry : buttonSamples.entrySet()) {
            final String elementID = buttonSampleEntry.getKey().toString();
            final Sample buttonSample = buttonSampleEntry.getValue();
            editor.putString(elementID, buttonSample.getSoundFilePath());
        }

        // Commit the changes to the buttons
        editor.apply();
    }

    /**
     * This method is for android to use when it shuts down the application for good, basically cleans up my threads and
     * what not
     */
    @Override
    public void onDestroy() {
        super.onDestroy();

        // This is sufficient to check that bluetooth is supported and therefore a reciever has been set
        if (findViewById(R.id.jam).hasOnClickListeners()) {
            unregisterReceiver(bluetoothReceiver);
        }
        if (server != null) {
            server.cancel();
            server = null;
        }
        if (client != null) {
            client.cancel();
            client = null;
        }
        if (connectionSocketManager != null) {
            connectionSocketManager.cancel();
            connectionSocketManager = null;
        }
        songsPlaying.release();
    }

    /**
     * This function gets called whenever I make an intent or specifically call another activity, only used for file
     * browsing intent currently
     * @param requestCode what we opened up the activity for in the first place
     * @param resultCode did the filebrowser activity exit alright?
     * @param filename an intent holding mainly just the filename
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent filename) {
        super.onActivityResult(requestCode, resultCode, filename);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_SAMPLE_CHOOSE) {
                if (filename != null) {
                    // Get the filename from the intent
                    String sampleFile = filename.getData().getEncodedPath();
                    if (!FileTypeUtils.isSupported(sampleFile)) {
                        Log.d("Samples", "Selected sample was not a supported mimetype");
                        Toast toast = Toast.makeText(getApplication(), "This audio file type is not supported", 2000);
                        toast.show();
                        return;
                    }

                    final int buttonID = selectedButtonID;

                    final String sampleWAV;
                    try {
                        sampleWAV = Decode.convertSupportedToWav(sampleFile);
                    } catch (Exception e) {
                        Log.e("Samples", "Sample failed to be converted to wav");
                        Toast toast = Toast.makeText(getApplication(), "An error occurred when setting this button", 2000);
                        toast.show();
                        return;
                    }

                    changeButtonSampleByName(buttonID, sampleWAV);
                    // Indicate the file that the user just selected
                    Toast toast = Toast.makeText(getApplicationContext(), sampleWAV, 1000);
                    toast.show();
                    Log.d("Samples", "Sample from file is: " + sampleWAV);
                    Log.d("Samples", "Button is: " + buttonID);
                }
            }
        }
    }


    /**
     * Sets a button to play based on a file in the android filesystem
     *
     * @param buttonID the identification of the button we'd like to change the sample for
     * @param newFilename the pathname of the file we'd like to set for the sample
     */
    private void changeButtonSampleByName(int buttonID, String newFilename) {
        removeSampleIfPresent(buttonID);

        // Load sample into soundPool and update map
        int newSoundID = songsPlaying.load(newFilename, 1); // The 1 is set for future compatibility
        // Get track information
        MediaMetadataRetriever metadata = new MediaMetadataRetriever();
        metadata.setDataSource(newFilename);
        int duration = Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        Log.d("Duration", "The duration of this file is " + duration);
        metadata.release();

        Sample newSample = new Sample(newFilename, newSoundID, duration, -1);
        buttonSamples.put(buttonID, newSample);
    }

    /**
     * Sets a button to use a sample from the apk
     *
     * @param buttonID the identification of the button we'd like to change the sample for
     * @param resId    the apk resource we'd like to use for the sample
     */
    private void changeButtonSampleByResource(int buttonID, int resId) {
        removeSampleIfPresent(buttonID);

        // Load sample into soundPool and update map
        int newSoundID = songsPlaying.load(getApplication(), resId, 1); // The 1 is set for future compatibility
        // Get Track information
        MediaMetadataRetriever metadata = new MediaMetadataRetriever();
        metadata.setDataSource(getApplication(), resourceUri(resId));
        int duration = Integer.parseInt(metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        metadata.release();


        Sample newSample = new Sample(null, newSoundID, duration, resId);
        buttonSamples.put(buttonID, newSample);
    }

    private void removeSampleIfPresent(int buttonID) {
        Sample currentSample = buttonSamples.get(buttonID);

        // Check if the button has had a sample assigned to it yet and remove that sample from soundpool
        if (currentSample != null) {
            final int currentSoundID = currentSample.getSoundID();
            songsPlaying.unload(currentSoundID);
        }
    }

    /**
     * @param resId resource Id
     * @return a Uri for the given resource id
     */
    public static Uri resourceUri(int resId) {
        return Uri.parse("android.resource://com.android.sampler/" + resId);
    }
}


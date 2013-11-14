package com.android.sampler;

import android.util.Log;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static java.lang.Math.abs;

/**
 * This singleton class listens for clicks of samples and also information about the current play state
 */
public class SampleTracker {
    private static SampleTracker tracker;
    private long startTime;
    private long endTime;
    private boolean firstRecording;
    private boolean isRecording;

    private SortedMap<Long, SampleInfo> samples;

    private SampleTracker() {
        resetTracker();
    }

    public static SampleTracker getInstance() {
        if (tracker == null) {
            tracker = new SampleTracker();
        }
        return tracker;
    }

    public void resetTracker() {
        isRecording = false;
        samples = new ConcurrentSkipListMap<Long, SampleInfo>();
        firstRecording = true;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public boolean isFirstRecording() {
        return firstRecording;
    }

    /**
     * Adds given audio information to our time ordered sample sequence.
     *
     * @param soundId   the soundpool id
     * @param startTime when the clip should be inserted into our sequence of audio clips
     * @param duration  the length of the sample clip
     */
    public void setSampleStart(int buttonId, int soundId, long startTime, long duration) {
        long startOffset = (startTime - this.startTime) % loopDuration();

        // I'm not dealing with audio that tries to wrap around
        if (duration + startOffset < loopDuration()) {
            if (!samples.containsKey(startOffset)) {
                samples.put(startOffset, new SampleInfo(buttonId, soundId, duration));
                //Log.d("Sample Record Insertion", "Registering soundId " + soundId + " with startTime " + startOffset);
            } else {
                //Log.d("Sample Record Insertion", "What have you done you've managed to click on something at the exact same millisecond !? O_O");
            }
        }
    }

    /**
     * @return how long our loop is from beginning till end
     */
    public long loopDuration() {
        // I'm really letting this be pretty long...
        if (firstRecording) {
            return Long.MAX_VALUE;
        }

        // Uhh... it works I guess
        return abs(endTime - startTime);
    }

    public void start(long startTime) {
        this.startTime = startTime;
        isRecording = true;
    }

    /**
     * @param endTime the time at which we were told to stop taking in recording
     * @return true if successfully stopped
     */
    public boolean stop(long endTime) {
        long potentialEndTime = -1;
        if (!samples.isEmpty()) {
            SampleInfo lastInfo = samples.get(samples.lastKey());
            // The keys are the offsets, so this checks to see if the last sample is still currently playing while we're being notified to stop recording
            potentialEndTime = samples.lastKey() + lastInfo.duration;
        }
        this.endTime = potentialEndTime > endTime ? potentialEndTime : endTime;
        isRecording = false;
        firstRecording = false;
        return true;
    }

    public SortedMap<Long, SampleInfo> getSampleSequence() {
        return samples;
    }

    public static class SampleInfo implements Serializable {
        private final long duration;
        private final int soundId;
        private final int buttonId;

        public SampleInfo(int buttonId, int soundId, long duration) {
            this.duration = duration;
            this.soundId = soundId;
            this.buttonId = buttonId;
        }

        public int getSoundId() {
            return soundId;
        }

        public int getButtonId() {
            return buttonId;
        }

    }
}

package com.android.sampler;

import android.media.SoundPool;
import android.os.SystemClock;
import android.util.Log;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class Looper extends Thread {
    private final SampleTracker tracker;
    private final SoundPool samples;
    private final static long LATENCY_COMPENSATION = 10;
    private AtomicBoolean shouldContinue;

    public Looper(SoundPool samplePool) {
        tracker = SampleTracker.getInstance();
        samples = samplePool;
        shouldContinue = new AtomicBoolean(true);
    }

    public synchronized void stopExecution() {
        shouldContinue.set(false);
    }

    @Override
    public void run() {
        // Loops until interrupted
        while (true) {
            Set<Map.Entry<Long, SampleTracker.SampleInfo>> trackerSequence = tracker.getSampleSequence().entrySet();

            long startTime = SystemClock.uptimeMillis();
            for (Map.Entry<Long, SampleTracker.SampleInfo> offsetSamplePair : trackerSequence) {
                if (!shouldContinue.get()) {
                    return;
                }
                long offset = offsetSamplePair.getKey();
                int soundId = offsetSamplePair.getValue().getSoundId();

                long timeRemainingToSample = (startTime + offset) - SystemClock.uptimeMillis();
                try {
                    sleep(timeRemainingToSample - LATENCY_COMPENSATION);
                } catch (InterruptedException e) {
                    // I'm totally fine with this being interrupted, especially since I stop this thread by interrupt..
                }

                samples.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
                Log.d("Looper", "Currently playing sound " + soundId);
            }
        }
    }
}

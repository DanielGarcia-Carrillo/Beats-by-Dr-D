package com.android.sampler;

import org.junit.Before;
import org.junit.Test;

import java.util.SortedMap;

import static org.junit.Assert.*;

public class SampleTrackerTest {
    @Before
    public void setUp() throws Exception {
        SampleTracker temp = SampleTracker.getInstance();
        temp.resetTracker();
    }

    @Test
    public void testSingleton() {
        SampleTracker first = SampleTracker.getInstance();
        SampleTracker second = SampleTracker.getInstance();

        // Since this class should be singleton, these should be the same reference
        assertTrue(first == second);
    }

    @Test
    public void testIsRecording() {
        SampleTracker tracker = SampleTracker.getInstance();

        tracker.start(23432);
        assertTrue(tracker.isRecording());

        tracker.stop(324235);
        assertFalse(tracker.isRecording());
    }

    @Test
    public void testLoopLength() {
        SampleTracker tracker = SampleTracker.getInstance();
        long start = 234324;
        long end = 324234;

        assertEquals(tracker.loopDuration(), Long.MAX_VALUE);

        tracker.start(start);
        tracker.stop(end);

        assertEquals(tracker.loopDuration(), end - start);
    }

    @Test
    public void testFirstRecording() {
        SampleTracker tracker = SampleTracker.getInstance();

        assertTrue(tracker.isFirstRecording());

        tracker.start(2342);

        assertTrue(tracker.isFirstRecording());

        tracker.stop(3242356);

        assertFalse(tracker.isFirstRecording());
    }

    @Test
    public void testSetSample() {
        long loopingStart = 23423;
        int soundId = 2;
        long firstStart = 23525;
        int soundId2 = 3;
        long secondStart = 25555;

        SampleTracker tracker = SampleTracker.getInstance();

        tracker.start(23423);
        tracker.setSampleStart(1, soundId, firstStart, 2);
        tracker.setSampleStart(2, soundId2, secondStart, 25);
        tracker.stop(40000);

        SortedMap<Long, SampleTracker.SampleInfo> sequence = tracker.getSampleSequence();
        assertNotNull(sequence);
        System.out.println(sequence.keySet().size());

        long firstOffset = firstStart - loopingStart;
        SampleTracker.SampleInfo info = sequence.get(firstOffset);
        assertNotNull(info);
        assertEquals(soundId, info.getSoundId());

        long secondOffset = secondStart - loopingStart;
        info = sequence.get(secondOffset);
        assertNotNull(info);
        assertEquals(soundId2, info.getSoundId());

    }
}

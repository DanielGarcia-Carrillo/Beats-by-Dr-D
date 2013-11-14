package com.android.sampler;

import android.content.Context;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class SampleSplicerTest {
    private SampleSplicer splicer;
    @Before
    public void setUp() throws Exception {
        // Creates basically null splicer, will change when I write tests that actually need input
        splicer = new SampleSplicer(EasyMock.createNiceMock(Context.class), new HashMap<Integer, Sample>(), null);
    }

    @Test
    public void testAudioBoundarySnapping() {
        assertEquals(SampleSplicer.HEADER_SIZE, splicer.snapToAudioBoundary(3));
        assertEquals(SampleSplicer.HEADER_SIZE, splicer.snapToAudioBoundary(45));
        assertEquals(48, splicer.snapToAudioBoundary(49));
        assertEquals(1024, splicer.snapToAudioBoundary(1025));
    }
}

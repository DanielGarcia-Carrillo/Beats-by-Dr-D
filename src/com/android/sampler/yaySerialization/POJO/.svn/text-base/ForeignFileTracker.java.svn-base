package com.android.sampler.yaySerialization.POJO;

import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class contains information similar to the SampleTracker but file path agnostic,
 * meant to be used in conjunction with AudioFileObjects
 */
public class ForeignFileTracker implements Serializable {
    private final SortedMap<Long, String> offsetToFileIdmap;

    public ForeignFileTracker(SortedMap<Long, String> offsetFileMap) {
        this.offsetToFileIdmap = new TreeMap<Long, String>(offsetFileMap);
    }

    public SortedMap<Long, String> getMap() {
        return offsetToFileIdmap;
    }
}

package com.android.sampler;

import com.android.sampler.audio.FileTypeUtils;

import java.io.Serializable;

public class Sample implements Serializable {
    private final String soundFile;
    private final int soundID;
    private final int duration;
    private final int resId;

    /**
     * Constructs under the precondition that this audio file type is supported
     * @param soundFile path to sound file
     * @param soundID the soundId for the sound pool
     * @param duration the length of the sound track
     * @param resId the resId if this is a resource o/w -1
     */
    public Sample(String soundFile, int soundID, int duration, int resId) {
        this.soundFile = soundFile;
        this.soundID = soundID;
        this.duration = duration;
        this.resId = resId;
    }

    public int getSoundID() {
        return soundID;
    }

    public int getResId() {
        return resId;
    }
    public String getSoundFilePath() {
        return soundFile;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isAndroidResource() {
        return resId != -1;
    }
}

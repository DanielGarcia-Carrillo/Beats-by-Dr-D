package com.android.sampler.audio;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileTypeUtilsTest {
    @Test
    public void testMP3s() {
        assertTrue(FileTypeUtils.isMp3("blh.mp3"));
        assertTrue(FileTypeUtils.isMp3("blh.mP3"));
        assertTrue(FileTypeUtils.isMp3("blh.MP3"));
        assertTrue(FileTypeUtils.isMp3(".MP3"));

        assertFalse(FileTypeUtils.isMp3("mp3"));
        assertFalse(FileTypeUtils.isMp3(".mp34"));
        assertFalse(FileTypeUtils.isMp3("lkj.mp3pojsa"));
    }

    @Test
    public void testWAVs() {
        assertTrue(FileTypeUtils.isWaveFile("file.wav"));
        assertTrue(FileTypeUtils.isWaveFile("file.wave"));
        assertTrue(FileTypeUtils.isWaveFile("file.waVe"));
        assertTrue(FileTypeUtils.isWaveFile("file.WAV"));
        assertTrue(FileTypeUtils.isWaveFile(".wav"));
        assertTrue(FileTypeUtils.isWaveFile(".wave"));

        assertFalse(FileTypeUtils.isWaveFile("wav"));
        assertFalse(FileTypeUtils.isWaveFile("wave"));
        assertFalse(FileTypeUtils.isWaveFile("lsakfj.waveasfd"));
    }
}

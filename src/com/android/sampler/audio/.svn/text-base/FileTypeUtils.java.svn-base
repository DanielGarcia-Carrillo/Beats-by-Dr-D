package com.android.sampler.audio;

public class FileTypeUtils {
    /**
     * This function checks only the filename to make sure that it is supported
     * @param filename the name of the sound file
     * @return true if the file is one of this program's supported audio types
     */
    public static boolean isSupported(String filename) {
        return isMp3(filename) || isWaveFile(filename);
    }

    /**
     * Note: this will only do a transformation to files that are supported
     * @param filename the audio file's name
     * @return a string with the extension of the file converted to "*.wav"
     */
    public static String toWavExtension(String filename) {
        String newFilename = filename;
        if (isMp3(filename)) {
            newFilename = filename.substring(0, filename.length()-4) + "wav";
        }

        return newFilename;
    }

    /**
     * @param filename the mp3 file name in question
     * @return true if the file has an mp3 extension
     */
    public static boolean isMp3(String filename) {
        return filename.toLowerCase().endsWith(".mp3");
    }

    /**
     * @param filename the audio file name
     * @return true if the file has a conventional wavefile extension
     */
    public static boolean isWaveFile(String filename) {
        return filename.toLowerCase().endsWith(".wav") || filename.toLowerCase().endsWith(".wave");
    }

    /**
     * Enum containing all filetypes that are compatible with this application
     */
    public enum FileType{
        MP3,
        WAV
    }
}

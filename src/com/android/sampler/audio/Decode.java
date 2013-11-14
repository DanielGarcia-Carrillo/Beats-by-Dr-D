package com.android.sampler.audio;

import javazoom.jl.converter.Converter;

public class Decode {
    /**
     * Takes a file that is expected to be of supported mime type and coverts it to wav
     * Note: will not throw an error if the file is not a supported type
     * @param filename file we wish to convert
     * @return the filename of the new wav file (or the original file if already wav/unsupported)
     */
    public static String convertSupportedToWav(String filename) throws Exception {
        String newFilename = filename;
        if (FileTypeUtils.isMp3(filename)) {
            Converter mp3Converter = new Converter();
            newFilename = FileTypeUtils.toWavExtension(filename);
            mp3Converter.convert(filename, newFilename);
        }
        return newFilename;
    }
}

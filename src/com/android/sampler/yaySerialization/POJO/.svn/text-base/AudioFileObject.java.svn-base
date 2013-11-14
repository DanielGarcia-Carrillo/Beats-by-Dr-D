package com.android.sampler.yaySerialization.POJO;

import java.io.*;

public class AudioFileObject implements Serializable {
    private byte[] fileData;
    private String uniqueFileId;

    public AudioFileObject(File audioFile, String uniqueFileId) {
        this.uniqueFileId = uniqueFileId;
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(audioFile, "r");
        } catch (FileNotFoundException exception) {
            fileData = null;
            return;
        }
        try {
            fileData = new byte[(int)file.length()];
            file.read(fileData);
        } catch (IOException except) {
            fileData = null;
            return;
        }
    }

    public AudioFileObject(byte[] data, String uniqueFileId) {
        fileData = new byte[data.length];
        System.arraycopy(data, 0, fileData, 0, data.length);
        this.uniqueFileId = uniqueFileId;
    }

    public byte[] getData() {
        return fileData;
    }

    public String getId() {
        return uniqueFileId;
    }

}

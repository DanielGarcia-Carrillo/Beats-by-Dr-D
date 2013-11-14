package com.android.sampler;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class SampleSplicer {
    private final SampleTracker tracker;
    private final SortedMap<Long, Sample> startTimeToSample;
    private final Context currentContext;
    private final File outputWavFile;
    public final long sampleRate = 44100L;
    public final int numChannels = 2; // Stereo
    public final int byteDepth = 2; // 16 bit PCM
    private final long totalAudioLen;
    private final long byteRate = sampleRate *numChannels*byteDepth;
    public final static int HEADER_SIZE = 44;

    public SampleSplicer(Context context, Map<Integer, Sample> buttonSampleMap, File outputWavFile) {
        tracker = SampleTracker.getInstance();
        startTimeToSample = combineMapsToStartMap(buttonSampleMap);
        currentContext = context;
        this.outputWavFile = outputWavFile;
        totalAudioLen = (long) (tracker.loopDuration()* byteRate / 1000.); // loopDuration returns milliseconds
    }

    private SortedMap<Long, Sample> combineMapsToStartMap(Map<Integer, Sample> buttonSampleMap) {
        SortedMap<Long, Sample> startTimeSamples = new ConcurrentSkipListMap<Long, Sample>();
        SortedMap<Long, SampleTracker.SampleInfo> temp = tracker.getSampleSequence();
        for (Long startTime: temp.keySet()) {
            startTimeSamples.put(startTime, buttonSampleMap.get(temp.get(startTime).getButtonId()));
        }

        return startTimeSamples;
    }

    public void iterativelyWriteSamples() throws IOException {

        FileOutputStream out = new FileOutputStream(outputWavFile);
        // Makes a base file for further writing
        writeSilentWavFile(out);
        out = null;

        InputStream in = null;
        // I'll probably skip around a bit for overlapping files despite the data being ordered
        RandomAccessFile output = new RandomAccessFile(outputWavFile, "rw"); // Read write mode
        Log.d("Random access file", "" + output.length());
        // Write out real data now that we have silence track
        try {
            for (Long startTimeOffset: startTimeToSample.keySet()) {
                Sample currentSample = startTimeToSample.get(startTimeOffset);
                if (currentSample.isAndroidResource()) {
                    in = currentContext.getResources().openRawResource(currentSample.getResId());
                } else {
                    in = new FileInputStream(currentSample.getSoundFilePath());
                }
                // Convert millisecond offset to second offset and multiply by byterate for offset from start of file
                int byteOffset = (int) (startTimeOffset*byteRate / 1000.) + HEADER_SIZE;
                byteOffset = snapToAudioBoundary(byteOffset);

                // We don't care about the header info
                in.skip(HEADER_SIZE);
                int bufferSize = 1024; // just an arbitrary, nice largish power of 2
                byte[] sampleBuffer = new byte[bufferSize];
                byte[] outputBuffer = new byte[bufferSize];

                // Iterate over buffers of length bufferSize until the sample has been entirely written
                while (true) {
                    int bytesRead = in.read(sampleBuffer, 0, bufferSize);
                    // Read in what's currently in the output file
                    output.seek(byteOffset);
                    output.read(outputBuffer, 0, bufferSize);

                    // Add each amplitude of size byteDepth to  corresponding amplitude in output
                    for (int i=0; i < bytesRead; i += byteDepth) {
                        add16BitSoundAmplitudes(sampleBuffer, i, outputBuffer, i);
                    }
                    if (bytesRead != bufferSize) {
                        break;
                    }

                    // Write changes back to where we got it originally
                    output.seek(byteOffset);
                    output.write(outputBuffer, 0, bufferSize);
                    // Increment output offset
                    byteOffset += bufferSize;
                }
                Log.d("Splicing", "Sample successfully spliced into output");
            }

        } finally {
            if (in != null) {
                in.close();
                in = null;
            }
            if (output != null) {
                output.close();
                output = null;
            }
        }
    }

    private void writeSilentWavFile(OutputStream out) throws IOException {
        // Make all empty zero'd out file
        try {
            writeWavHeader(out);
            // 2048 is just arbitrarily big to allow fast writing
            byte[] silence = new byte[2048];
            for (long i=0; i < totalAudioLen; i += 2048) {
                out.write(silence);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    /**
     * Given an outputstream, will write out all the header information for a standard RIFF WAVE file format
     * @param out output stream
     * @throws IOException exception gets bubbled up from write operation
     */
    public void writeWavHeader(OutputStream out) throws IOException {
        final long totalDataLen = totalAudioLen + 36; // Because data starts at 37th byte
        byte[] header = new byte[HEADER_SIZE];
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1;  // format = 1
        header[21] = 0;
        header[22] = (byte) numChannels;
        header[23] = 0;
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * numChannels);  // block align
        header[33] = 0;
        header[34] = 16;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        out.write(header, 0, HEADER_SIZE);
    }

    /**
     * Takes two pairs of bytes and adds them together like integers, returns them in little endian.
     * If the addition calculation overflows 0xffff then the value will just be 0xffff (ie cutoff)
     * @return true if successful
     */
    private boolean add16BitSoundAmplitudes(byte[] sample, int sampleOffset, byte[] output, int outOffset) {
        if (sampleOffset + 1 >= sample.length || outOffset + 1 >= output.length) {
            return false;
        }
        // The first byte is least significant, add any overflow goes into 2nd byte calculation
        int firstByteSum = sample[sampleOffset] + output[outOffset];
        output[outOffset] = (byte)(firstByteSum & 0xff);

        // Add 2nd byte of sample and offset along with any carry from the first calculation
        byte carryOver = (byte) (firstByteSum >> 8);
        // Truncates to at most 0xff
        output[outOffset+1] = (byte) ((sample[sampleOffset + 1] + output[outOffset+1] + carryOver) & 0xff);

        return true;
    }

    /**
     * Since audio data is aligned in a very specific way, ie first channel 2 byte, second channel 2 byte
     * This function helps to align the given offset to the first channel boundary given the output's information
     */
    public int snapToAudioBoundary(int offset) {
        if (offset < HEADER_SIZE) {
            return HEADER_SIZE;
        }

        int audioPacketSize = numChannels * byteDepth;
        // TODO i feel like the following line can be simpler...
        return offset - (offset % audioPacketSize);
    }
}

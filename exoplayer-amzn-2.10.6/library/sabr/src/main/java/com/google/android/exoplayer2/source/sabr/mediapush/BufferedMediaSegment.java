package com.google.android.exoplayer2.source.sabr.mediapush;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a buffered media segment in YouTube's Platypus / SABR media engine.
 * Extracted and de-obfuscated from official YouTube APK media pipeline.
 */
public class BufferedMediaSegment {
    public final String formatId;
    public final long sequenceNumber;
    public final long startTimeUs;
    public final long durationUs;
    public final int byteSize;
    public byte[] data;
    public final List<Object> consumers = new ArrayList<>();
    private long mCurrentPositionUs;
    private boolean mIsComplete;

    public BufferedMediaSegment(String formatId, long sequenceNumber, long startTimeUs, long durationUs, int byteSize) {
        this.formatId = formatId;
        this.sequenceNumber = sequenceNumber;
        this.startTimeUs = startTimeUs;
        this.durationUs = durationUs;
        this.byteSize = byteSize;
        this.mCurrentPositionUs = startTimeUs;
        this.mIsComplete = false;
    }

    public int a() {
        return byteSize;
    }

    public long f() {
        return startTimeUs;
    }

    public long c() {
        return mCurrentPositionUs;
    }

    public boolean l() {
        return mIsComplete;
    }

    public void setComplete(boolean complete) {
        this.mIsComplete = complete;
    }

    public int m(long timeUs) {
        if (timeUs < startTimeUs || timeUs > startTimeUs + durationUs) {
            return -1;
        }
        if (durationUs <= 0) {
            return 0;
        }
        long relativeOffsetUs = timeUs - startTimeUs;
        return (int) (relativeOffsetUs / 20_000); // approximate index or relative offset
    }

    public void k(int sampleIndex, long offsetUs) {
        this.mCurrentPositionUs = startTimeUs + offsetUs;
    }

    public void j(long offsetUs) {
        this.mCurrentPositionUs = startTimeUs + offsetUs;
    }
}

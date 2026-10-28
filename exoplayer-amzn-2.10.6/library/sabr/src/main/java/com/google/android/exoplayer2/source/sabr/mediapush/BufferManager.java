package com.google.android.exoplayer2.source.sabr.mediapush;

/**
 * Interface for multi-stream media buffer management in YouTube's Platypus pipeline.
 */
public interface BufferManager {
    int STREAM_TYPE_AUDIO = 0;
    int STREAM_TYPE_VIDEO = 1;
    int STREAM_TYPE_SUBTITLE = 2;

    MediaSegmentHolder getHolder(int streamType);
    long getBufferedPositionUs();
    boolean seek(int streamType, long timeUs);
    MediaPushReceiver startPush(int streamType, String formatId);
    void release();
    boolean isDisposed();
}

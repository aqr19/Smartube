package com.google.android.exoplayer2.source.sabr.mediapush;

import java.nio.ByteBuffer;

/**
 * Interface for receiving incoming media chunks and headers pushed from YouTube's native UMP network stack.
 * Extracted from com.google.android.libraries.youtube.media.interfaces.MediaPushReceiver.
 */
public interface MediaPushReceiver {
    /**
     * Called when a new media segment starts.
     * @param formatId Format/Stream identifier (itag).
     * @param sequenceNumber Sequence number of the media chunk.
     * @param startTimeUs Start time in microseconds.
     * @param durationUs Duration in microseconds.
     * @param isInitSegment Whether this is an initialization segment (ftyp/moov).
     */
    void startPushSegment(String formatId, long sequenceNumber, long startTimeUs, long durationUs, boolean isInitSegment);

    /**
     * Called when a slice of binary media data is pushed.
     * @param byteBuffer Direct or wrapped byte buffer containing ISO-BMFF / WebM container data.
     */
    void pushSegmentData(ByteBuffer byteBuffer);

    /**
     * Called when the current media segment push has completed.
     */
    void endPushSegment();

    /**
     * Called on QOE or network error.
     */
    void onError(Throwable error, String context);
}

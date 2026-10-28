package com.google.android.exoplayer2.source.sabr.mediapush;

import java.util.HashMap;
import java.util.Map;

/**
 * Coordinates track playback start positions across multi-stream audio, video, and subtitle feeds.
 * Extracted from com.google.android.libraries.youtube.media.player.exo2.platypus.mediapush.StartPositionTracker.
 */
public class StartPositionTracker {
    public final Map<Integer, Long> startPositions = new HashMap<>();
    private boolean mIsInitialized;

    public synchronized void setStartPosition(int streamType, long positionUs) {
        if (!startPositions.containsKey(streamType)) {
            startPositions.put(streamType, positionUs);
            notifySync();
        }
    }

    public synchronized long getStartPosition(int streamType) {
        Long pos = startPositions.get(streamType);
        return pos != null ? pos : 0L;
    }

    public synchronized void a() {
        mIsInitialized = true;
    }

    public synchronized boolean isInitialized() {
        return mIsInitialized;
    }

    public synchronized void reset() {
        startPositions.clear();
        mIsInitialized = false;
    }

    private void notifySync() {
        a();
    }
}

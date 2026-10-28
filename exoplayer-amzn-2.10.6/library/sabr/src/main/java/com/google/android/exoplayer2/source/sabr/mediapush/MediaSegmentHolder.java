package com.google.android.exoplayer2.source.sabr.mediapush;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.util.ParsableByteArray;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base holder for media segments and receiver management.
 * Extracted from com.google.android.libraries.youtube.media.player.exo2.platypus.mediapush.MediaSegmentHolder.
 */
public abstract class MediaSegmentHolder {
    public final int streamType;
    public final List<BufferedMediaSegment> segments = Collections.synchronizedList(new ArrayList<>());
    public final Map<String, Format> formats = new ConcurrentHashMap<>();
    public final StartPositionTracker startPositionTracker;
    protected String mCurrentFormatId;
    protected boolean mIsDisposed;
    protected long mBufferedPositionUs;
    private final MediaPushReceiver mReceiver;

    public MediaSegmentHolder(int streamType, StartPositionTracker startPositionTracker) {
        this.streamType = streamType;
        this.startPositionTracker = startPositionTracker;
        this.mReceiver = new MediaPushReceiverImpl();
    }

    public MediaPushReceiver getReceiver() {
        return mReceiver;
    }

    public long getBufferedPositionUs() {
        return mBufferedPositionUs;
    }

    public synchronized void dispose() {
        mIsDisposed = true;
        segments.clear();
        formats.clear();
    }

    public boolean isDisposed() {
        return mIsDisposed;
    }

    public abstract void sampleData(ParsableByteArray data, int length);
    public abstract void sampleMetadata(long timeUs, int flags, int size, int offset, byte[] cryptoData);
    public abstract void onFormatChanged(Format format);

    public BufferedMediaSegment resolveSegment(String formatId, long sequenceNumber, long startTimeUs, long durationUs) {
        synchronized (segments) {
            for (BufferedMediaSegment seg : segments) {
                if (seg.sequenceNumber == sequenceNumber && seg.formatId.equals(formatId)) {
                    return seg;
                }
            }
            BufferedMediaSegment newSeg = new BufferedMediaSegment(formatId, sequenceNumber, startTimeUs, durationUs, 0);
            segments.add(newSeg);
            return newSeg;
        }
    }

    protected class MediaPushReceiverImpl implements MediaPushReceiver {
        private BufferedMediaSegment mCurrentSegment;

        @Override
        public void startPushSegment(String formatId, long sequenceNumber, long startTimeUs, long durationUs, boolean isInitSegment) {
            mCurrentFormatId = formatId;
            if (startPositionTracker != null && startTimeUs > 0) {
                startPositionTracker.setStartPosition(streamType, startTimeUs);
            }
            mCurrentSegment = resolveSegment(formatId, sequenceNumber, startTimeUs, durationUs);
        }

        @Override
        public void pushSegmentData(ByteBuffer byteBuffer) {
            if (mIsDisposed || byteBuffer == null || !byteBuffer.hasRemaining()) {
                return;
            }
            int length = byteBuffer.remaining();
            byte[] bytes = new byte[length];
            byteBuffer.get(bytes);
            ParsableByteArray parsableData = new ParsableByteArray(bytes);
            sampleData(parsableData, length);
        }

        @Override
        public void endPushSegment() {
            if (mCurrentSegment != null) {
                mCurrentSegment.setComplete(true);
            }
        }

        @Override
        public void onError(Throwable error, String context) {
            // Log or handle push pipeline error
        }
    }
}

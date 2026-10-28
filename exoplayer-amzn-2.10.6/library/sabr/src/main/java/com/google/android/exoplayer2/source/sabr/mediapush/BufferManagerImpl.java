package com.google.android.exoplayer2.source.sabr.mediapush;

import com.google.android.exoplayer2.upstream.Allocator;
import com.liskovsoft.sharedutils.mylogger.Log;

/**
 * Orchestrates multi-stream buffering across audio, video, and captions.
 * Directly corresponds to com.google.android.libraries.youtube.media.player.exo2.platypus.mediapush.BufferManagerImpl.
 */
public class BufferManagerImpl implements BufferManager {
    private static final String TAG = BufferManagerImpl.class.getSimpleName();
    private final SampleHolder mAudioHolder;    // Field a in decompiled code
    private final SampleHolder mVideoHolder;    // Field b in decompiled code
    private final MediaSegmentHolder mCaptionHolder; // Field c in decompiled code
    private final StartPositionTracker mStartPositionTracker;
    private boolean mIsDisposed;                // Field f in decompiled code

    public BufferManagerImpl(Allocator allocator) {
        this.mStartPositionTracker = new StartPositionTracker();
        this.mAudioHolder = new SampleHolder(STREAM_TYPE_AUDIO, allocator, mStartPositionTracker);
        this.mVideoHolder = new SampleHolder(STREAM_TYPE_VIDEO, allocator, mStartPositionTracker);
        this.mCaptionHolder = new SampleHolder(STREAM_TYPE_SUBTITLE, allocator, mStartPositionTracker);
        this.mIsDisposed = false;
    }

    /**
     * Maps stream ordinal to target MediaSegmentHolder.
     * Corresponds to BufferManagerImpl.k(sbu).
     */
    @Override
    public MediaSegmentHolder getHolder(int streamType) {
        if (streamType == STREAM_TYPE_AUDIO) {
            return mAudioHolder;
        } else if (streamType == STREAM_TYPE_VIDEO) {
            return mVideoHolder;
        } else if (streamType == STREAM_TYPE_SUBTITLE) {
            return mCaptionHolder;
        }
        throw new IllegalArgumentException("Unknown stream type: " + streamType);
    }

    /**
     * Calculates the highest buffered time between active streams.
     * Corresponds to BufferManagerImpl.i() -> Math.max(this.a.g(), this.b.g()).
     */
    @Override
    public long getBufferedPositionUs() {
        if (mIsDisposed) {
            return 0L;
        }
        return Math.max(mAudioHolder.getLargestQueuedTimestampUs(), mVideoHolder.getLargestQueuedTimestampUs());
    }

    /**
     * Handles seeking with strict lifecycle check.
     * Corresponds to BufferManagerImpl.n(sbu, long).
     */
    @Override
    public Boolean seek(int streamType, long timeUs) {
        if (!this.mIsDisposed) {
            MediaSegmentHolder holder = getHolder(streamType);
            if (holder instanceof SampleHolder) {
                ((SampleHolder) holder).seekTo(timeUs);
            }
            return Boolean.TRUE;
        }
        // Throws PlatypusException or reports "seek with disposed BufferManager" in official APK
        Log.e(TAG, "seek with disposed BufferManager for stream: " + streamType + " at " + timeUs);
        return Boolean.FALSE;
    }

    /**
     * Starts receiving pushed media data for a stream.
     * Corresponds to BufferManagerImpl.d(...).
     */
    @Override
    public MediaPushReceiver startPush(int streamType, String formatId) {
        MediaSegmentHolder holder = getHolder(streamType);
        return holder.getReceiver();
    }

    @Override
    public synchronized void release() {
        if (!mIsDisposed) {
            mIsDisposed = true;
            mAudioHolder.dispose();
            mVideoHolder.dispose();
            mCaptionHolder.dispose();
            mStartPositionTracker.reset();
        }
    }

    @Override
    public boolean isDisposed() {
        return mIsDisposed;
    }
}

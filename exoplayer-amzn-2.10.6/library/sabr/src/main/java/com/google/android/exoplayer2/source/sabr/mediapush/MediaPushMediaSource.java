package com.google.android.exoplayer2.source.sabr.mediapush;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.BaseMediaSource;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.SinglePeriodTimeline;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.upstream.TransferListener;

import java.io.IOException;
import java.util.List;

/**
 * YouTube's native MediaPushMediaSource implementation for ExoPlayer.
 * Replaces traditional pull-based chunk sources with an event-driven push architecture,
 * eliminating chunk fragmentation, sync drifts, and segment mismatch errors.
 * Extracted from com.google.android.libraries.youtube.media.player.exo2.platypus.mediapush.MediaPushMediaSource.
 */
public class MediaPushMediaSource extends BaseMediaSource {
    private final long mDurationUs;
    private final List<Format> mAudioFormats;
    private final List<Format> mVideoFormats;
    private final boolean mIsLive;
    private MediaPushMediaPeriod mCurrentPeriod;

    public MediaPushMediaSource(long durationMs, List<Format> audioFormats, List<Format> videoFormats, boolean isLive) {
        this.mDurationUs = durationMs > 0 ? C.msToUs(durationMs) : C.TIME_UNSET;
        this.mAudioFormats = audioFormats;
        this.mVideoFormats = videoFormats;
        this.mIsLive = isLive;
    }

    @Override
    protected void prepareSourceInternal(@Nullable TransferListener mediaTransferListener) {
        Timeline timeline = new SinglePeriodTimeline(
                mDurationUs,
                /* isSeekable= */ !mIsLive,
                /* isDynamic= */ mIsLive,
                /* isLive= */ mIsLive,
                /* manifest= */ null,
                /* tag= */ null
        );
        refreshSourceInfo(timeline, null);
    }

    @Override
    public void maybeThrowSourceInfoRefreshError() throws IOException {
        // Ready
    }

    @Override
    public MediaPeriod createPeriod(MediaPeriodId id, Allocator allocator, long startPositionUs) {
        mCurrentPeriod = new MediaPushMediaPeriod(allocator, mAudioFormats, mVideoFormats);
        return mCurrentPeriod;
    }

    @Override
    public void releasePeriod(MediaPeriod mediaPeriod) {
        if (mediaPeriod instanceof MediaPushMediaPeriod) {
            ((MediaPushMediaPeriod) mediaPeriod).release();
        }
        if (mCurrentPeriod == mediaPeriod) {
            mCurrentPeriod = null;
        }
    }

    @Override
    protected void releaseSourceInternal() {
        if (mCurrentPeriod != null) {
            mCurrentPeriod.release();
            mCurrentPeriod = null;
        }
    }

    @Nullable
    public MediaPushMediaPeriod getCurrentPeriod() {
        return mCurrentPeriod;
    }
}

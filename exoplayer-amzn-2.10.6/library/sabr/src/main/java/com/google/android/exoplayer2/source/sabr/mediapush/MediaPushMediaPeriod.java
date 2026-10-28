package com.google.android.exoplayer2.source.sabr.mediapush;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.source.MediaPeriod;
import com.google.android.exoplayer2.source.SampleStream;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.util.Assertions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete MediaPeriod implementation for YouTube's Platypus Push media pipeline.
 * Extracted from com.google.android.libraries.youtube.media.player.exo2.platypus.mediapush.
 */
public class MediaPushMediaPeriod implements MediaPeriod {
    private final BufferManagerImpl mBufferManager;
    private final TrackGroupArray mTrackGroups;
    private Callback mCallback;
    private boolean mPrepared;

    public MediaPushMediaPeriod(Allocator allocator, List<Format> audioFormats, List<Format> videoFormats) {
        this.mBufferManager = new BufferManagerImpl(allocator);

        List<TrackGroup> groups = new ArrayList<>();
        if (videoFormats != null && !videoFormats.isEmpty()) {
            groups.add(new TrackGroup(videoFormats.toArray(new Format[0])));
        }
        if (audioFormats != null && !audioFormats.isEmpty()) {
            groups.add(new TrackGroup(audioFormats.toArray(new Format[0])));
        }
        this.mTrackGroups = new TrackGroupArray(groups.toArray(new TrackGroup[0]));
    }

    public BufferManagerImpl getBufferManager() {
        return mBufferManager;
    }

    @Override
    public void prepare(Callback callback, long positionUs) {
        this.mCallback = callback;
        this.mPrepared = true;
        callback.onPrepared(this);
    }

    @Override
    public void maybeThrowPrepareError() throws IOException {
        // Ready
    }

    @Override
    public TrackGroupArray getTrackGroups() {
        return mTrackGroups;
    }

    @Override
    public long selectTracks(TrackSelection[] selections, boolean[] mayRetainStreamFlags, SampleStream[] streams, boolean[] streamResetFlags, long positionUs) {
        for (int i = 0; i < selections.length; i++) {
            if (streams[i] != null && (selections[i] == null || !mayRetainStreamFlags[i])) {
                streams[i] = null;
            }
            if (streams[i] == null && selections[i] != null) {
                TrackSelection selection = selections[i];
                Format selectedFormat = selection.getSelectedFormat();
                int streamType = com.google.android.exoplayer2.util.MimeTypes.isVideo(selectedFormat.sampleMimeType) ?
                        BufferManager.STREAM_TYPE_VIDEO : BufferManager.STREAM_TYPE_AUDIO;
                SampleHolder holder = (SampleHolder) mBufferManager.getHolder(streamType);
                holder.format(selectedFormat);
                streams[i] = new MediaPushSampleStream(holder);
                streamResetFlags[i] = true;
            }
        }
        return positionUs;
    }

    @Override
    public void discardBuffer(long positionUs, boolean toKeyframe) {
        // Buffers managed through BufferManager
    }

    @Override
    public long readDiscontinuity() {
        return C.TIME_UNSET;
    }

    @Override
    public long seekToUs(long positionUs) {
        mBufferManager.seek(BufferManager.STREAM_TYPE_VIDEO, positionUs);
        mBufferManager.seek(BufferManager.STREAM_TYPE_AUDIO, positionUs);
        return positionUs;
    }

    @Override
    public long getAdjustedSeekPositionUs(long positionUs, SeekParameters seekParameters) {
        return positionUs;
    }

    @Override
    public long getBufferedPositionUs() {
        return mBufferManager.getBufferedPositionUs();
    }

    @Override
    public long getNextLoadPositionUs() {
        return getBufferedPositionUs();
    }

    @Override
    public boolean continueLoading(long positionUs) {
        return false;
    }

    @Override
    public void reevaluateBuffer(long positionUs) {
        // Adaptive reevaluation
    }

    public void release() {
        mBufferManager.release();
    }
}

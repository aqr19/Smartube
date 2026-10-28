package com.google.android.exoplayer2.source.sabr.mediapush;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.source.SampleQueue;
import com.google.android.exoplayer2.upstream.Allocator;
import com.google.android.exoplayer2.util.ParsableByteArray;

import java.io.IOException;

/**
 * Concrete track sample queue and buffer manager for an individual media track (Audio or Video).
 * Bridges incoming push data from YouTube's network stack into ExoPlayer's TrackOutput and SampleQueue.
 * Extracted from com.google.android.libraries.youtube.media.player.exo2.platypus.mediapush.SampleHolder.
 */
public class SampleHolder extends MediaSegmentHolder implements TrackOutput {
    private final SampleQueue mSampleQueue;
    private long mPlaybackPositionUs;
    private boolean mHasFormat;

    public SampleHolder(int streamType, Allocator allocator, StartPositionTracker startPositionTracker) {
        super(streamType, startPositionTracker);
        this.mSampleQueue = new SampleQueue(allocator);
    }

    public SampleQueue getSampleQueue() {
        return mSampleQueue;
    }

    @Override
    public void format(Format format) {
        this.mHasFormat = true;
        this.mSampleQueue.format(format);
        onFormatChanged(format);
    }

    @Override
    public int sampleData(com.google.android.exoplayer2.extractor.ExtractorInput input, int length, boolean allowEndOfInput) throws IOException, InterruptedException {
        return mSampleQueue.sampleData(input, length, allowEndOfInput);
    }

    @Override
    public void sampleData(ParsableByteArray data, int length) {
        mSampleQueue.sampleData(data, length);
        mBufferedPositionUs = mSampleQueue.getLargestQueuedTimestampUs();
    }

    @Override
    public void sampleMetadata(long timeUs, int flags, int size, int offset, TrackOutput.CryptoData cryptoData) {
        mSampleQueue.sampleMetadata(timeUs, flags, size, offset, cryptoData);
        mBufferedPositionUs = Math.max(mBufferedPositionUs, timeUs);
    }

    @Override
    public void sampleMetadata(long timeUs, int flags, int size, int offset, byte[] cryptoData) {
        sampleMetadata(timeUs, flags, size, offset, (TrackOutput.CryptoData) null);
    }

    @Override
    public void onFormatChanged(Format format) {
        if (mCurrentFormatId != null) {
            formats.put(mCurrentFormatId, format);
        }
    }

    /**
     * Reads a sample out of the queue into ExoPlayer's DecoderInputBuffer for rendering.
     * Corresponds to SampleHolder.R(FormatHolder, DecoderInputBuffer, int).
     */
    public synchronized int read(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean formatRequired, boolean loadingFinished, long decodeOnlyUntilUs) {
        int result = mSampleQueue.read(formatHolder, buffer, formatRequired, loadingFinished, decodeOnlyUntilUs);
        if (result == com.google.android.exoplayer2.C.RESULT_BUFFER_READ) {
            mPlaybackPositionUs = buffer.timeUs;
        }
        return result;
    }

    /**
     * Adaptive buffer trimming and pruning.
     * Corresponds to SampleHolder.I(BufferedMediaSegment, long).
     */
    public synchronized void pruneBuffer(BufferedMediaSegment segment, long timeUs) {
        if (segment == null || mSampleQueue == null) {
            return;
        }
        if (timeUs > mSampleQueue.getFirstTimestampUs()) {
            int targetIndex = segment.m(timeUs);
            if (targetIndex != -1) {
                mSampleQueue.discardTo(timeUs, true, true);
                segment.k(targetIndex, timeUs - segment.f());
            }
        }
    }

    public synchronized void seekTo(long timeUs) {
        mPlaybackPositionUs = timeUs;
        mSampleQueue.reset();
    }

    public synchronized long getLargestQueuedTimestampUs() {
        return mSampleQueue.getLargestQueuedTimestampUs();
    }

    @Override
    public synchronized void dispose() {
        super.dispose();
        mSampleQueue.reset();
    }
}

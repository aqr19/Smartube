package com.google.android.exoplayer2.source.sabr.mediapush;

import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.source.SampleStream;

import java.io.IOException;

/**
 * Bridges ExoPlayer's renderers to YouTube's SampleHolder reading pipeline.
 * Directly corresponds to SampleQueueReader calling SampleHolder.R(...) in YouTube.apk.
 */
public class MediaPushSampleStream implements SampleStream {
    private final SampleHolder mSampleHolder;

    public MediaPushSampleStream(SampleHolder sampleHolder) {
        this.mSampleHolder = sampleHolder;
    }

    @Override
    public boolean isReady() {
        return mSampleHolder != null && !mSampleHolder.isDisposed();
    }

    @Override
    public void maybeThrowError() throws IOException {
        // No fatal errors queued
    }

    @Override
    public int readData(FormatHolder formatHolder, DecoderInputBuffer buffer, boolean formatRequired) {
        if (mSampleHolder == null || mSampleHolder.isDisposed()) {
            return com.google.android.exoplayer2.C.RESULT_NOTHING_READ;
        }
        return mSampleHolder.read(formatHolder, buffer, formatRequired, false, 0);
    }

    @Override
    public int skipData(long positionUs) {
        return 0;
    }
}

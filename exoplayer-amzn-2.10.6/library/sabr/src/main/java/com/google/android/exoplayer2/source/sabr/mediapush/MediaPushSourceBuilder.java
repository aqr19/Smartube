package com.google.android.exoplayer2.source.sabr.mediapush;

import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.source.sabr.manifest.AdaptationSet;
import com.google.android.exoplayer2.source.sabr.manifest.Period;
import com.google.android.exoplayer2.source.sabr.manifest.Representation;
import com.google.android.exoplayer2.source.sabr.manifest.SabrManifest;
import com.google.android.exoplayer2.source.sabr.manifest.SabrManifestParser;
import com.google.android.exoplayer2.util.MimeTypes;
import com.liskovsoft.mediaserviceinterfaces.data.MediaItemFormatInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds YouTube's native MediaPushMediaSource directly from MediaItemFormatInfo.
 */
public class MediaPushSourceBuilder {
    public static MediaPushMediaSource build(MediaItemFormatInfo formatInfo) {
        if (formatInfo == null) {
            return null;
        }
        SabrManifestParser parser = new SabrManifestParser();
        SabrManifest manifest = parser.parse(formatInfo);

        List<Format> audioFormats = new ArrayList<>();
        List<Format> videoFormats = new ArrayList<>();

        if (manifest != null && manifest.getPeriodCount() > 0) {
            Period period = manifest.getPeriod(0);
            if (period != null && period.adaptationSets != null) {
                for (AdaptationSet adaptationSet : period.adaptationSets) {
                    if (adaptationSet != null && adaptationSet.representations != null) {
                        for (Representation rep : adaptationSet.representations) {
                            if (rep != null && rep.format != null) {
                                if (MimeTypes.isVideo(rep.format.sampleMimeType)) {
                                    videoFormats.add(rep.format);
                                } else if (MimeTypes.isAudio(rep.format.sampleMimeType)) {
                                    audioFormats.add(rep.format);
                                }
                            }
                        }
                    }
                }
            }
        }

        long durationMs = 0;
        try {
            if (formatInfo.getLengthSeconds() != null) {
                durationMs = Long.parseLong(formatInfo.getLengthSeconds()) * 1000L;
            }
        } catch (Exception ignored) {
        }

        return new MediaPushMediaSource(durationMs, audioFormats, videoFormats, formatInfo.isLive());
    }
}

package org.twinkie.phbot.library.lavaplayer.container.adts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerHints;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.info.AudioTrackInfoBuilder;

import java.io.IOException;

import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.supportedFormat;

/**
 * Container detection probe for ADTS stream format.
 */
public class AdtsContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(AdtsContainerProbe.class);

  @Override
  public String getName() {
    return "adts";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    boolean invalidMimeType = hints.mimeType != null && !"audio/aac".equalsIgnoreCase(hints.mimeType);
    boolean invalidFileExtension = hints.fileExtension != null && !"aac".equalsIgnoreCase(hints.fileExtension);
    return hints.present() && !invalidMimeType && !invalidFileExtension;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {
    AdtsStreamReader reader = new AdtsStreamReader(inputStream);

    if (reader.findPacketHeader(MediaContainerDetection.STREAM_SCAN_DISTANCE) == null) {
      return null;
    }

    log.debug("Track {} is an ADTS stream.", reference.identifier);

    return supportedFormat(this, null, AudioTrackInfoBuilder.create(reference, inputStream).build());
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    return new AdtsAudioTrack(trackInfo, inputStream);
  }
}

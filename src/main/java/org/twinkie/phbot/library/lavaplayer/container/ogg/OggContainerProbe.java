package org.twinkie.phbot.library.lavaplayer.container.ogg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerHints;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.info.AudioTrackInfoBuilder;

import java.io.IOException;

import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.checkNextBytes;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.supportedFormat;
import static org.twinkie.phbot.library.lavaplayer.container.ogg.OggPacketInputStream.OGG_PAGE_HEADER;

/**
 * Container detection probe for OGG stream.
 */
public class OggContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(OggContainerProbe.class);

  @Override
  public String getName() {
    return "ogg";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    return false;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream stream) throws IOException {
    if (!checkNextBytes(stream, OGG_PAGE_HEADER)) {
      return null;
    }

    log.debug("Track {} is an OGG file.", reference.identifier);

    AudioTrackInfoBuilder infoBuilder = AudioTrackInfoBuilder.create(reference, stream);

    try {
      collectStreamInformation(stream, infoBuilder);
    } catch (Exception e) {
      log.warn("Failed to collect additional information on OGG stream.", e);
    }

    return supportedFormat(this, null, infoBuilder.build());
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    return new OggAudioTrack(trackInfo, inputStream);
  }

  private void collectStreamInformation(SeekableInputStream stream, AudioTrackInfoBuilder infoBuilder) throws IOException {
    OggPacketInputStream packetInputStream = new OggPacketInputStream(stream, false);
    OggMetadata metadata = OggTrackLoader.loadMetadata(packetInputStream);

    if (metadata != null) {
      infoBuilder.apply(metadata);
    }
  }
}

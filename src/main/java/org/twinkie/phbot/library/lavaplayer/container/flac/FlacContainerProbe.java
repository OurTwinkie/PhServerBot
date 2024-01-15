package org.twinkie.phbot.library.lavaplayer.container.flac;

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

/**
 * Container detection probe for MP3 format.
 */
public class FlacContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(FlacContainerProbe.class);

  private static final String TITLE_TAG = "TITLE";
  private static final String ARTIST_TAG = "ARTIST";

  @Override
  public String getName() {
    return "flac";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    return false;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {
    if (!checkNextBytes(inputStream, FlacFileLoader.FLAC_CC)) {
      return null;
    }

    log.debug("Track {} is a FLAC file.", reference.identifier);

    FlacTrackInfo fileInfo = new FlacFileLoader(inputStream).parseHeaders();

    AudioTrackInfo trackInfo = AudioTrackInfoBuilder.create(reference, inputStream)
        .setTitle(fileInfo.tags.get(TITLE_TAG))
        .setAuthor(fileInfo.tags.get(ARTIST_TAG))
        .setLength(fileInfo.duration)
        .build();

    return supportedFormat(this, null, trackInfo);
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    return new FlacAudioTrack(trackInfo, inputStream);
  }
}

package org.twinkie.phbot.library.lavaplayer.container.wav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerHints;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.io.IOException;

import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.*;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.supportedFormat;
import static org.twinkie.phbot.library.lavaplayer.container.wav.WavFileLoader.WAV_RIFF_HEADER;
import static org.twinkie.phbot.library.lavaplayer.tools.DataFormatTools.defaultOnNull;

/**
 * Container detection probe for WAV format.
 */
public class WavContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(WavContainerProbe.class);

  @Override
  public String getName() {
    return "wav";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    return false;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {
    if (!checkNextBytes(inputStream, WAV_RIFF_HEADER)) {
      return null;
    }

    log.debug("Track {} is a WAV file.", reference.identifier);

    WavFileInfo fileInfo = new WavFileLoader(inputStream).parseHeaders();

    return supportedFormat(this, null, new AudioTrackInfo(
        defaultOnNull(reference.title, UNKNOWN_TITLE),
        UNKNOWN_ARTIST,
        fileInfo.getDuration(),
        reference.identifier,
        false,
        reference.identifier,
        null,
        null
    ));
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    return new WavAudioTrack(trackInfo, inputStream);
  }
}

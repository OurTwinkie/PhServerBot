package org.twinkie.phbot.library.lavaplayer.container.mp3;

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

import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.STREAM_SCAN_DISTANCE;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.checkNextBytes;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.supportedFormat;

/**
 * Container detection probe for MP3 format.
 */
public class Mp3ContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(Mp3ContainerProbe.class);

  private static final int[] ID3_TAG = new int[] { 0x49, 0x44, 0x33 };

  @Override
  public String getName() {
    return "mp3";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    boolean invalidMimeType = hints.mimeType != null && !"audio/mpeg".equalsIgnoreCase(hints.mimeType);
    boolean invalidFileExtension = hints.fileExtension != null && !"mp3".equalsIgnoreCase(hints.fileExtension);
    return hints.present() && !invalidMimeType && !invalidFileExtension;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {
    if (!checkNextBytes(inputStream, ID3_TAG)) {
      byte[] frameHeader = new byte[4];
      Mp3FrameReader frameReader = new Mp3FrameReader(inputStream, frameHeader);
      if (!frameReader.scanForFrame(STREAM_SCAN_DISTANCE, false)) {
        return null;
      }

      inputStream.seek(0);
    }

    log.debug("Track {} is an MP3 file.", reference.identifier);

    Mp3TrackProvider file = new Mp3TrackProvider(null, inputStream);

    try {
      file.parseHeaders();

      return supportedFormat(this, null, AudioTrackInfoBuilder.create(reference, inputStream)
          .apply(file).setIsStream(!file.isSeekable()).build());
    } finally {
      file.close();
    }
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    return new Mp3AudioTrack(trackInfo, inputStream);
  }
}

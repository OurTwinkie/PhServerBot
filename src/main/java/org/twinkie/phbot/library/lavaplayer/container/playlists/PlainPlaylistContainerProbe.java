package org.twinkie.phbot.library.lavaplayer.container.playlists;

import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerHints;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.tools.DataFormatTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.STREAM_SCAN_DISTANCE;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.matchNextBytesAsRegex;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.refer;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.unsupportedFormat;

/**
 * Probe for a playlist containing the raw link without any format.
 */
public class PlainPlaylistContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(PlainPlaylistContainerProbe.class);

  private static final Pattern linkPattern = Pattern.compile("^(?:https?|icy)://.*");

  @Override
  public String getName() {
    return "plain";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    return false;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {
    if (!matchNextBytesAsRegex(inputStream, STREAM_SCAN_DISTANCE, linkPattern, StandardCharsets.UTF_8)) {
      return null;
    }

    log.debug("Track {} is a plain playlist file.", reference.identifier);
    return loadFromLines(DataFormatTools.streamToLines(inputStream, StandardCharsets.UTF_8));
  }

  private MediaContainerDetectionResult loadFromLines(String[] lines) {
    for (String line : lines) {
      Matcher matcher = linkPattern.matcher(line);

      if (matcher.matches()) {
        return refer(this, new AudioReference(matcher.group(0), null));
      }
    }

    return unsupportedFormat(this, "The playlist file contains no links.");
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    throw new UnsupportedOperationException();
  }
}

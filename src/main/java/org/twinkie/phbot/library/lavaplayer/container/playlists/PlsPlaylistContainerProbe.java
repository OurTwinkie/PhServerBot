package org.twinkie.phbot.library.lavaplayer.container.playlists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerHints;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerProbe;
import org.twinkie.phbot.library.lavaplayer.tools.DataFormatTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.UNKNOWN_TITLE;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetection.checkNextBytes;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.refer;
import static org.twinkie.phbot.library.lavaplayer.container.MediaContainerDetectionResult.unsupportedFormat;

/**
 * Probe for PLS playlist.
 */
public class PlsPlaylistContainerProbe implements MediaContainerProbe {
  private static final Logger log = LoggerFactory.getLogger(PlsPlaylistContainerProbe.class);

  private static final int[] PLS_HEADER = new int[] { '[', -1, 'l', 'a', 'y', 'l', 'i', 's', 't', ']' };

  private static Pattern filePattern = Pattern.compile("\\s*File([0-9]+)=((?:https?|icy)://.*)\\s*");
  private static Pattern titlePattern = Pattern.compile("\\s*Title([0-9]+)=(.*)\\s*");

  @Override
  public String getName() {
    return "pls";
  }

  @Override
  public boolean matchesHints(MediaContainerHints hints) {
    return false;
  }

  @Override
  public MediaContainerDetectionResult probe(AudioReference reference, SeekableInputStream inputStream) throws IOException {
    if (!checkNextBytes(inputStream, PLS_HEADER)) {
      return null;
    }

    log.debug("Track {} is a PLS playlist file.", reference.identifier);
    return loadFromLines(DataFormatTools.streamToLines(inputStream, StandardCharsets.UTF_8));
  }

  private MediaContainerDetectionResult loadFromLines(String[] lines) {
    Map<String, String> trackFiles = new HashMap<>();
    Map<String, String> trackTitles = new HashMap<>();

    for (String line : lines) {
      Matcher fileMatcher = filePattern.matcher(line);

      if (fileMatcher.matches()) {
        trackFiles.put(fileMatcher.group(1), fileMatcher.group(2));
        continue;
      }

      Matcher titleMatcher = titlePattern.matcher(line);
      if (titleMatcher.matches()) {
        trackTitles.put(titleMatcher.group(1), titleMatcher.group(2));
      }
    }

    for (Map.Entry<String, String> entry : trackFiles.entrySet()) {
      String title = trackTitles.get(entry.getKey());
      return refer(this, new AudioReference(entry.getValue(), title != null ? title : UNKNOWN_TITLE));
    }

    return unsupportedFormat(this, "The playlist file contains no links.");
  }

  @Override
  public AudioTrack createTrack(String parameters, AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    throw new UnsupportedOperationException();
  }
}

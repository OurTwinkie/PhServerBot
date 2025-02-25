package org.twinkie.phbot.library.lavaplayer.container.mp3;

import org.twinkie.phbot.library.lavaplayer.tools.Units;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;

import java.io.IOException;

/**
 * Seeker for an MP3 stream, which actually does not allow seeking and reports UnitConstants.DURATION_MS_UNKNOWN as
 * duration.
 */
public class Mp3StreamSeeker implements Mp3Seeker {
  @Override
  public long getDuration() {
    return Units.DURATION_MS_UNKNOWN;
  }

  @Override
  public boolean isSeekable() {
    return false;
  }

  @Override
  public long seekAndGetFrameIndex(long timecode, SeekableInputStream inputStream) throws IOException {
    throw new UnsupportedOperationException("Cannot seek on a stream.");
  }
}

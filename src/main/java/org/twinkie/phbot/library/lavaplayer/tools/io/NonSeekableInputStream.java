package org.twinkie.phbot.library.lavaplayer.tools.io;

import org.apache.commons.io.input.CountingInputStream;
import org.twinkie.phbot.library.lavaplayer.tools.Units;
import org.twinkie.phbot.library.lavaplayer.track.info.AudioTrackInfoProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class NonSeekableInputStream extends SeekableInputStream {
  private final CountingInputStream delegate;

  public NonSeekableInputStream(InputStream delegate) {
    super(Units.CONTENT_LENGTH_UNKNOWN, 0);
    this.delegate = new CountingInputStream(delegate);
  }

  @Override
  public long getPosition() {
    return delegate.getByteCount();
  }

  @Override
  protected void seekHard(long position) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean canSeekHard() {
    return false;
  }

  @Override
  public List<AudioTrackInfoProvider> getTrackInfoProviders() {
    return Collections.emptyList();
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public int read(byte[] buffer, int offset, int length) throws IOException {
    return delegate.read(buffer, offset, length);
  }

  @Override
  public void close() throws IOException {
    delegate.close();
  }
}

package org.twinkie.phbot.library.lavaplayer.source.stream;

import org.twinkie.phbot.library.lavaplayer.tools.io.ChainedInputStream;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.DelegatedAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.io.InputStream;

/**
 * Audio track that handles processing M3U segment streams which using MPEG-TS wrapped ADTS codec.
 */
public abstract class M3uStreamAudioTrack extends DelegatedAudioTrack {
  /**
   * @param trackInfo Track info
   */
  public M3uStreamAudioTrack(AudioTrackInfo trackInfo) {
    super(trackInfo);
  }

  protected abstract M3uStreamSegmentUrlProvider getSegmentUrlProvider();

  protected abstract HttpInterface getHttpInterface();

  protected abstract void processJoinedStream(
      LocalAudioTrackExecutor localExecutor,
      InputStream stream
  ) throws Exception;

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    try (final HttpInterface httpInterface = getHttpInterface()) {
      try (ChainedInputStream chainedInputStream = new ChainedInputStream(() -> getSegmentUrlProvider().getNextSegmentStream(httpInterface))) {
        processJoinedStream(localExecutor, chainedInputStream);
      }
    }
  }
}

package org.twinkie.phbot.library.lavaplayer.container.wav;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.BaseAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

/**
 * Audio track that handles a WAV stream
 */
public class WavAudioTrack extends BaseAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(WavAudioTrack.class);

  private final SeekableInputStream inputStream;

  /**
   * @param trackInfo Track info
   * @param inputStream Input stream for the WAV file
   */
  public WavAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    super(trackInfo);

    this.inputStream = inputStream;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    WavTrackProvider trackProvider = new WavFileLoader(inputStream).loadTrack(localExecutor.getProcessingContext());

    try {
      log.debug("Starting to play WAV track {}", getIdentifier());
      localExecutor.executeProcessingLoop(trackProvider::provideFrames, trackProvider::seekToTimecode);
    } finally {
      trackProvider.close();
    }
  }
}

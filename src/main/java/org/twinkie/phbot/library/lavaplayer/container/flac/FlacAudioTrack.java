package org.twinkie.phbot.library.lavaplayer.container.flac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.tools.io.SeekableInputStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.BaseAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

/**
 * Audio track that handles a FLAC stream
 */
public class FlacAudioTrack extends BaseAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(FlacAudioTrack.class);

  private final SeekableInputStream inputStream;

  /**
   * @param trackInfo Track info
   * @param inputStream Input stream for the FLAC file
   */
  public FlacAudioTrack(AudioTrackInfo trackInfo, SeekableInputStream inputStream) {
    super(trackInfo);

    this.inputStream = inputStream;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    FlacFileLoader file = new FlacFileLoader(inputStream);
    FlacTrackProvider trackProvider = file.loadTrack(localExecutor.getProcessingContext());

    try {
      log.debug("Starting to play FLAC track {}", getIdentifier());
      localExecutor.executeProcessingLoop(trackProvider::provideFrames, trackProvider::seekToTimecode);
    } finally {
      trackProvider.close();
    }
  }
}

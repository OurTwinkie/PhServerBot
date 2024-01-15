package org.twinkie.phbot.library.lavaplayer.source.yamusic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.mp3.Mp3AudioTrack;
import org.twinkie.phbot.library.lavaplayer.source.AudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.tools.io.PersistentHttpStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.DelegatedAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.net.URI;

/**
 * Audio track that handles processing Yandex Music tracks.
 */
public class YandexMusicAudioTrack extends DelegatedAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(YandexMusicAudioTrack.class);

  private final YandexMusicAudioSourceManager sourceManager;

  /**
   * @param trackInfo Track info
   * @param sourceManager Source manager which was used to find this track
   */
  public YandexMusicAudioTrack(AudioTrackInfo trackInfo, YandexMusicAudioSourceManager sourceManager) {
    super(trackInfo);
    this.sourceManager = sourceManager;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
      String trackMediaUrl = sourceManager.getDirectUrlLoader().getDirectUrl(trackInfo.identifier, "mp3");
      log.debug("Starting Yandex Music track from URL: {}", trackMediaUrl);
      try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackMediaUrl), null)) {
        processDelegate(new Mp3AudioTrack(trackInfo, stream), localExecutor);
      }
    }
  }

  @Override
  public AudioTrack makeClone() {
    return new YandexMusicAudioTrack(trackInfo, sourceManager);
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return sourceManager;
  }
}

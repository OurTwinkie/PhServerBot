package org.twinkie.phbot.library.lavaplayer.source.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.MediaContainerDescriptor;
import org.twinkie.phbot.library.lavaplayer.source.AudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.tools.Units;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.tools.io.PersistentHttpStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.DelegatedAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.InternalAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.net.URI;

/**
 * Audio track that handles processing HTTP addresses as audio tracks.
 */
public class HttpAudioTrack extends DelegatedAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(HttpAudioTrack.class);

  private final MediaContainerDescriptor containerTrackFactory;
  private final HttpAudioSourceManager sourceManager;

  /**
   * @param trackInfo Track info
   * @param containerTrackFactory Container track factory - contains the probe with its parameters.
   * @param sourceManager Source manager used to load this track
   */
  public HttpAudioTrack(AudioTrackInfo trackInfo, MediaContainerDescriptor containerTrackFactory,
                        HttpAudioSourceManager sourceManager) {

    super(trackInfo);

    this.containerTrackFactory = containerTrackFactory;
    this.sourceManager = sourceManager;
  }

  /**
   * @return The media probe which handles creating a container-specific delegated track for this track.
   */
  public MediaContainerDescriptor getContainerTrackFactory() {
    return containerTrackFactory;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
      log.debug("Starting http track from URL: {}", trackInfo.identifier);

      try (PersistentHttpStream inputStream = new PersistentHttpStream(httpInterface, new URI(trackInfo.identifier), Units.CONTENT_LENGTH_UNKNOWN)) {
        processDelegate((InternalAudioTrack) containerTrackFactory.createTrack(trackInfo, inputStream), localExecutor);
      }
    }
  }

  @Override
  protected AudioTrack makeShallowClone() {
    return new HttpAudioTrack(trackInfo, containerTrackFactory, sourceManager);
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return sourceManager;
  }
}

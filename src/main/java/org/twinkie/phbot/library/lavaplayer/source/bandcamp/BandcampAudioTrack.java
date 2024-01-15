package org.twinkie.phbot.library.lavaplayer.source.bandcamp;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.mp3.Mp3AudioTrack;
import org.twinkie.phbot.library.lavaplayer.source.AudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.tools.io.PersistentHttpStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;
import org.twinkie.phbot.library.lavaplayer.track.DelegatedAudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.playback.LocalAudioTrackExecutor;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * Audio track that handles processing Bandcamp tracks.
 */
public class BandcampAudioTrack extends DelegatedAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(BandcampAudioTrack.class);

  private final BandcampAudioSourceManager sourceManager;

  /**
   * @param trackInfo Track info
   * @param sourceManager Source manager which was used to find this track
   */
  public BandcampAudioTrack(AudioTrackInfo trackInfo, BandcampAudioSourceManager sourceManager) {
    super(trackInfo);

    this.sourceManager = sourceManager;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
      log.debug("Loading Bandcamp track page from URL: {}", trackInfo.identifier);

      String trackMediaUrl = getTrackMediaUrl(httpInterface);
      log.debug("Starting Bandcamp track from URL: {}", trackMediaUrl);

      try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(trackMediaUrl), null)) {
        processDelegate(new Mp3AudioTrack(trackInfo, stream), localExecutor);
      }
    }
  }

  private String getTrackMediaUrl(HttpInterface httpInterface) throws IOException {
    try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(trackInfo.identifier))) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new IOException("Invalid status code for track page: " + statusCode);
      }

      String responseText = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
      JsonBrowser trackInfo = sourceManager.readTrackListInformation(responseText);

      return trackInfo.get("trackinfo").index(0).get("file").get("mp3-128").text();
    }
  }

  @Override
  protected AudioTrack makeShallowClone() {
    return new BandcampAudioTrack(trackInfo, sourceManager);
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return sourceManager;
  }
}

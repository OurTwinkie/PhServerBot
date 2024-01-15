package org.twinkie.phbot.library.lavaplayer.source.nico;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.mpeg.MpegAudioTrack;
import org.twinkie.phbot.library.lavaplayer.source.AudioSourceManager;
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
import java.util.Map;

import static org.twinkie.phbot.library.lavaplayer.tools.DataFormatTools.convertToMapLayout;

/**
 * Audio track that handles processing NicoNico tracks.
 */
public class NicoAudioTrack extends DelegatedAudioTrack {
  private static final Logger log = LoggerFactory.getLogger(NicoAudioTrack.class);

  private final NicoAudioSourceManager sourceManager;

  /**
   * @param trackInfo Track info
   * @param sourceManager Source manager which was used to find this track
   */
  public NicoAudioTrack(AudioTrackInfo trackInfo, NicoAudioSourceManager sourceManager) {
    super(trackInfo);

    this.sourceManager = sourceManager;
  }

  @Override
  public void process(LocalAudioTrackExecutor localExecutor) throws Exception {
    sourceManager.checkLoggedIn();

    try (HttpInterface httpInterface = sourceManager.getHttpInterface()) {
      loadVideoMainPage(httpInterface);
      String playbackUrl = loadPlaybackUrl(httpInterface);

      log.debug("Starting NicoNico track from URL: {}", playbackUrl);

      try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, new URI(playbackUrl), null)) {
        processDelegate(new MpegAudioTrack(trackInfo, stream), localExecutor);
      }
    }
  }

  private void loadVideoMainPage(HttpInterface httpInterface) throws IOException {
    HttpGet request = new HttpGet("http://www.nicovideo.jp/watch/" + trackInfo.identifier);

    try (CloseableHttpResponse response = httpInterface.execute(request)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new IOException("Unexpected status code from video main page: " + statusCode);
      }

      EntityUtils.consume(response.getEntity());
    }
  }

  private String loadPlaybackUrl(HttpInterface httpInterface) throws IOException {
    HttpGet request = new HttpGet("http://flapi.nicovideo.jp/api/getflv/" + trackInfo.identifier);

    try (CloseableHttpResponse response = httpInterface.execute(request)) {
      int statusCode = response.getStatusLine().getStatusCode();
      if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new IOException("Unexpected status code from playback parameters page: " + statusCode);
      }

      String text = EntityUtils.toString(response.getEntity());
      Map<String, String> format = convertToMapLayout(URLEncodedUtils.parse(text, StandardCharsets.UTF_8));

      return format.get("url");
    }
  }

  @Override
  protected AudioTrack makeShallowClone() {
    return new NicoAudioTrack(trackInfo, sourceManager);
  }

  @Override
  public AudioSourceManager getSourceManager() {
    return sourceManager;
  }
}

package org.twinkie.phbot.library.lavaplayer.source.vimeo;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.twinkie.phbot.library.lavaplayer.player.AudioPlayerManager;
import org.twinkie.phbot.library.lavaplayer.source.AudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.tools.DataFormatTools;
import org.twinkie.phbot.library.lavaplayer.tools.ExceptionTools;
import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpConfigurable;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterfaceManager;
import org.twinkie.phbot.library.lavaplayer.track.AudioItem;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

/**
 * Audio source manager which detects Vimeo tracks by URL.
 */
public class VimeoAudioSourceManager implements AudioSourceManager, HttpConfigurable {
  private static final String TRACK_URL_REGEX = "^https://vimeo.com/[0-9]+(?:\\?.*|)$";
  private static final Pattern trackUrlPattern = Pattern.compile(TRACK_URL_REGEX);

  private final HttpInterfaceManager httpInterfaceManager;

  /**
   * Create an instance.
   */
  public VimeoAudioSourceManager() {
    httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
  }

  @Override
  public String getSourceName() {
    return "vimeo";
  }

  @Override
  public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
    if (!trackUrlPattern.matcher(reference.identifier).matches()) {
      return null;
    }

    try (HttpInterface httpInterface = httpInterfaceManager.getInterface()) {
      return loadFromTrackPage(httpInterface, reference.identifier);
    } catch (IOException e) {
      throw new FriendlyException("Loading Vimeo track information failed.", SUSPICIOUS, e);
    }
  }

  @Override
  public boolean isTrackEncodable(AudioTrack track) {
    return true;
  }

  @Override
  public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
    // Nothing special to encode
  }

  @Override
  public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
    return new VimeoAudioTrack(trackInfo, this);
  }

  @Override
  public void shutdown() {
    ExceptionTools.closeWithWarnings(httpInterfaceManager);
  }

  /**
   * @return Get an HTTP interface for a playing track.
   */
  public HttpInterface getHttpInterface() {
    return httpInterfaceManager.getInterface();
  }

  @Override
  public void configureRequests(Function<RequestConfig, RequestConfig> configurator) {
    httpInterfaceManager.configureRequests(configurator);
  }

  @Override
  public void configureBuilder(Consumer<HttpClientBuilder> configurator) {
    httpInterfaceManager.configureBuilder(configurator);
  }

  JsonBrowser loadConfigJsonFromPageContent(String content) throws IOException {
    String configText = DataFormatTools.extractBetween(content, "window.vimeo.clip_page_config = ", "\n");

    if (configText != null) {
      return JsonBrowser.parse(configText);
    }

    return null;
  }

  private AudioItem loadFromTrackPage(HttpInterface httpInterface, String trackUrl) throws IOException {
    try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(trackUrl))) {
      int statusCode = response.getStatusLine().getStatusCode();

      if (statusCode == HttpStatus.SC_NOT_FOUND) {
        return AudioReference.NO_TRACK;
      } else if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new FriendlyException("Server responded with an error.", SUSPICIOUS,
            new IllegalStateException("Response code is " + statusCode));
      }

      return loadTrackFromPageContent(trackUrl, IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));
    }
  }

  private AudioTrack loadTrackFromPageContent(String trackUrl, String content) throws IOException {
    JsonBrowser config = loadConfigJsonFromPageContent(content);

    if (config == null) {
      throw new FriendlyException("Track information not found on the page.", SUSPICIOUS, null);
    }

    return new VimeoAudioTrack(new AudioTrackInfo(
        config.get("clip").get("title").text(),
        config.get("owner").get("display_name").text(),
        (long) (config.get("clip").get("duration").get("raw").as(Double.class) * 1000.0),
        trackUrl,
        false,
        trackUrl,
        config.get("thumbnail").get("src").text(),
        null
    ), this);
  }
}

package org.twinkie.phbot.library.lavaplayer.source.beam;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.twinkie.phbot.library.lavaplayer.player.AudioPlayerManager;
import org.twinkie.phbot.library.lavaplayer.source.AudioSourceManager;
import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.Units;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

/**
 * Audio source manager which detects Beam.pro tracks by URL.
 */
public class BeamAudioSourceManager implements AudioSourceManager, HttpConfigurable {
  private static final String STREAM_NAME_REGEX = "^https://(?:www\\.)?(?:beam\\.pro|mixer\\.com)/([^/]+)$";
  private static final Pattern streamNameRegex = Pattern.compile(STREAM_NAME_REGEX);

  private final HttpInterfaceManager httpInterfaceManager;

  /**
   * Create an instance.
   */
  public BeamAudioSourceManager() {
    this.httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
  }

  @Override
  public String getSourceName() {
    return "beam.pro";
  }

  @Override
  public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
    String streamName = getChannelNameFromUrl(reference.identifier);
    if (streamName == null) {
      return null;
    }

    JsonBrowser channelInfo = fetchStreamChannelInfo(streamName);

    if (channelInfo == null) {
      return AudioReference.NO_TRACK;
    } else {
      String displayName = channelInfo.get("name").text();
      String id = getPlayedStreamId(channelInfo);
      String thumbnailUrl = channelInfo.get("thumbnail").get("url").text();

      if (displayName == null || id == null) {
        throw new IllegalStateException("Expected id and name fields from Beam channel info.");
      }

      return new BeamAudioTrack(new AudioTrackInfo(
          displayName,
          streamName,
          Units.DURATION_MS_UNKNOWN,
          id + "|" + streamName + "|" + reference.identifier,
          true,
          "https://beam.pro/" + streamName,
          thumbnailUrl,
          null
      ), this);
    }
  }

  @Override
  public boolean isTrackEncodable(AudioTrack track) {
    return true;
  }

  @Override
  public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
    // Nothing special to do, URL (identifier) is enough
  }

  @Override
  public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
    return new BeamAudioTrack(trackInfo, this);
  }

  @Override
  public void shutdown() {
    // Nothing to shut down.
  }

  private static String getPlayedStreamId(JsonBrowser channelInfo) {
    // If there is a hostee, this means that the current channel itself is not actually broadcasting anything and all
    // further requests should be performed with the ID of the hostee. Hostee is not rechecked later so it will keep
    // playing the current hostee even if it changes.
    String hosteeId = channelInfo.get("hosteeId").text();
    return hosteeId != null ? hosteeId : channelInfo.get("id").text();
  }

  private static String getChannelNameFromUrl(String url) {
    Matcher matcher = streamNameRegex.matcher(url);
    if (!matcher.matches()) {
      return null;
    }

    return matcher.group(1);
  }

  private JsonBrowser fetchStreamChannelInfo(String name) {
    try (HttpInterface httpInterface = getHttpInterface()) {
      return HttpClientTools.fetchResponseAsJson(httpInterface,
          new HttpGet("https://mixer.com/api/v1/channels/" + name + "?noCount=1"));
    } catch (IOException e) {
      throw new FriendlyException("Loading Beam channel information failed.", SUSPICIOUS, e);
    }
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
}

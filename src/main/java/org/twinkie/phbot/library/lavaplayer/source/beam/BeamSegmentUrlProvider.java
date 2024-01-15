package org.twinkie.phbot.library.lavaplayer.source.beam;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twinkie.phbot.library.lavaplayer.container.playlists.ExtendedM3uParser;
import org.twinkie.phbot.library.lavaplayer.source.stream.M3uStreamSegmentUrlProvider;
import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.io.IOException;
import java.util.List;

import static org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools.fetchResponseLines;

/**
 * Provider for Beam segment URLs from a channel.
 */
public class BeamSegmentUrlProvider extends M3uStreamSegmentUrlProvider {
  private static final Logger log = LoggerFactory.getLogger(BeamSegmentUrlProvider.class);

  private final String channelId;
  private String streamSegmentPlaylistUrl;

  /**
   * @param channelId Channel ID number.
   */
  public BeamSegmentUrlProvider(String channelId) {
    this.channelId = channelId;
  }

  @Override
  protected String getQualityFromM3uDirective(ExtendedM3uParser.Line directiveLine) {
    return directiveLine.directiveArguments.get("NAME");
  }

  @Override
  protected String fetchSegmentPlaylistUrl(HttpInterface httpInterface) throws IOException {
    if (streamSegmentPlaylistUrl != null) {
      return streamSegmentPlaylistUrl;
    }

    HttpUriRequest jsonRequest = new HttpGet("https://mixer.com/api/v1/channels/" + channelId + "/manifest.light2");
    JsonBrowser lightManifest = HttpClientTools.fetchResponseAsJson(httpInterface, jsonRequest);

    if (lightManifest == null) {
      throw new IllegalStateException("Did not find light manifest at " + jsonRequest.getURI());
    }

    HttpUriRequest manifestRequest = new HttpGet("https://mixer.com" + lightManifest.get("hlsSrc").text());
    List<ChannelStreamInfo> streams = loadChannelStreamsList(fetchResponseLines(httpInterface, manifestRequest,
        "mixer channel streams list"));

    if (streams.isEmpty()) {
      throw new IllegalStateException("No streams available on channel.");
    }

    ChannelStreamInfo stream = streams.get(0);

    log.debug("Chose stream with quality {} from url {}", stream.quality, stream.url);
    streamSegmentPlaylistUrl = stream.url;
    return streamSegmentPlaylistUrl;
  }

  @Override
  protected HttpUriRequest createSegmentGetRequest(String url) {
    return new HttpGet(url);
  }
}

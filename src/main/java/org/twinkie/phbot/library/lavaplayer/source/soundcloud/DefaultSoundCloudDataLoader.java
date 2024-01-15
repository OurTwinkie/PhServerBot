package org.twinkie.phbot.library.lavaplayer.source.soundcloud;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class DefaultSoundCloudDataLoader implements SoundCloudDataLoader {
  @Override
  public JsonBrowser load(HttpInterface httpInterface, String url) throws IOException {
    try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(buildUri(url)))) {
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
        return JsonBrowser.NULL_BROWSER;
      }

      HttpClientTools.assertSuccessWithContent(response, "video page response");

      String json = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

      return JsonBrowser.parse(json);
    }
  }

  private URI buildUri(String url) {
    try {
      return new URIBuilder("https://api-v2.soundcloud.com/resolve")
              .addParameter("url", url)
              .build();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}

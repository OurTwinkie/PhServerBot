package org.twinkie.phbot.library.lavaplayer.source.soundcloud;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.twinkie.phbot.library.lavaplayer.tools.ExceptionTools;
import org.twinkie.phbot.library.lavaplayer.tools.FriendlyException;
import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.tools.io.PersistentHttpStream;
import org.twinkie.phbot.library.lavaplayer.track.AudioReference;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.twinkie.phbot.library.lavaplayer.tools.FriendlyException.Severity.SUSPICIOUS;

public class SoundCloudHelper {
  public static String nonMobileUrl(String url) {
    if (url.startsWith("https://m.")) {
      return "https://" + url.substring("https://m.".length());
    } else {
      return url;
    }
  }

  public static String loadPlaybackUrl(HttpInterface httpInterface, String jsonUrl) throws IOException {
    try (PersistentHttpStream stream = new PersistentHttpStream(httpInterface, URI.create(jsonUrl), null)) {
      if (!HttpClientTools.isSuccessWithContent(stream.checkStatusCode())) {
        throw new IOException("Invalid status code for soundcloud stream: " + stream.checkStatusCode());
      }

      JsonBrowser json = JsonBrowser.parse(stream);
      return json.get("url").text();
    }
  }

  public static AudioReference redirectMobileLink(HttpInterface httpInterface, AudioReference reference) {
    try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(reference.identifier))) {
      HttpClientTools.assertSuccessWithContent(response, "mobile redirect response");
      HttpClientContext context = httpInterface.getContext();
      List<URI> redirects = context.getRedirectLocations();
      if (redirects != null && !redirects.isEmpty()) {
        return new AudioReference(redirects.get(0).toString(), null);
      } else {
        throw new FriendlyException("Unable to process soundcloud mobile link", SUSPICIOUS,
            new IllegalStateException("Expected soundcloud to redirect soundcloud.app.goo.gl link to a valid track/playlist link, but it did not redirect at all"));
      }
    } catch (Exception e) {
      throw ExceptionTools.wrapUnfriendlyExceptions(e);
    }
  }
}

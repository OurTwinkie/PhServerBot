package org.twinkie.phbot.library.lavaplayer.tools.http;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.twinkie.phbot.library.lavaplayer.tools.ExceptionTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpClientTools;
import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;

import java.io.IOException;
import java.io.InputStream;

public class HttpStreamTools {
  public static InputStream streamContent(HttpInterface httpInterface, HttpUriRequest request) {
    CloseableHttpResponse response = null;
    boolean success = false;

    try {
      response = httpInterface.execute(request);
      int statusCode = response.getStatusLine().getStatusCode();

      if (!HttpClientTools.isSuccessWithContent(statusCode)) {
        throw new IOException("Invalid status code from " + request.getURI() + " URL: " + statusCode);
      }

      success = true;
      return response.getEntity().getContent();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      if (response != null && !success) {
        ExceptionTools.closeWithWarnings(response);
      }
    }
  }
}

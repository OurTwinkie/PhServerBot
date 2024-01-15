package org.twinkie.phbot.library.lavaplayer.tools.http;

import org.twinkie.phbot.library.lavaplayer.tools.io.HttpConfigurable;

public interface ExtendedHttpConfigurable extends HttpConfigurable {
  void setHttpContextFilter(HttpContextFilter filter);
}

package org.twinkie.phbot.library.lavaplayer.source.yamusic;

import org.twinkie.phbot.library.lavaplayer.tools.http.ExtendedHttpConfigurable;

public interface YandexMusicApiLoader {
  ExtendedHttpConfigurable getHttpConfiguration();
  void shutdown();
}

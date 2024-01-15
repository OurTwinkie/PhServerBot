package org.twinkie.phbot.library.lavaplayer.source.yamusic;

public interface YandexMusicDirectUrlLoader extends YandexMusicApiLoader {
  String getDirectUrl(String trackId, String codec);
}

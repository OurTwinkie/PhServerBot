package org.twinkie.phbot.library.lavaplayer.source.yamusic;

import org.twinkie.phbot.library.lavaplayer.track.AudioItem;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public interface YandexMusicPlaylistLoader extends YandexMusicApiLoader {
  AudioItem loadPlaylist(String login, String id, String trackProperty, Function<AudioTrackInfo, AudioTrack> trackFactory);
  AudioItem loadPlaylist(String album, String trackProperty, Function<AudioTrackInfo, AudioTrack> trackFactory);
}

package org.twinkie.phbot.library.lavaplayer.source.yamusic;

import org.twinkie.phbot.library.lavaplayer.track.AudioItem;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public interface YandexMusicSearchResultLoader extends YandexMusicApiLoader {
  AudioItem loadSearchResult(String query,
                             YandexMusicPlaylistLoader playlistLoader,
                             Function<AudioTrackInfo, AudioTrack> trackFactory);
}

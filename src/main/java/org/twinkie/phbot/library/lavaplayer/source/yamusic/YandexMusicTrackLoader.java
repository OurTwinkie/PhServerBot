package org.twinkie.phbot.library.lavaplayer.source.yamusic;

import org.twinkie.phbot.library.lavaplayer.track.AudioItem;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public interface YandexMusicTrackLoader extends YandexMusicApiLoader {
  AudioItem loadTrack(String albumId, String trackId, Function<AudioTrackInfo, AudioTrack> trackFactory);
}

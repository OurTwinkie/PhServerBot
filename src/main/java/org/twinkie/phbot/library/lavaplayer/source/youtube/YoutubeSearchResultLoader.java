package org.twinkie.phbot.library.lavaplayer.source.youtube;

import org.twinkie.phbot.library.lavaplayer.tools.http.ExtendedHttpConfigurable;
import org.twinkie.phbot.library.lavaplayer.track.AudioItem;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public interface YoutubeSearchResultLoader {
  AudioItem loadSearchResult(String query, Function<AudioTrackInfo, AudioTrack> trackFactory);

  ExtendedHttpConfigurable getHttpConfiguration();
}

package org.twinkie.phbot.library.lavaplayer.source.youtube;

import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterface;
import org.twinkie.phbot.library.lavaplayer.track.AudioPlaylist;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public interface YoutubePlaylistLoader {
  void setPlaylistPageCount(int playlistPageCount);

  AudioPlaylist load(HttpInterface httpInterface, String playlistId, String selectedVideoId,
                     Function<AudioTrackInfo, AudioTrack> trackFactory);
}

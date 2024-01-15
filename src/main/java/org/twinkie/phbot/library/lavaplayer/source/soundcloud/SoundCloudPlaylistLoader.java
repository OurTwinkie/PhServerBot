package org.twinkie.phbot.library.lavaplayer.source.soundcloud;

import org.twinkie.phbot.library.lavaplayer.tools.io.HttpInterfaceManager;
import org.twinkie.phbot.library.lavaplayer.track.AudioPlaylist;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrack;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.function.Function;

public interface SoundCloudPlaylistLoader {
  AudioPlaylist load(
      String identifier,
      HttpInterfaceManager httpInterfaceManager,
      Function<AudioTrackInfo, AudioTrack> trackFactory
  );
}

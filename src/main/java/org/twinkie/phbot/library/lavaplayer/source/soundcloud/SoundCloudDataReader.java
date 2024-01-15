package org.twinkie.phbot.library.lavaplayer.source.soundcloud;

import org.twinkie.phbot.library.lavaplayer.tools.JsonBrowser;
import org.twinkie.phbot.library.lavaplayer.track.AudioTrackInfo;

import java.util.List;

public interface SoundCloudDataReader {
  JsonBrowser findTrackData(JsonBrowser rootData);

  String readTrackId(JsonBrowser trackData);

  boolean isTrackBlocked(JsonBrowser trackData);

  AudioTrackInfo readTrackInfo(JsonBrowser trackData, String identifier);

  List<SoundCloudTrackFormat> readTrackFormats(JsonBrowser trackData);

  JsonBrowser findPlaylistData(JsonBrowser rootData, String kind);

  String readPlaylistName(JsonBrowser playlistData);

  String readPlaylistIdentifier(JsonBrowser playlistData);

  List<JsonBrowser> readPlaylistTracks(JsonBrowser playlistData);
}
